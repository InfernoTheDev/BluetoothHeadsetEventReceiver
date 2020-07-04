package cordova-bluetooth-headset-event-receiver;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.companion.BluetoothDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class echoes a string called from JavaScript.
 */
public class BluetoothHeadsetEventReceiver extends CordovaPlugin {

    CallbackContext mCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("registerReceiver")) {
            this.mCallbackContext = callbackContext;
            setBroadcastReceiver();
            callbackContext.success(true);
            return true;
        }
        return false;
    }

    public void setBroadcastReceiver() {
        if (this.broadcastReceiver != null){
            this.unregisterReceiver(this.broadcastReceiver);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY+"."+ BluetoothAssignedNumbers.PLANTRONICS);
        intentFilter.addAction(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE);
        intentFilter.addAction(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
        intentFilter.addAction(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS);
        intentFilter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    public JSONObject getDevicesInfo(String deviceAddress){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice pairedDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);

        Log.d(TAG, "pairedDevices getName: " + pairedDevice.getName());
        Log.d(TAG, "pairedDevices getAddress: " + pairedDevice.getAddress());
        Log.d(TAG, "pairedDevices getBondState: " + pairedDevice.getBondState());
        Log.d(TAG, "pairedDevices getType: " + pairedDevice.getType());


        JSONObject deviceInfo = new JSONObject();
        try {
            deviceInfo.put("name", pairedDevice.getName());
            deviceInfo.put("address", pairedDevice.getAddress());
            deviceInfo.put("bondState", pairedDevice.getBondState() + "");
            deviceInfo.put("type", pairedDevice.getType() + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return deviceInfo;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            JSONObject result = new JSONObject();

            Log.d(TAG, "==============================");
            Log.d(TAG, "broadcastReceiver onReceive action: " + intent.getAction());
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                for(String key : bundle.keySet()){

                    Log.d(TAG, key + " => " + bundle.get(key));

                    // Insert all
                    try {
                        result.put(key, bundle.get(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Insert Devices Info
                    if (key.equalsIgnoreCase(BluetoothDevice.EXTRA_DEVICE)) {
                        try {
                            String addr = bundle.getString(key);
                            result.put("deviceInfo", getDevicesInfo(addr));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Insert Event
                    if (key.equalsIgnoreCase(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS)) {
                        Object[] args = (Object[]) bundle.get(key);
                        Log.d(TAG, "ARGS => " + Arrays.asList(args));
                        try {
                            result.put("event", args);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            mCallbackContext.success(result);

        }

    };
}
