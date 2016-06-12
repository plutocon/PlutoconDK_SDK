package com.kongtech.dk.sdk.connection;

import android.content.Context;

import com.kongtech.dk.sdk.sensors.Sensor;

public class HIDConnection extends SensorConnection {

    public HIDConnection(Context context, Sensor sensor) {
        super(context, sensor);
    }

    public HIDConnection(Context context, String address, int type) {
        super(context, address, type);
    }


}
