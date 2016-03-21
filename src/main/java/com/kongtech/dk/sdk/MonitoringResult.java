package com.kongtech.dk.sdk;

import com.kongtech.dk.sdk.sensors.Sensor;

import java.util.ArrayList;
import java.util.List;

public class MonitoringResult {

    private List<Sensor> sensors;

    public MonitoringResult(){
        sensors = new ArrayList<>();
    }

    public void updateSensor(Sensor sensor,int position){
        Sensor s = sensors.get(position);
        sensor.setInterval((int) (sensor.getLastSeenMillis() - s.getLastSeenMillis()));
        sensors.set(position, sensor);
    }

    public void addSensor(Sensor sensor){
        if(sensors.contains(sensor)) return;
        sensor.setInterval(0);
        sensors.add(sensor);
    }

    public int isContained(Sensor sensor){
        return sensors.indexOf(sensor);
    }

    public List<Sensor> getList() {
        return sensors;
    }

    public void clear(){
        sensors.clear();
    }
}
