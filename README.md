# PlutoconDK SDK#

##Permissions

### Android 6.0 runtime permissions

## Tutorials
### Quick start for monitoring sensors
````java
private SensorManager sensorManager;

// Initialization
sensorManager = new SensorManager(context);
sensorManager.connectService(new SensorManager.OnReadyServiceListener() {
	@Override
	public void onReady() {
		//do something        
	} 
});

// Registration Listener
sensorManager.setOnMonitoringSensorListener(new SensorManager.OnMonitoringSensorListener() {
	@Override 
	public void onSensorDiscovered(Sensor sensor, List<Sensor> sensors) {
		//do somethings
	}
});

// Start monitoring foreground
sensorManager.startMonitoring(SensorManager.MONITORING_FOREGROUND);

// Start monitoring background
sensorManager.startMonitoring(SensorManager.MONITORING_BACKGROUND);

// Stop Monitoring
sensorManager.stopMonitoring();

// Disconnect to manager service.
sensorManager.close();
```

### Quick start for connecting sensor
````java
// Initialization
SensorConnection sensorConnection = new SensorConnection(sensor);

// Connect to sensor
sensorConnection.connect(new SensorConnection.OnConnectionStateChangeCallback() {
	@Override
	public void onConnectionStateDisconnected() {
		//do something;
	}

	@Override
	public void onConnectionStateConnected() {
		//do something;
	}
});

// Read sensor property
sensorConnection.getAdvertisingInterval();
sensorConnection.getBatteryVoltage();
sensorConnection.getBroadcastingPower();
sensorConnection.getHardwareVersion();
sensorConnection.getSoftwareVersion();
sensorConnection.getUUID();

// Disconnect to sensor
sensorConnection.disconnect();
````
### Quick start for edit sensor property
````java
SensorEditor editor = sensorConnection.getSensorEditor();
editor.setProperty(DKUUID.ADV_INTERVAL_CHARACTERISTIC, interval)
      .setUUID(ParcelUuid.fromString("1f4ae6a0-0037-4020-4101-271071580001"))
      .setOnEditCompleteCallback(new SensorEditor.OnEditCompleteCallback() {
      	@Override
	public void onEditCompleteCallback() {
                        // do something
	}
});
editor.commit();

````
