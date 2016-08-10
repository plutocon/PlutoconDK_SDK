# PlutoconDK SDK#

##Installation
### Gradle via jCenter
Declare in your Gradle's `build.gradle` dependency to this library.
```gradle
repositories {
	jcenter()
}

dependencies {
	compile 'com.kongtech.dk.sdk:plutocondk_sdk:1.1.0'
}
```


##Permissions
### Basic permissions
The following permissions are included in the sdk
  - 'android.permission.BLUETOOTH'
  - 'android.permission.BLUETOOTH_ADMIN'

### Android 6.0 runtime permissions
  - If running on Android 6.0 or later, Location Services must be turned on.
  - If running on Android 6.0 or later and your app is targeting SDK < 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION`) must be granted for <b>background</b> beacon detection.
  - If running on Android 6.0 or later and your app is targeting SDK >= 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` must be granted.

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

// Set monitoring listener
sensorManager.setOnMonitoringSensorListener(new SensorManager.OnMonitoringSensorListener() {
	@Override 
	public void onSensorDiscovered(Sensor sensor, List<Sensor> sensors) {
		//do somethings
	}
});

// Start monitoring foreground with listener
sensorManager.startMonitoring(SensorManager.MONITORING_FOREGROUND);

// Start monitoring background
sensorManager.startMonitoring(SensorManager.MONITORING_BACKGROUND);

// Stop Monitoring
sensorManager.stopMonitoring();

// Disconnect from manager service.
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

// Disconnect from sensor
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
