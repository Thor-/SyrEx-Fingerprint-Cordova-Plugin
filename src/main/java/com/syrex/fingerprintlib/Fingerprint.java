package com.syrex.fingerprintlib;

import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ilya on 24.11.2015.
 */
public class Fingerprint extends CordovaPlugin implements DeviceListener {

    public static final String TAG = "Fingerprint";
    private static final String ACTION_GET_TEMPLATE = "getTemplate";
    private static final String ACTION_GET_FEATURE = "getFeature";
    private static final String ACTION_CLOSE_BT = "closeBluetooth";
    private static final String ACTION_MATCH = "match";

    private BluetoothManager mBluetoothManager;
    private CallbackContext mCallbackContext;
    private String mAction;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        initBluetoothManager();
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute: " + action);
        Boolean result = true;
        if (action.equals(ACTION_GET_TEMPLATE)) {
            mCallbackContext = callbackContext;
            mAction = action;
            mBluetoothManager.getTemplate();
        } else if (action.equals(ACTION_GET_FEATURE)) {
            mCallbackContext = callbackContext;
            mAction = action;
            mBluetoothManager.getFeature();
        } else if (action.equals(ACTION_CLOSE_BT)) {
            mBluetoothManager.closeBt();
        } else if (action.equals(ACTION_MATCH)) {
            final String feature = args.optString(0);
            final String template = args.optString(1);
            if (feature.isEmpty() || template.isEmpty())
                return false;

            mCallbackContext = callbackContext;
            mAction = action;
//            cordova.getThreadPool().execute(new Runnable() {
//                public void run() {
                    boolean r = mBluetoothManager.match(feature, template);
                    onMatch(r);
//                    callbackContext.success(); // Thread-safe.
//                }
//            });
        } else {
            result = false;
            //reset();
        }
        return result;
    }

    private void initBluetoothManager() {
        if (mBluetoothManager == null) {
            mBluetoothManager = new BluetoothManager(cordova.getActivity(), this);
//            cordova.setActivityResultCallback(this);
        }
    }

//    private void reset() {
//        mCallbackContext = null;
//        mAction = null;
//    }

    private void success(int status, Object data) {
        if (mCallbackContext == null)
            return;

        JSONObject obj = new JSONObject();
        try {
            obj.put("status", status);
            obj.put("data", data);
            mCallbackContext.success(obj);
            Log.d(TAG, "success: done");
        } catch (JSONException e) {
            error(e.getMessage());
            Log.e(TAG, e.getMessage());
        }
    }

    private void error(String message) {
        Log.e(TAG, "error: " + message);
        if (mCallbackContext != null)
            mCallbackContext.error(message);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        mBluetoothManager.onActivityResult(requestCode, resultCode, intent);
//    }

    @Override
    public void onCancel() {
        Log.d(TAG, "onCancel");
        success(DeviceListener.CANCEL, null);
        //reset();
    }

    @Override
    public void onGetFeature(String feature) {
        Log.d(TAG, "onGetFeature: feature=" + feature);
//        if (mAction != null && mAction.equals(ACTION_GET_FEATURE) && mCallbackContext != null) {
            success(DeviceListener.OK, feature);
            //reset();
//        }
    }

    @Override
    public void onGetTemplate(String template) {
        Log.d(TAG, "onGetTemplate: template=" + template);
//        if (mAction != null && mAction.equals(ACTION_GET_TEMPLATE) && mCallbackContext != null) {
            success(DeviceListener.OK, template);
//        }
    }

    @Override
    public void onMatch(boolean matched) {
        Log.d(TAG, "onMatch: matched=" + matched);
        //if (mAction != null && mAction.equals(ACTION_MATCH) && mCallbackContext != null) {
            success(DeviceListener.OK, matched);
//        }
    }

    @Override
    public void onError(int status) {
        Log.d(TAG, "onError: status=" + status);
        error(Integer.toString(status));
    }
}
