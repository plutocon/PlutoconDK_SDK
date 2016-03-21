package com.kongtech.dk.sdk.service;

import android.app.Service;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kongtech.dk.sdk.sensors.Sensor;
import com.kongtech.dk.sdk.utils.Plog;

import java.util.List;

public class SensorService extends Service {

    private RequestHandler requestHandler = null;
    private Messenger messengerService = null;
    private SensorScanner sensorScanner;

    private Messenger responseMessenger;

    public SensorService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("Messenger Thread");
        thread.start();

        requestHandler = new RequestHandler(thread.getLooper());
        messengerService = new Messenger(requestHandler);

        sensorScanner = new SensorScanner(this.getApplicationContext(), SensorScanner.SCAN_FOREGROUND);
    }

    private void sendScanResult(Sensor sensor){
        if(this.responseMessenger == null) return;

        Message scanResultMsg = Message.obtain(null, MessageUtil.RESPONSE_SCAN_RESULT);
        scanResultMsg.getData().putParcelable(MessageUtil.SCAN_RESULT, sensor);

        try {
            this.responseMessenger.send(scanResultMsg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestHandler.getLooper().quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messengerService.getBinder();
    }

    private class RequestHandler extends Handler {
        private RequestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            final Messenger replyTo = msg.replyTo;
            responseMessenger = replyTo;

            switch (msg.what) {
                case MessageUtil.REQUEST_SCAN_START:
                    sensorScanner.start(scanCallback);
                    break;
                case MessageUtil.REQUEST_SCAN_STOP:
                    sensorScanner.stop();
                    break;
                case MessageUtil.REQUEST_MODE_BACKGROUND:
                    sensorScanner.setScanMode(SensorScanner.SCAN_BACKGROUND);
                    break;
                case MessageUtil.REQUEST_MODE_FOREGROUND:
                    sensorScanner.setScanMode(SensorScanner.SCAN_FOREGROUND);
                    break;
            }
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Plog.i("ScanResult: " + result.toString());

            Sensor sensor = Sensor.createFromScanResult(result);
            if(sensor != null) SensorService.this.sendScanResult(sensor);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Plog.i("ScanResults: " + results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Plog.e("ScanError: " + errorCode);
        }
    };
}
