package com.kongtech.dk.sdk.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlutoconDKReader {

    private ConcurrentHashMap<ParcelUuid, BluetoothGattCharacteristic> characteristics;
    private List<BluetoothGattCharacteristic> readList;

    private BluetoothGatt sensorGatt;

    private OnReadCompleteCallback onReadCompleteCallback;

    public PlutoconDKReader(BluetoothGatt sensorGatt){
        this.sensorGatt = sensorGatt;
        this.readList = new ArrayList<>();
        this.characteristics = new ConcurrentHashMap<>();

        List<BluetoothGattService> sensorServices = sensorGatt.getServices();
        for(BluetoothGattService service : sensorServices){
            for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                if((characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ)) != 0)
                    characteristics.put(new ParcelUuid(characteristic.getUuid()), characteristic);
            }
        }
    }

    public PlutoconDKReader getProperty(ParcelUuid uuid){
        BluetoothGattCharacteristic characteristic = characteristics.get(uuid);

        if(characteristic == null) return this;

        readList.add(characteristic);
        return this;
    }

    public void commit(){
        executeNext();
    }

    public boolean executeNext(){
        if(readList.size() > 0 ){
            BluetoothGattCharacteristic characteristic = readList.get(0);
            readList.remove(0);
            sensorGatt.readCharacteristic(characteristic);
            return true;
        }
        return false;
    }

    public PlutoconDKReader setOnReadCompleteCallback(OnReadCompleteCallback onReadCompleteCallback) {
        this.onReadCompleteCallback = onReadCompleteCallback;
        return this;
    }

    public void onReadComplete(){
        if(onReadCompleteCallback != null)
            onReadCompleteCallback.onReadComplete();
    }

    public interface OnReadCompleteCallback{
        void onReadComplete();
    }
}
