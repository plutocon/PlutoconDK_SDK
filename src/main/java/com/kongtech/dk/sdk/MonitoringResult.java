package com.kongtech.dk.sdk;

import com.kongtech.dk.sdk.device.PlutoconDK;

import java.util.ArrayList;
import java.util.List;

public class MonitoringResult {

    private List<PlutoconDK> plutoconDKs;

    public MonitoringResult(){
        plutoconDKs = new ArrayList<>();
    }

    public void updateSensor(PlutoconDK plutoconDK, int position){
        PlutoconDK s = plutoconDKs.get(position);
        plutoconDK.setInterval((int) (plutoconDK.getLastSeenMillis() - s.getLastSeenMillis()));
        plutoconDKs.set(position, plutoconDK);
    }

    public void addSensor(PlutoconDK plutoconDK){
        if(plutoconDKs.contains(plutoconDK)) return;
        plutoconDK.setInterval(0);
        plutoconDKs.add(plutoconDK);
    }

    public int isContained(PlutoconDK plutoconDK){
        return plutoconDKs.indexOf(plutoconDK);
    }

    public List<PlutoconDK> getList() {
        return plutoconDKs;
    }

    public void clear(){
        plutoconDKs.clear();
    }
}
