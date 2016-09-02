package com.kongtech.dk.sdk.device;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class AirQualitySensor extends PlutoconDK{

    private int value;

    public AirQualitySensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING_AIR;
    }

    @Override
    public int getType() {
        return TYPE_AIR;
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return DKUUID.SENSOR_CHARACTERISTIC;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        super.update(manufacturerSpecificData);
        if(manufacturerSpecificData == null) return;

        byte[] valueBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);

        this.value = ((valueBytes[0] & 0xff) << 8) | (valueBytes[1] & 0xff);
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {
        AirQualityDataReceiver airQualityDataReceiver = (AirQualityDataReceiver) plutoconDKDataReceiver;

        this.value = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
        if(airQualityDataReceiver != null){
            airQualityDataReceiver.AirQualityDataReceive(value);
        }

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(value);
    }

    public int getValue() {
        return value;
    }

    public AirQualitySensor(Parcel source) {
        super(source);
        value = source.readInt();
    }

    public static final Parcelable.Creator<AirQualitySensor> CREATOR = new Creator<AirQualitySensor>() {
        @Override
        public AirQualitySensor createFromParcel(Parcel source) {
            return new AirQualitySensor(source);
        }

        @Override
        public AirQualitySensor[] newArray(int size) {
            return new AirQualitySensor[size];
        }
    };

    public interface AirQualityDataReceiver extends PlutoconDKDataReceiver {
        void AirQualityDataReceive(int value);
    }
}
