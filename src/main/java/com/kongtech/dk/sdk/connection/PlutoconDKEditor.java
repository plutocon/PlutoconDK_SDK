package com.kongtech.dk.sdk.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import com.kongtech.dk.sdk.utils.DKUUID;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlutoconDKEditor {

    private ConcurrentHashMap<ParcelUuid, BluetoothGattCharacteristic> characteristics;
    private List<BluetoothGattCharacteristic> editList;

    private BluetoothGatt sensorGatt;

    private OnEditCompleteCallback onEditCompleteCallback;

    public PlutoconDKEditor(BluetoothGatt sensorGatt){
        this.sensorGatt = sensorGatt;
        this.editList = new ArrayList<>();
        this.characteristics = new ConcurrentHashMap<>();

        List<BluetoothGattService> sensorServices = sensorGatt.getServices();
        for(BluetoothGattService service : sensorServices){
            for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                if((characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0)
                    characteristics.put(new ParcelUuid(characteristic.getUuid()), characteristic);
            }
        }
    }

    public PlutoconDKEditor setUUID(ParcelUuid uuid){
        BluetoothGattCharacteristic characteristic = characteristics.get(DKUUID.UUID_CHARACTERISTIC);
        if(characteristic == null) return this;

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getUuid().getMostSignificantBits());
        bb.putLong(uuid.getUuid().getLeastSignificantBits());
        characteristic.setValue(bb.array());
        editList.add(characteristic);
        return this;
    }

    public PlutoconDKEditor setProperty(ParcelUuid uuid, int value){
        BluetoothGattCharacteristic characteristic = characteristics.get(uuid);

        if(characteristic == null) return this;

        byte[] d = new byte[2];
        short v = (short)value;
        d[0] = (byte)(v >> 8);
        d[1] = (byte)(v & 0xFF);

        characteristic.setValue(d);
        editList.add(characteristic);

        return this;
    }

    public PlutoconDKEditor setProperty(ParcelUuid uuid, String value){
        BluetoothGattCharacteristic characteristic = characteristics.get(uuid);

        if(characteristic == null) return this;

        characteristic.setValue(value);
        editList.add(characteristic);
        return this;
    }

    public PlutoconDKEditor setProperty(ParcelUuid uuid, byte[] value){
        BluetoothGattCharacteristic characteristic = characteristics.get(uuid);

        if(characteristic == null) return this;

        characteristic.setValue(value);
        editList.add(characteristic);
        return this;
    }

    public void commit(){
        executeNext();
    }

    public boolean executeNext(){
        if(editList.size() > 0 ){
            BluetoothGattCharacteristic characteristic = editList.get(0);
            editList.remove(0);
            boolean test = sensorGatt.writeCharacteristic(characteristic);
            return true;
        }
        return false;
    }

    public PlutoconDKEditor setOnEditCompleteCallback(OnEditCompleteCallback onEditCompleteCallback) {
        this.onEditCompleteCallback = onEditCompleteCallback;
        return this;
    }

    public void onEditComplete(){
        if(onEditCompleteCallback != null)
            onEditCompleteCallback.onEditCompleteCallback();
    }

    public interface OnEditCompleteCallback{
        void onEditCompleteCallback();
    }
}
