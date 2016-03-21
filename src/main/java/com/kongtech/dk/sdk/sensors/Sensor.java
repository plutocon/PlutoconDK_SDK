package com.kongtech.dk.sdk.sensors;

import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;
import com.kongtech.dk.sdk.utils.PlutoconDKUUID;

import java.util.Arrays;

public abstract class Sensor implements Parcelable, Comparable<Sensor> {

    public static final int TYPE_BEACON = 0;
    public static final int TYPE_ACC = 1;
    public static final int TYPE_EMG = 2;
    public static final int TYPE_TEMP = 3;

    public static final String TYPE_STRING_TEMP = "DK_TEMP";
    public static final String TYPE_STRING_ACC = "DK_ACC";
    public static final String TYPE_STRING_BEACON = "DK_BEACON";
    public static final String TYPE_STRING_EMG = "DK_EMG";

    private String name;
    private String macAddress;
    private int rssi;
    private long lastSeenMillis;

    private int interval;
    private boolean isSelect;

    public abstract String getTypeString();

    public abstract int getType();

    public abstract void update(byte[] manufacturerSpecificData);

    public abstract String getMajorString();

    public abstract String getMinorString();

    public abstract String getUuidString();

    public abstract ParcelUuid getNotificationUUID();

    public abstract void dataReceiveCallback(byte[] data, SensorDataReceiver sensorDataReceiver);

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getType());
        dest.writeString(name);
        dest.writeString(macAddress);
        dest.writeInt(rssi);
        dest.writeLong(lastSeenMillis);
        dest.writeInt(interval);
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
            }
            sensor.setInterval(interval);
            return sensor;
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };

    public Sensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        this.name = name;
        this.macAddress = macAddress;
        this.rssi = rssi;
        this.lastSeenMillis = lastSeenMillis;
        this.update(manufacturerSpecificData);
        this.interval = 0;
    }

    protected Sensor(Parcel source) {
        int type = source.readInt();
        this.name = source.readString();
        this.macAddress = source.readString();
        this.rssi = source.readInt();
        this.lastSeenMillis = source.readLong();
        this.interval = source.readInt();
    }

    public String getName() {
        return name;
    }

    public void setSensorName(String sensorName) {
        this.name = sensorName;
    }

    public int getRssi() {
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
        return Boolean.compare(another.isSelect(), this.isSelect());
    }

    public static Sensor createFromScanResult(ScanResult scanResult) {
        byte[] serviceData = scanResult.getScanRecord().getServiceData(PlutoconDKUUID.SERVICE_DATA_UUID);
        byte[] manufacturerSpecificData = scanResult.getScanRecord().getManufacturerSpecificData(76);

        if (serviceData != null && manufacturerSpecificData != null) {
            int type = Sensor.getTypeFromData(serviceData);
            if (type != 2) return null;

            String name = scanResult.getDevice().getName();
            String address = scanResult.getDevice().getAddress();

            int rssi = scanResult.getRssi();
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
            }
        }
        return null;
    }

    private static int getCategoryFromData(byte[] serviceData) {
        return serviceData[1];
    }

    private static int getTypeFromData(byte[] serviceData) {
        return serviceData[0];
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void select(boolean select) {
        this.isSelect = select;
    }
}

