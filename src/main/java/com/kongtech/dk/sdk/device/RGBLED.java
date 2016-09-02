package com.kongtech.dk.sdk.device;

import android.graphics.Color;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;

import java.util.Arrays;

public class RGBLED extends PlutoconDK {

    private int color;

    public RGBLED(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING_RGB;
    }

    @Override
    public int getType() {
        return TYPE_RGB;
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return null;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        super.update(manufacturerSpecificData);
        if (manufacturerSpecificData == null) return;

        byte[] colorBytes = Arrays.copyOfRange(manufacturerSpecificData, 19, 22);

        int r, g, b;

        r = colorBytes[0] & 0xff;
        g = colorBytes[1] & 0xff;
        b = colorBytes[2] & 0xff;

        this.color = Color.argb(255, r, g, b);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.color);
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {

    }

    public int getColor() {
        return color;
    }

    public int getRed() {
        return Color.red(color);
    }

    public int getGreen() {
        return Color.green(color);
    }

    public int getBlue() {
        return Color.blue(color);
    }

    public RGBLED(Parcel source) {
        super(source);
        this.color = source.readInt();
    }

    public static final Parcelable.Creator<RGBLED> CREATOR = new Creator<RGBLED>() {
        @Override
        public RGBLED createFromParcel(Parcel source) {
            return new RGBLED(source);
        }

        @Override
        public RGBLED[] newArray(int size) {
            return new RGBLED[size];
        }
    };
}
