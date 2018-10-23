# PlutoconDK SDK

## Installation

### Gradle via jCenter
Declare in your Gradle's `build.gradle` dependency to this library.

```gradle
repositories {
	jcenter()
}

dependencies {
	compile 'com.kongtech.dk.sdk:plutocondk_sdk:1.2.2'
}
```


## Permissions

### Basic permissions
The following permissions are included in the sdk
  - 'android.permission.BLUETOOTH'
  - 'android.permission.BLUETOOTH_ADMIN'

### Android 6.0 runtime permissions
  - If running on Android 6.0 or later, Location Services must be turned on.
  - If running on Android 6.0 or later and your app is targeting SDK < 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION`) must be granted for <b>background</b> beacon detection.
  - If running on Android 6.0 or later and your app is targeting SDK >= 23 (M), any location permission (`ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` must be granted.

## Tutorials
### Quick start for monitoring plutoconDKs

````java
private PlutoconDKManager plutoconDKManager;

// Initialization
plutoconDKManager = new PlutoconDKManager(context);
plutoconDKManager.connectService(new PlutoconDKManager.OnReadyServiceListener(){
	@Override	
	public void onReady(){
		//do something
	}
});

// Set monitoring listener
plutoconDKManager.setOnMonitoringPlutoconDKListener(new OnMonitoringPlutoconDKListener(){
	@Override
	public void onPlutoconDKDiscovered(PlutoconDK plutoconDK, List<PlutoconDK> plutoconDKs) {
		//do something
                }
});

// Start monitoring foreground with listener
plutoconDKManager.startMonitoring(PlutoconDKManager.MONITORING_FOREGROUND);

// Start monitoring background
plutoconDKManager.startMonitoring(PlutoconDKManager.MONITORING_BACKGROUND);

// Stop Monitoring
plutoconDKManager.stopMonitoring();

// Disconnect from manager service.
plutoconDKManager.close();
````

### Quick start for connecting plutoconDK
````java
// Initialization
PlutoconDKConnection plutoconDKConnection = new PlutoconDKConnection(plutoconDK);

// Connect to plutoconDK
plutoconDKConnection.connect(new PlutoconDKConnection.OnConnectionStateChangeCallback() {
	@Override
	public void onConnectionStateDisconnected() {
		//do something;
	}

	@Override
	public void onConnectionStateConnected() {
		//do something;
	}
});

// Read plutoconDK property
plutoconDKConnection.getAdvertisingInterval();
plutoconDKConnection.getBatteryVoltage();
plutoconDKConnection.getBroadcastingPower();
plutoconDKConnection.getHardwareVersion();
plutoconDKConnection.getSoftwareVersion();
plutoconDKConnection.getUUID();

// Turn on sensor power(Require softwareversion A.1.2.0)
plutoconDKConnection.setPower(true);

// Turn off sensor power(Require softwareversion A.1.2.0)
plutoconDKConnection.setPower(false);

// Disconnect from plutoconDK
plutoconDKConnection.disconnect();
````

### Quick start for edit plutoconDK property
````java
PlutoconDKEditor editor = plutoconDKConnection.getSensorEditor();
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
