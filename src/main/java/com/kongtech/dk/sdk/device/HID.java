package com.kongtech.dk.sdk.device;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;

public class HID extends PlutoconDK {

    public HID(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    protected HID(Parcel source) {
        super(source);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING_HID;
    }

    @Override
    public int getType() {
        return TYPE_HID;
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return null;
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {

    }

    public static final Parcelable.Creator<HID> CREATOR = new Creator<HID>() {
        @Override
        public HID createFromParcel(Parcel source) {
            return new HID(source);
        }

        @Override
        public HID[] newArray(int size) {
            return new HID[size];
        }
    };
}
