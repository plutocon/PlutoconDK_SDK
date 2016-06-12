package com.kongtech.dk.sdk.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.ParcelUuid;

import com.kongtech.dk.sdk.sensors.Sensor;
import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;
import com.kongtech.dk.sdk.utils.Plog;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SensorConnection {

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private Context context;

    private Sensor sensor;
    private BluetoothGatt bluetoothGatt;
    private String address;
    private int type;

    private OnConnectionStateChangeCallback onConnectionStateChangeListener;
    private OnConnectionRemoteRssiCallback onConnectionRemoteRssiCallback;

    private ConcurrentHashMap<ParcelUuid, BluetoothGattCharacteristic> characteristics;
    private SensorEditor sensorEditor = null;
    private SensorReader sensorReader = null;

    private SensorDataReceiver sensorDataReceiver;
    private OnBatteryInfoCallback onBatteryInfoCallback;

    private ConcurrentLinkedQueue<SensorOperation> notifyQueue;
    private boolean isWritingDescriptor;

    private boolean isConnected = false;

    public SensorConnection(Context context, Sensor sensor) {
        this.context = context;
        this.sensor = sensor;
        this.address = sensor.getMacAddress();
        this.type = sensor.getType();

        this.notifyQueue = new ConcurrentLinkedQueue<>();
        isWritingDescriptor = false;
    }

    public SensorConnection(Context context, String address, int type) {
        this.context = context;
        this.address = address;
        this.type = type;

        this.notifyQueue = new ConcurrentLinkedQueue<>();
        isWritingDescriptor = false;
    }

    public void disconnect() {
        this.notifyDisconnected();
    }

    public void connect(final OnConnectionStateChangeCallback onConnectionStateChangeListener) {
        this.onConnectionStateChangeListener = onConnectionStateChangeListener;

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(this.address);

        Plog.d("Trying to Connect: " + this.address);

        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if(status == BluetoothGatt.GATT_SUCCESS) isConnected = true;
                else isConnected = false;
                if (SensorConnection.this.onConnectionStateChangeListener != null) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Plog.d("GATT Service discovered");
                        SensorConnection.this.getCharacteristicList();
                        SensorConnection.this.readDefaultProperty(new SensorReader.OnReadCompleteCallback() {
                            @Override
                            public void onReadComplete() {
                                SensorConnection.this.onConnectionStateChangeListener.onConnectionStateConnected();
                            }
                        });
                    } else {
                        Plog.d("GATT Service discover Failed");
                        SensorConnection.this.notifyDisconnected();
                    }
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(status == BluetoothGatt.GATT_SUCCESS) isConnected = true;
                else if(status == BluetoothGatt.GATT_FAILURE) isConnected = false;
                if (SensorConnection.this.onConnectionStateChangeListener != null) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Plog.d("Connected to GATT server.");
                        Plog.d("Attempting to start service discovery:"
                                + SensorConnection.this.bluetoothGatt.discoverServices());
                    } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        SensorConnection.this.notifyDisconnected();
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (sensorEditor != null && !sensorEditor.executeNext()) {
                    sensorEditor.onEditComplete();
                    sensorEditor = null;
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (sensorReader != null && !sensorReader.executeNext()) {
                    sensorReader.onReadComplete();
                    sensorReader = null;
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Plog.d("onDescriptorWrite ");
                notifyQueue.poll();
                if(notifyQueue.size() > 0){
                    SensorOperation operation = notifyQueue.poll();
                    operation.execute(gatt);
                }
                else isWritingDescriptor = false;
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                if (characteristic.getUuid().compareTo(DKUUID.BATTERY_CHARACTERISTIC.getUuid()) == 0) {
                    if (onBatteryInfoCallback != null) {
                        byte[] data = characteristic.getValue();
                        int value = (data[0] << 8) | data[1];
                        onBatteryInfoCallback.onBatteryInfoCallback(value);
                    }
                } else if (sensorDataReceiver != null) {
                    sensor.dataReceiveCallback(characteristic.getValue(), sensorDataReceiver);
                }
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if (onConnectionRemoteRssiCallback != null) {
                    onConnectionRemoteRssiCallback.onConnectionRemoteRssiCallback(rssi);
                }
            }
        });
    }

    private void getCharacteristicList() {
        characteristics = new ConcurrentHashMap<>();
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                characteristics.put(new ParcelUuid(characteristic.getUuid()), characteristic);
            }
        }
    }

    private void notifyDisconnected() {
        isConnected = false;
        if (this.bluetoothGatt != null) {
            this.bluetoothGatt.close();
            characteristics = null;
            if (onConnectionStateChangeListener != null)
                onConnectionStateChangeListener.onConnectionStateDisconnected();
        }
    }

    public void setOnConnectionRemoteRssiCallback(OnConnectionRemoteRssiCallback onConnectionRemoteRssiCallback) {
        this.onConnectionRemoteRssiCallback = onConnectionRemoteRssiCallback;
    }

    public SensorEditor getSensorEditor() {
        return sensorEditor = new SensorEditor(bluetoothGatt);
    }

    public SensorReader getSensorReader() {
        return sensorReader = new SensorReader(bluetoothGatt);
    }

    public void setNotifySensorData(final boolean enabled, SensorDataReceiver sensorDataReceiver) {
        final BluetoothGattCharacteristic characteristic = characteristics.get(sensor.getNotificationUUID());
        if (characteristic != null) {

            this.sensorDataReceiver = sensorDataReceiver;

            SensorOperation oper = new SensorOperation() {
                @Override
                public void execute(BluetoothGatt bluetoothGatt) {
                    bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

                    BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
                    descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] { 0x00, 0x00 });
                    bluetoothGatt.writeDescriptor(descriptor);

                }
            };
            notifyQueue.add(oper);
            if(!isWritingDescriptor){
                oper.execute(bluetoothGatt);
                isWritingDescriptor = true;
            }
        }
    }

    /*public void setNotifyBatteryInfo(boolean enabled, OnBatteryInfoCallback onBatteryInfoCallback) {
        final BluetoothGattCharacteristic characteristic = characteristics.get(DKUUID.BATTERY_CHARACTERISTIC);
        if (characteristic != null) {

            this.onBatteryInfoCallback = onBatteryInfoCallback;

            SensorOperation oper = new SensorOperation() {
                @Override
                public void execute(BluetoothGatt bluetoothGatt) {
                    bluetoothGatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);

                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                }
            };
            notifyQueue.add(oper);
            if(!isWritingDescriptor){
                oper.execute(bluetoothGatt);
                isWritingDescriptor = true;
            }
        }
    }*/

    private void readDefaultProperty(SensorReader.OnReadCompleteCallback onReadCompleteCallback) {
        SensorReader reader = this.getSensorReader();

        reader.getProperty(DKUUID.ADV_INTERVAL_CHARACTERISTIC)
                .getProperty(DKUUID.TX_LEVEL_CHARACTERISTIC)
                .getProperty(DKUUID.BATTERY_CHARACTERISTIC)
                .getProperty(DKUUID.SOFTWARE_VERSION_CHARACTERISTIC)
                .getProperty(DKUUID.HARDWARE_VERSION_CHARACTERISTIC)
                .getProperty(DKUUID.MODEL_NUMBER_CHARACTERISTIC)
                .getProperty(DKUUID.MANUFACTURE_NAME_CHARACTERISTIC)
                .setOnReadCompleteCallback(onReadCompleteCallback);
        if(type == Sensor.TYPE_BEACON) {
            reader.getProperty(DKUUID.UUID_CHARACTERISTIC);
        }
        reader.commit();
    }

    public ParcelUuid getUUID(){
        byte[] data = characteristics.get(DKUUID.UUID_CHARACTERISTIC).getValue();
        ByteBuffer bb = ByteBuffer.wrap(data);
        long high = bb.getLong();
        long low = bb.getLong();
        return new ParcelUuid(new UUID(high, low));
    }

    public int getAdvertisingInterval() {
        byte[] data = characteristics.get(DKUUID.ADV_INTERVAL_CHARACTERISTIC).getValue();
        return (((int) data[0]) << 8) | ((int) data[1] & 0xFF);
    }

    public int getBroadcastingPower() {
        byte[] data = characteristics.get(DKUUID.TX_LEVEL_CHARACTERISTIC).getValue();
        return (short)(((int) data[0]) << 8) | ((int) data[1] & 0xFF);
    }

    public int getBatteryVoltage() {
        byte[] data = characteristics.get(DKUUID.BATTERY_CHARACTERISTIC).getValue();
        return (short)(((int) data[0]) << 8) | ((int) data[1] & 0xFF);
    }

    public void getRemoteRssi(OnConnectionRemoteRssiCallback onConnectionRemoteRssiCallback) {
        this.onConnectionRemoteRssiCallback = onConnectionRemoteRssiCallback;
        if (bluetoothGatt != null) bluetoothGatt.readRemoteRssi();
    }

    public String getSoftwareVersion() {
        return new String(characteristics.get(DKUUID.SOFTWARE_VERSION_CHARACTERISTIC).getValue());
    }

    public String getHardwareVersion() {
        return new String(characteristics.get(DKUUID.HARDWARE_VERSION_CHARACTERISTIC).getValue());
    }

    public String getModelNumber() {
        return new String(characteristics.get(DKUUID.MODEL_NUMBER_CHARACTERISTIC).getValue());
    }

    public String getManufactureName(){
        return new String(characteristics.get(DKUUID.MANUFACTURE_NAME_CHARACTERISTIC).getValue());
    }

    public boolean isConnected() {
        return isConnected;
    }

    public interface OnConnectionRemoteRssiCallback {
        void onConnectionRemoteRssiCallback(int rssi);
    }

    public interface OnConnectionStateChangeCallback {
        void onConnectionStateDisconnected();

        void onConnectionStateConnected();
    }

    public interface OnBatteryInfoCallback {
        void onBatteryInfoCallback(int value);
    }

}
