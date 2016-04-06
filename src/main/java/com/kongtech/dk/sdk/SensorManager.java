package com.kongtech.dk.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kongtech.dk.sdk.sensors.Sensor;
import com.kongtech.dk.sdk.service.MessageUtil;
import com.kongtech.dk.sdk.service.scanner.LollipopScanner;
import com.kongtech.dk.sdk.service.SensorService;
import com.kongtech.dk.sdk.utils.Plog;

import java.util.List;

public class SensorManager {

    public final static int MONITORING_BACKGROUND = LollipopScanner.SCAN_BACKGROUND;
    public final static int MONITORING_FOREGROUND = LollipopScanner.SCAN_FOREGROUND;
    private Context context;

    private SensorManager.SensorServiceConnection serviceConnection;
    private Messenger messengerService;
    private Messenger responseMessenger;

    private OnReadyServiceListener onReadyService;


    private OnMonitoringSensorListener onMonitoringSensorListener;

    private MonitoringResult monitoringResult;

    public SensorManager(Context context) {
        this.context = context;
        this.serviceConnection = new SensorServiceConnection();
        this.messengerService = null;

        HandlerThread thread = new HandlerThread("Messenger Thread");
        thread.start();
        this.responseMessenger = new Messenger(new ResponseHandler(thread.getLooper()));

        this.onReadyService = null;

        this.monitoringResult = new MonitoringResult();
    }

    public void close() {
        if (this.isServiceConnected()) {
            this.stopMonitoring();
            context.unbindService(this.serviceConnection);
        }
    }

    public void startMonitoring(int mode) {
        Message scanModeMsg = null;
        Message scanStartMsg = Message.obtain(null, MessageUtil.REQUEST_SCAN_START);

        monitoringResult.clear();
        scanStartMsg.replyTo = responseMessenger;

        if (mode == LollipopScanner.SCAN_FOREGROUND)
            scanModeMsg = Message.obtain(null, MessageUtil.REQUEST_MODE_FOREGROUND);
        else scanModeMsg = Message.obtain(null, MessageUtil.REQUEST_MODE_BACKGROUND);

        try {
            if (messengerService != null) {
                this.messengerService.send(scanModeMsg);
                this.messengerService.send(scanStartMsg);
            }
        } catch (RemoteException e) {
            Plog.e("Error Start Monitoring");
        }
    }

    public void stopMonitoring() {
        Message scanStopMsg = Message.obtain(null, MessageUtil.REQUEST_SCAN_STOP);
        try {
            if (messengerService != null)
                this.messengerService.send(scanStopMsg);
        } catch (RemoteException e) {
            Plog.e("Error Stop Monitoring");
        }
    }

    public boolean isServiceConnected() {
        return messengerService != null;
    }

    public boolean connectService(OnReadyServiceListener onReadyService) {
        if (isServiceConnected()) {
            if(onReadyService != null) onReadyService.onReady();
            return true;
        }
        this.onReadyService = onReadyService;
        return this.context.bindService(new Intent(this.context, SensorService.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setOnMonitoringSensorListener(OnMonitoringSensorListener onMonitoringSensorListener) {
        this.onMonitoringSensorListener = onMonitoringSensorListener;
    }

    private class ResponseHandler extends Handler {
        private ResponseHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageUtil.RESPONSE_SCAN_RESULT:
                    msg.getData().setClassLoader(Sensor.class.getClassLoader());
                    Sensor sensor = msg.getData().getParcelable(MessageUtil.SCAN_RESULT);

                    int p = SensorManager.this.monitoringResult.isContained(sensor);

                    if (p > -1) {
                        SensorManager.this.monitoringResult.updateSensor(sensor, p);
                    } else {
                        SensorManager.this.monitoringResult.addSensor(sensor);
                    }

                    if (onMonitoringSensorListener != null) {
                        onMonitoringSensorListener.onSensorDiscovered(sensor, monitoringResult.getList());
                    }

                    break;
            }
        }
    }

    private class SensorServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorManager.this.messengerService = new Messenger(service);

            if (onReadyService != null) {
                onReadyService.onReady();
                onReadyService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            SensorManager.this.messengerService = null;
        }
    }

    public interface OnReadyServiceListener {
        void onReady();
    }

    public interface OnMonitoringSensorListener {
        void onSensorDiscovered(Sensor sensor, List<Sensor> sensors);
    }
}
