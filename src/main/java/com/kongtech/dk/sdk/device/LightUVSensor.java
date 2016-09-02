package com.kongtech.dk.sdk.device;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class LightUVSensor extends PlutoconDK {

    private int light;
    private int uv;

    public LightUVSensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return PlutoconDK.TYPE_STRING_LUX;
    }

    @Override
    public int getType() {
        return PlutoconDK.TYPE_LUX;
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return DKUUID.SENSOR_CHARACTERISTIC;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        super.update(manufacturerSpecificData);

        if(manufacturerSpecificData == null) return;

        byte[] uvBytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 20);
        byte[] lightBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);

        this.light = ((lightBytes[0] & 0xff) << 8) | (lightBytes[1] & 0xff);
        this.uv = ((uvBytes[0] & 0xff) << 8) | (uvBytes[1] & 0xff);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.light);
        dest.writeInt(this.uv);
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {
        LightUVDataReceiver lightUVDataReceiver = (LightUVDataReceiver) plutoconDKDataReceiver;

        this.uv = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
        this.light = ((data[2] & 0xff) << 8) | (data[3] & 0xff);

        if(lightUVDataReceiver != null)
            lightUVDataReceiver.LightUVDataReceive(light, uv);
    }

    public int getLight() {
        return light;
    }

    public int getUV() {
        return uv;
    }

    public LightUVSensor(Parcel source) {
        super(source);
        this.light = source.readInt();
        this.uv = source.readInt();
    }

    public static final Parcelable.Creator<LightUVSensor> CREATOR = new Creator<LightUVSensor>() {
        @Override
        public LightUVSensor createFromParcel(Parcel source) {
            return new LightUVSensor(source);
        }

        @Override
        public LightUVSensor[] newArray(int size) {
            return new LightUVSensor[size];
        }
    };

    public interface LightUVDataReceiver extends PlutoconDKDataReceiver {
        void LightUVDataReceive(int light, int uv);
    }
}

