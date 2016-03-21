package com.kongtech.dk.sdk.connection;

import android.bluetooth.BluetoothGatt;

public interface SensorOperation {
    void execute(BluetoothGatt bluetoothGatt);
}
