package com.kongtech.dk.sdk.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import com.kongtech.dk.sdk.service.filter.SensorFilter;
import com.kongtech.dk.sdk.utils.Plog;

import java.util.ArrayList;
import java.util.List;

public class SensorScanner {

    public static final int SCAN_FOREGROUND = ScanSettings.SCAN_MODE_LOW_LATENCY;
    public static final int SCAN_BACKGROUND = ScanSettings.SCAN_MODE_LOW_POWER;

    private BluetoothAdapter adapter;
    private BluetoothLeScanner leScanner;

    private ScanSettings.Builder setting;
    private List<ScanFilter> filters = new ArrayList<>();
    private ScanCallback leScanCallback = null;

    private boolean batchMode;
    private boolean isScanning;

    public SensorScanner(Context context, int scanMode) {

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.leScanner = adapter.getBluetoothLeScanner();

        this.batchMode = this.adapter.isOffloadedScanBatchingSupported();

        this.setting = new ScanSettings.Builder();
        this.setting.setScanMode(scanMode);

        if (Build.VERSION.SDK_INT >= 23)
            this.setting.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

        filters.clear();
        filters.addAll(SensorFilter.createDefaultFilter());
    }

    public void start(ScanCallback callback) {
        if (!this.isScanning) {
            this.leScanCallback = callback;
            this.leScanner.startScan(this.filters, this.setting.build(), this.leScanCallback);
            this.isScanning = true;
        } else
            Plog.i("Already Scan");
    }

    public void stop() {
        if (this.isScanning) {
            this.isScanning = false;
            this.leScanner.flushPendingScanResults(this.leScanCallback);
            this.leScanner.stopScan(this.leScanCallback);
        }
    }

    public void setScanMode(int mode) {
        this.setting.setScanMode(mode);
    }

    public boolean isBatchMode() {
        return this.batchMode;
    }
}
