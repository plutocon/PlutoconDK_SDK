package com.kongtech.dk.sdk.device;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class HRMSensor extends PlutoconDK {

    private int value;

    public HRMSensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return PlutoconDK.TYPE_STRING_HRM;
    }

    @Override
    public int getType() {
        return PlutoconDK.TYPE_HRM;
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.value);
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {
        HRMDataReceiver hrmDataReceiver = (HRMDataReceiver) plutoconDKDataReceiver;

        this.value = data[0] & 0xff;
        if(hrmDataReceiver != null){
            hrmDataReceiver.HRMDataReceive(value);
        }
    }

    public int getValue(){
        return value;
    }

    public HRMSensor(Parcel source) {
        super(source);
        this.value = source.readInt();
    }

    public static final Parcelable.Creator<HRMSensor> CREATOR = new Creator<HRMSensor>() {
        @Override
        public HRMSensor createFromParcel(Parcel source) {
            return new HRMSensor(source);
        }

        @Override
        public HRMSensor[] newArray(int size) {
            return new HRMSensor[size];
        }
    };

    public interface HRMDataReceiver extends PlutoconDKDataReceiver {
        void HRMDataReceive(int hrm);
    }
}
