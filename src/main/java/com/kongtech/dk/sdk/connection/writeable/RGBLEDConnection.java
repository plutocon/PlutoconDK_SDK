package com.kongtech.dk.sdk.connection.writeable;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.kongtech.dk.sdk.device.PlutoconDK;

public class RGBLEDConnection extends WritableDKConnection {

    public RGBLEDConnection(Context context, PlutoconDK plutoconDK) {
        super(context, plutoconDK);
    }

    public RGBLEDConnection(Context context, String address, int type) {
        super(context, address, type);
    }

    public synchronized void setColor(int red, int green, int blue) {
        byte[] bytes = new byte[]{(byte)(red & 0xff), (byte)(green & 0xff), (byte)(blue & 0xff)};
        write(bytes);
    }

    public synchronized void setColor(int color){
        byte[] bytes = new byte[3];
        bytes[0] = (byte) (Color.red(color) & 0xff);
        bytes[1] = (byte) (Color.green(color) & 0xff);
        bytes[2] = (byte) (Color.blue(color) & 0xff);
        write(bytes);
    }
}
