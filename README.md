# Cordova plugin for Android
- Receive event from Bluetooth Headset

# Plugin install
 - Clone this repo into your project
 - Edit package.json file, add ```cordova-bluetooth-headset-event-receiver: "file:BluetoothHeadsetEventReceiver"```
 - Then run ```ionic cordova plugin add BluetoothHeadsetEventReceiver```

# Usage
 ```js 
declare var cordova: any; 
...
var success = function (result) {
   console.log("success:", result);
}

var failure = function (result) {
   console.log("failure:", result);
}

cordova.plugins.BluetoothHeadsetEventReceiver.registerReceiver(success, failure);
```