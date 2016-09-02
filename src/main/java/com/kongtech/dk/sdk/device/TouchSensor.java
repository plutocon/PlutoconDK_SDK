package com.kongtech.dk.sdk.device;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class TouchSensor extends PlutoconDK {

    private int values[];

    public TouchSensor(String name, String macAddress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAddress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING_TOUCH;
    }

    @Override
    public int getType() {
        return TYPE_TOUCH;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        super.update(manufacturerSpecificData);
        if(values == null) values = new int[9];

        if(manufacturerSpecificData == null) return;
        byte[] valueBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
        int value = ((valueBytes[0] & 0xff) << 8) | (valueBytes[1] & 0xff);

        for(int i = 0 ; i < 9 ; i ++){
            values[i] = value & 0x01;
            value = value >> 1;
        }
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return DKUUID.SENSOR_CHARACTERISTIC;
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {
        TouchDataReceiver receiver = (TouchDataReceiver) plutoconDKDataReceiver;

        for(int i = 0 ; i <9;i++){
            values[i] = ((data[i*2] & 0xff) << 8) | (data[i*2+1] & 0xff);
        }
        if(receiver != null){
            receiver.TouchDataReceive(values);
        }
    }

    public int[] getValue() {
        return values;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeIntArray(this.values);
    }

    public TouchSensor(Parcel source) {
        super(source);
        if(values == null) values = new int[9];
        source.readIntArray(values);
    }

    public static final Parcelable.Creator<TouchSensor> CREATOR = new Creator<TouchSensor>() {
        @Override
        public TouchSensor createFromParcel(Parcel source) {
            return new TouchSensor(source);
        }

        @Override
        public TouchSensor[] newArray(int size) {
            return new TouchSensor[size];
        }
    };

    public interface TouchDataReceiver extends PlutoconDKDataReceiver {
        void TouchDataReceive(int[] value);
    }


}
