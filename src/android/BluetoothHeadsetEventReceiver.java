package cordova.bluetooth.headseteventreceiver;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;


/**
 * This class echoes a string called from JavaScript.
 */
public class BluetoothHeadsetEventReceiver extends CordovaPlugin {
    private final static String TAG = "HeadsetEventReceiver";
    private final static String REGISTER_RECEIVER_SUCCESS = "REGISTER_RECEIVER_SUCCESS";
    CallbackContext mCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("registerReceiver")) {
            this.mCallbackContext = callbackContext;

            setBroadcastReceiver();

            PluginResult result = new PluginResult(PluginResult.Status.OK, REGISTER_RECEIVER_SUCCESS);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;
        }
        return true;
    }

    public void setBroadcastReceiver() {
        try {
            //  Ensure we only have a single registered broadcast receiver
            this.cordova.getActivity().unregisterReceiver(this.broadcastReceiver);
        }
        catch (IllegalArgumentException e) {}

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY+"."+ BluetoothAssignedNumbers.PLANTRONICS);
        intentFilter.addAction(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE);
        intentFilter.addAction(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
        intentFilter.addAction(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS);
        intentFilter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        this.cordova.getActivity().registerReceiver(broadcastReceiver, intentFilter);
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
                            String addr = bundle.get(key).toString();
                            Log.d(TAG, "addr => " + addr);
                            result.put("deviceInfo", getDevicesInfo(addr));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Insert Event
                    if (key.equalsIgnoreCase(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS)) {
                        Object[] args = (Object[]) bundle.get(key);
                        JSONArray jsonArray = new JSONArray();
                        Log.d(TAG, "ARGS => " + Arrays.asList(args));

                        for (Object value : args) {
                            jsonArray.put(value);
                        }

                        try {
                            result.put("event", jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
            pluginResult.setKeepCallback(true);
            mCallbackContext.sendPluginResult(pluginResult);

        }

    };
}
