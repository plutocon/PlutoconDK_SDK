package com.kongtech.dk.sdk.sensors;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.kongtech.dk.sdk.sensors.receiver.SensorDataReceiver;
import com.kongtech.dk.sdk.utils.PlutoconDKUUID;

import java.util.Arrays;

public class TemperatureHumiditySensor extends Sensor {
    private float temperature;
    private float humidity;

    public TemperatureHumiditySensor(String name, String macAdress, int rssi, long lastSeenMillis, byte[] manufacturerSpecificData) {
        super(name, macAdress, rssi, lastSeenMillis, manufacturerSpecificData);
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    @Override
    public String getTypeString() {
        return Sensor.TYPE_STRING_TEMP;
    }

    @Override
    public int getType() {
        return Sensor.TYPE_TEMP;
    }

    @Override
    public void update(byte[] manufacturerSpecificData) {
        if(manufacturerSpecificData == null) return;
        byte[] humidityBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
        byte[] temperatureBytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 20);

        int hum = ((humidityBytes[0] & 0xff) << 8) | (humidityBytes[1] & 0xff);
        int temp = ((temperatureBytes[0] & 0xff) << 8) | (temperatureBytes[1] & 0xff);
        this.humidity = (((float)hum) / 65536.0f) * 100.0f;
        this.temperature = (((float)temp) / 65536.0f) * 165.0f - 40.0f;
    }

    @Override
    public String getMajorString() {
        return "온도";
    }

    @Override
    public String getMinorString() {
        return "습도";
    }

    @Override
    public String getUuidString() {
        return "온습 uuid";
    }

    @Override
    public ParcelUuid getNotificationUUID() {
        return PlutoconDKUUID.SENSOR_CHARACTERISTIC;
    }

    @Override
    public void dataReceiveCallback(byte[] data, SensorDataReceiver sensorDataReceiver) {
        TemperatureHumidityDataReceiver receiver = (TemperatureHumidityDataReceiver)sensorDataReceiver;

        int hum = ((data[2] & 0xff) << 8) | (data[3] & 0xff);
        int temp = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
        this.humidity = (((float)hum) / 65536.0f) * 100.0f;
        this.temperature = (((float)temp) / 65536.0f) * 165.0f - 40.0f;

        if(receiver != null){
            receiver.TemperatureHumidityDataReceive(this.temperature, this.humidity);
        }
    }

    public TemperatureHumiditySensor(Parcel source){
        super(source);
        this.temperature = source.readFloat();
        this.humidity = source.readFloat();
    }

    public void writeToParcel(Parcel dest, int flag){
        super.writeToParcel(dest, flag);
        dest.writeFloat(this.temperature);
        dest.writeFloat(this.humidity);
    }

    public static final Parcelable.Creator<TemperatureHumiditySensor> CREATOR = new Parcelable.Creator<TemperatureHumiditySensor>() {
        public TemperatureHumiditySensor createFromParcel(Parcel in) {
            return new TemperatureHumiditySensor(in);
        }
        public TemperatureHumiditySensor[] newArray(int size) {
            return new TemperatureHumiditySensor[size];
        }
    };

    public interface TemperatureHumidityDataReceiver extends SensorDataReceiver {
        void TemperatureHumidityDataReceive(float temperature, float humidity);
    }
}
