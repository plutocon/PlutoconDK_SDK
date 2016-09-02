package com.kongtech.dk.sdk.device;


import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class Microphone extends PlutoconDK {

    private int value;

    public Microphone(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING_MIC;
    }

    @Override
    public int getType() {
        return TYPE_MIC;
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
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {
        MicDataReceiver receiver = (MicDataReceiver) plutoconDKDataReceiver;

        this.value = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
        if(receiver != null){
            receiver.MicDataReceive(value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.value);
    }

    public int getValue() {
        return value;
    }

    public Microphone(Parcel source){
        super(source);
        this.value = source.readInt();
    }

    public static final Parcelable.Creator<Microphone> CREATOR = new Creator<Microphone>() {
        @Override
        public Microphone createFromParcel(Parcel source) {
            return new Microphone(source);
        }

        @Override
        public Microphone[] newArray(int size) {
            return new Microphone[size];
        }
    };

    public interface MicDataReceiver extends PlutoconDKDataReceiver {
        void MicDataReceive(int value);
    }
}
