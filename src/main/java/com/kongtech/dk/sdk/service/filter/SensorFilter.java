package com.kongtech.dk.sdk.service.filter;

import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;

import com.kongtech.dk.sdk.utils.PlutoconDKUUID;

import java.util.ArrayList;
import java.util.List;

public class SensorFilter {

    private SensorFilter(){

    }

    public static List<ScanFilter>  createDefaultFilter(){
        List<ScanFilter> filter = new ArrayList<>();
        filter.add(new ScanFilter.Builder().setServiceData(PlutoconDKUUID.SERVICE_DATA_UUID,new byte[11], new byte[]{0,0,0,0,0,0,0,0,0,0,0}).build());
        return filter;
    }
}
