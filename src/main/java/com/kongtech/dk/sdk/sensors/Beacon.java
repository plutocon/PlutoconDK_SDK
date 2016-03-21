package com.kongtech.dk.sdk.sensors;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class Beacon extends Sensor {

    private int major;
    private int minor;

    public ParcelUuid uuid;

    public Beacon(String name, String macAdress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAdress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return Sensor.TYPE_STRING_BEACON;
    }

    @Override
    public int getType() {
        return Sensor.TYPE_BEACON;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {

        if(manufacturerSpecificData == null) return;

        byte[] uuidBytes = Arrays.copyOfRange(manufacturerSpecificData, 2, 18);
        byte[] majorBytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 20);
        byte[] minorBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
        ByteBuffer proximityUUIDBuffer = ByteBuffer.wrap(uuidBytes);

        this.uuid = new ParcelUuid(new UUID(proximityUUIDBuffer.getLong(), proximityUUIDBuffer.getLong()));
        this.major = ((majorBytes[0] & 0xff) << 8) | (majorBytes[1] & 0xff);
        this.minor = ((minorBytes[0] & 0xff) << 8) | (minorBytes[1] & 0xff);
    }

    public Beacon(Parcel source){
        super(source);
        this.major = source.readInt();
        this.minor = source.readInt();
        this.uuid = source.readParcelable(ParcelUuid.class.getClassLoader());
    }

    public int getMajor(){
        return major;
    }

    public int getMinor(){
        return minor;
    }

    public String getMajorString() {
        return major + "";
    }

    public String getMinorString() {
        return minor + "";
    }

    @Override
    public String getUuidString() {
        return uuid.toString();
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return null;
    }

    @Override
    public void dataReceiveCallback(byte[] data, SensorDataReceiver sensorDataReceiver) {

    }

    public void writeToParcel(Parcel dest, int flag){
        super.writeToParcel(dest, flag);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeParcelable(uuid, flag);
    }

    public static final Parcelable.Creator<Beacon> CREATOR = new Parcelable.Creator<Beacon>() {
        public Beacon createFromParcel(Parcel in) {
            return new Beacon(in);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };
}
