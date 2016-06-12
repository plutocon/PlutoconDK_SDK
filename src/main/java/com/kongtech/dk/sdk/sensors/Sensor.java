package com.kongtech.dk.sdk.sensors;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.repackaged.ScanRecord;
import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public abstract class Sensor implements Parcelable, Comparable<Sensor> {

    public static final int TYPE_BEACON = 0;
    public static final int TYPE_ACC = 1;
    public static final int TYPE_EMG = 2;
    public static final int TYPE_TEMP = 3;
    public static final int TYPE_MIC = 4;
    public static final int TYPE_TOUCH = 5;
    public static final int TYPE_HID = 6;

    public static final String TYPE_STRING_TEMP = "DK_TEMP";
    public static final String TYPE_STRING_ACC = "DK_ACC";
    public static final String TYPE_STRING_BEACON = "DK_BEACON";
    public static final String TYPE_STRING_EMG = "DK_EMG";
    public static final String TYPE_STRING_MIC = "DK_MIC";
    public static final String TYPE_STRING_TOUCH = "DK_TOUCH";
    public static final String TYPE_STRING_HID = "DK_HID";

    private String name;
    private String macAddress;
    private ParcelUuid uuid;
    private int rssi;
    private long lastSeenMillis;

    private int interval;
    private boolean isSelect;

    public abstract String getTypeString();

    public abstract int getType();

    public abstract ParcelUuid getNotificationUUID();

    public abstract void dataReceiveCallback(byte[] data, SensorDataReceiver sensorDataReceiver);

    public Sensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        this.name = name;
        this.macAddress = macAddress;
        this.rssi = rssi;
        this.lastSeenMillis = lastSeenMillis;
        this.interval = 0;
        this.uuid = null;
        this.update(manufacturerSpecificData);
    }

    protected Sensor(Parcel source) {
        int type = source.readInt();
        this.name = source.readString();
        this.macAddress = source.readString();
        this.rssi = source.readInt();
        this.lastSeenMillis = source.readLong();
        this.interval = source.readInt();
        this.uuid = source.readParcelable(ParcelUuid.class.getClassLoader());
    }

    public String getName() {
        return name;
    }

    public void setSensorName(String sensorName) {
        this.name = sensorName;
    }

    public int getRSSI() {
        return rssi;
    }

    public long getLastSeenMillis() {
        return lastSeenMillis;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public ParcelUuid getUUID() {
        return uuid;
    }

    public void setUUID(ParcelUuid UUID) {
        this.uuid = UUID;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void select(boolean select) {
        this.isSelect = select;
    }

    public void update(byte[] manufacturerSpecificData) {
        if (manufacturerSpecificData == null) return;

        byte[] uuidBytes = Arrays.copyOfRange(manufacturerSpecificData, 2, 18);
        ByteBuffer proximityUUIDBuffer = ByteBuffer.wrap(uuidBytes);

        this.uuid = new ParcelUuid(new UUID(proximityUUIDBuffer.getLong(), proximityUUIDBuffer.getLong()));
    }

    private static int getCategoryFromData(byte[] serviceData) {
        return serviceData[1];
    }

    private static int getTypeFromData(byte[] serviceData) {
        return serviceData[0];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getType());
        dest.writeString(name);
        dest.writeString(macAddress);
        dest.writeInt(rssi);
        dest.writeLong(lastSeenMillis);
        dest.writeInt(interval);
        dest.writeParcelable(uuid, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Sensor> CREATOR = new Creator<Sensor>() {
        @Override
        public Sensor createFromParcel(Parcel source) {
            int type = source.readInt();
            String name = source.readString();
            String address = source.readString();
            int rssi = source.readInt();
            long lastSeenMillis = source.readLong();
            int interval = source.readInt();
            ParcelUuid uuid = source.readParcelable(ParcelUuid.class.getClassLoader());
            Sensor sensor = null;

            switch (type) {
                case TYPE_ACC:
                    sensor = new AccelerationSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_BEACON:
                    sensor = new Beacon(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_EMG:
                    sensor = new EMGSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_TEMP:
                    sensor = new TemperatureHumiditySensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_MIC:
                    sensor = new Microphone(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_TOUCH:
                    sensor = new TouchSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_HID:
                    sensor = new HID(name, address, rssi, lastSeenMillis, null);
                    break;
            }
            sensor.setInterval(interval);
            sensor.setUUID(uuid);
            return sensor;
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };

    @Override
    public int hashCode() {
        return this.macAddress.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && this.getClass() == o.getClass()) {
            Sensor sensor = (Sensor) o;
            if (this.macAddress.equals(sensor.getMacAddress())) return true;
            return false;
        }
        return false;
    }

    @Override
    public int compareTo(Sensor another) {
        return another.isSelect() == this.isSelect() ? 0 : another.isSelect() ? 1 : -1;
    }

    public static Sensor createFromScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi) {
        byte[] serviceData = scanRecord.getServiceData(DKUUID.SERVICE_DATA_UUID);
        byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(76);

        if (serviceData != null && manufacturerSpecificData != null
                && serviceData.length == 11) {
            int type = Sensor.getTypeFromData(serviceData);
            if (type != 2) return null;

            String name = device.getName();
            String address = device.getAddress();

            long lastSeenMillis = System.currentTimeMillis();

            int category = Sensor.getCategoryFromData(serviceData);

            switch (category) {
                case Sensor.TYPE_BEACON:
                    return new Beacon(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case Sensor.TYPE_ACC:
                    return new AccelerationSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case Sensor.TYPE_EMG:
                    return new EMGSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case Sensor.TYPE_TEMP:
                    return new TemperatureHumiditySensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case Sensor.TYPE_MIC:
                    return new Microphone(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case Sensor.TYPE_TOUCH:
                    return new TouchSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case Sensor.TYPE_HID:
                    return new HID(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
            }
        }
        return null;
    }
}

