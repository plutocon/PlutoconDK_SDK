package com.kongtech.dk.sdk.device;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.repackaged.ScanRecord;
import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public abstract class PlutoconDK implements Parcelable, Comparable<PlutoconDK> {

    public static final int TYPE_BEACON = 0;
    public static final int TYPE_ACC = 1;
    public static final int TYPE_EMG = 2;
    public static final int TYPE_TEMP = 3;
    public static final int TYPE_MIC = 4;
    public static final int TYPE_TOUCH = 5;
    public static final int TYPE_HID = 6;
    public static final int TYPE_HRM = 7;

    public static final int TYPE_LUX = 9;
    public static final int TYPE_RGB = 10;
    public static final int TYPE_AIR = 11;

    public static final String TYPE_STRING_TEMP = "DK_TEMP";
    public static final String TYPE_STRING_ACC = "DK_ACC";
    public static final String TYPE_STRING_BEACON = "DK_BEACON";
    public static final String TYPE_STRING_EMG = "DK_EMG";
    public static final String TYPE_STRING_MIC = "DK_MIC";
    public static final String TYPE_STRING_TOUCH = "DK_TOUCH";
    public static final String TYPE_STRING_HID = "DK_HID";
    public static final String TYPE_STRING_HRM = "DK_HRM";
    public static final String TYPE_STRING_LUX = "DK_LUX";
    public static final String TYPE_STRING_RGB = "DK_RGB";
    public static final String TYPE_STRING_AIR = "DK_AIR";


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

    public abstract void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver);

    public PlutoconDK(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        this.name = name;
        this.macAddress = macAddress;
        this.rssi = rssi;
        this.lastSeenMillis = lastSeenMillis;
        this.interval = 0;
        this.uuid = null;
        this.update(manufacturerSpecificData);
    }

    protected PlutoconDK(Parcel source) {
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

    public boolean isWritable() {
        return false;
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

    public static final Parcelable.Creator<PlutoconDK> CREATOR = new Creator<PlutoconDK>() {
        @Override
        public PlutoconDK createFromParcel(Parcel source) {
            int type = source.readInt();
            String name = source.readString();
            String address = source.readString();
            int rssi = source.readInt();
            long lastSeenMillis = source.readLong();
            int interval = source.readInt();
            ParcelUuid uuid = source.readParcelable(ParcelUuid.class.getClassLoader());
            PlutoconDK plutoconDK = null;

            switch (type) {
                case TYPE_ACC:
                    plutoconDK = new AccelerationSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_BEACON:
                    plutoconDK = new Beacon(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_EMG:
                    plutoconDK = new EMGSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_TEMP:
                    plutoconDK = new TemperatureHumiditySensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_MIC:
                    plutoconDK = new Microphone(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_TOUCH:
                    plutoconDK = new TouchSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_HID:
                    plutoconDK = new HID(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_HRM:
                    plutoconDK = new HRMSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_LUX:
                    plutoconDK = new LightUVSensor(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_RGB:
                    plutoconDK = new RGBLED(name, address, rssi, lastSeenMillis, null);
                    break;
                case TYPE_AIR:
                    plutoconDK = new AirQualitySensor(name, address, rssi, lastSeenMillis, null);
                    break;
            }
            plutoconDK.setInterval(interval);
            plutoconDK.setUUID(uuid);
            return plutoconDK;
        }

        @Override
        public PlutoconDK[] newArray(int size) {
            return new PlutoconDK[size];
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
            PlutoconDK plutoconDK = (PlutoconDK) o;
            if (this.macAddress.equals(plutoconDK.getMacAddress())) return true;
            return false;
        }
        return false;
    }

    @Override
    public int compareTo(PlutoconDK another) {
        return another.isSelect() == this.isSelect() ? 0 : another.isSelect() ? 1 : -1;
    }

    public static PlutoconDK createFromScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi) {
        byte[] serviceData = scanRecord.getServiceData(DKUUID.SERVICE_DATA_UUID);
        byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(76);

        if (serviceData != null && manufacturerSpecificData != null
                && serviceData.length == 11) {
            int type = PlutoconDK.getTypeFromData(serviceData);
            if (type != 2) return null;

            String name = device.getName();
            String address = device.getAddress();

            long lastSeenMillis = System.currentTimeMillis();

            int category = PlutoconDK.getCategoryFromData(serviceData);

            switch (category) {
                case PlutoconDK.TYPE_BEACON:
                    return new Beacon(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_ACC:
                    return new AccelerationSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_EMG:
                    return new EMGSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_TEMP:
                    return new TemperatureHumiditySensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_MIC:
                    return new Microphone(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_TOUCH:
                    return new TouchSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_HID:
                    return new HID(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_HRM:
                    return new HRMSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_LUX:
                    return new LightUVSensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_RGB:
                    return new RGBLED(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
                case PlutoconDK.TYPE_AIR:
                    return new AirQualitySensor(name, address, rssi, lastSeenMillis, manufacturerSpecificData);
            }
        }
        return null;
    }
}


