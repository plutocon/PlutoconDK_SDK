package com.kongtech.dk.sdk.service.scanner;

import android.bluetooth.BluetoothDevice;

import com.kongtech.dk.sdk.repackaged.ScanRecord;

public interface PlutoconDKScanner {
    void start();
    void stop();

    interface ScannerCallback {
        void onLeScan(BluetoothDevice device, int rssi, ScanRecord scanRecord);
    }
}
