package com.kongtech.dk.sdk.sensors;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class EMGSensor extends Sensor{

    private int value;

    public EMGSensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return Sensor.TYPE_STRING_EMG;
    }

    @Override
    public int getType() {
        return Sensor.TYPE_EMG;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        super.update(manufacturerSpecificData);
        if(manufacturerSpecificData == null) return;
        byte[] valueBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
        this.value = ((valueBytes[0] & 0xff) << 8) | (valueBytes[1] & 0xff);
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return DKUUID.SENSOR_CHARACTERISTIC;
    }

    @Override
    public void dataReceiveCallback(byte[] data, SensorDataReceiver sensorDataReceiver) {
        EMGDataReceiver receiver = (EMGDataReceiver)sensorDataReceiver;

        this.value = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
        if(receiver != null){
            receiver.EMGDataReceive(value, adcToVoltage(value));
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.value);
    }

    public static float adcToVoltage(int adcValue){
        return (float)adcValue / 1024f * 3.3f;
    }

    public int getValue() {
        return value;
    }

    public EMGSensor(Parcel source){
        super(source);
        this.value = source.readInt();
    }

    public static final Parcelable.Creator<EMGSensor> CREATOR = new Creator<EMGSensor>() {
        @Override
        public EMGSensor createFromParcel(Parcel source) {
            return new EMGSensor(source);
        }

        @Override
        public EMGSensor[] newArray(int size) {
            return new EMGSensor[size];
        }
    };

    public interface EMGDataReceiver extends SensorDataReceiver {
        void EMGDataReceive(int adcValue, float voltage);
    }
}
