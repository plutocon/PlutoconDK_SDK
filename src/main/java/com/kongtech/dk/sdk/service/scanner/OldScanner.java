package com.kongtech.dk.sdk.service.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kongtech.dk.sdk.repackaged.ScanRecord;
import com.kongtech.dk.sdk.service.scanner.SensorScanner.ScannerCallback;
import com.kongtech.dk.sdk.utils.Plog;


public class OldScanner implements SensorScanner{

    private final BluetoothAdapter adapter;
    private final BluetoothAdapter.LeScanCallback leScanCallback;

    private ScannerCallback scannerCallback;
    private boolean isScanning;

    public OldScanner(Context context, ScannerCallback scannerCallback) {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.scannerCallback = scannerCallback;

        this.leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                OldScanner.this.scannerCallback.onLeScan(device, rssi, ScanRecord.parseFromBytes(scanRecord));
            }
        };
    }

    @Override
    public void start() {
        if(!this.isScanning) {
            this.adapter.startLeScan(this.leScanCallback);
            this.isScanning = true;
        } else
            Plog.i("Already Scan");
    }

    @Override
    public void stop() {
        if(this.isScanning) {
            this.isScanning = false;
            this.adapter.stopLeScan(this.leScanCallback);
        }
    }
}
