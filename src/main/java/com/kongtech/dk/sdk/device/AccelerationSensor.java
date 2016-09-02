package com.kongtech.dk.sdk.device;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.device.receiver.PlutoconDKDataReceiver;
import com.kongtech.dk.sdk.utils.DKUUID;

import java.util.Arrays;

public class AccelerationSensor extends PlutoconDK {

    private int accelerationX;
    private int accelerationY;
    private int accelerationZ;

    public AccelerationSensor(String name, String macAdress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAdress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    @Override
    public String getTypeString() {
        return PlutoconDK.TYPE_STRING_ACC;
    }

    @Override
    public int getType() {
        return PlutoconDK.TYPE_ACC;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        super.update(manufacturerSpecificData);
        if (manufacturerSpecificData == null) return;

        byte[] bytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 23);
        int[] acc = this.getAccelerationValue(bytes);

        accelerationX = acc[0];
        accelerationY = acc[1];
        accelerationZ = acc[2];
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return DKUUID.SENSOR_CHARACTERISTIC;
    }

    @Override
    public void dataReceiveCallback(byte[] data, PlutoconDKDataReceiver plutoconDKDataReceiver) {

        int[] acc = this.getAccelerationValue(data);

        accelerationX = acc[0]; accelerationY = acc[1]; accelerationZ = acc[2];

        if (plutoconDKDataReceiver != null) {
            AccelerationDataReceiver receiver = (AccelerationDataReceiver) plutoconDKDataReceiver;
            receiver.onAccelerationDataReceive(acc[0], acc[1], acc[2]);
        }
    }

    public int[] getAccelerationValue(byte[] acc_in) {
        int[] acc = new int[]{0, 0, 0};
        int[] ints = new int[]{acc_in[0] & 0xff, acc_in[1] & 0xff, acc_in[2] & 0xff, acc_in[3] & 0xff, acc_in[4] & 0xff};

        int base = (int) Math.pow(2, 21) - 1;

        if (((ints[0] >> 7) & 1) == 1) {
            acc[0] = base << 11;
        }
        acc[0] = acc[0] | ((ints[0] & 0b1111111) << 4) | (ints[1] >> 4);

        if (((ints[1] & 0b1000) >> 3) == 1) {
            acc[1] = base << 11;
        }
        acc[1] = acc[1] | ((ints[1] & 0b111)) << 8 | ints[2];

        if (((ints[3] >> 7) & 1) == 1) {
            acc[2] = base << 11;
        }
        acc[2] = acc[2] | ((ints[3] & 0b1111111) << 4) | (ints[4] >> 4);

        return acc;
    }

    public AccelerationSensor(Parcel source) {
        super(source);
        this.accelerationX = source.readInt();
        this.accelerationY = source.readInt();
        this.accelerationZ = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        super.writeToParcel(dest, flag);
        dest.writeInt(accelerationX);
        dest.writeInt(accelerationY);
        dest.writeInt(accelerationZ);
    }

    public static final Parcelable.Creator<AccelerationSensor> CREATOR = new Parcelable.Creator<AccelerationSensor>() {
        public AccelerationSensor createFromParcel(Parcel in) {
            return new AccelerationSensor(in);
        }

        public AccelerationSensor[] newArray(int size) {
            return new AccelerationSensor[size];
        }
    };

    public interface AccelerationDataReceiver extends PlutoconDKDataReceiver {
        void onAccelerationDataReceive(int x, int y, int z);
    }

    public int getAccelerationZ() {
        return accelerationZ;
    }

    public int getAccelerationY() {
        return accelerationY;
    }

    public int getAccelerationX() {
        return accelerationX;
    }
}
