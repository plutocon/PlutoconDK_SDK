package com.kongtech.dk.sdk.sensors;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;

import java.util.Arrays;

public class Beacon extends Sensor {

    private int major;
    private int minor;

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
        super.update(manufacturerSpecificData);
        if(manufacturerSpecificData == null) return;

        byte[] majorBytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 20);
        byte[] minorBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
        this.major = ((majorBytes[0] & 0xff) << 8) | (majorBytes[1] & 0xff);
        this.minor = ((minorBytes[0] & 0xff) << 8) | (minorBytes[1] & 0xff);
    }

    public Beacon(Parcel source){
        super(source);
        this.major = source.readInt();
        this.minor = source.readInt();
    }

    public int getMajor(){
        return major;
    }

    public int getMinor(){
        return minor;
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
