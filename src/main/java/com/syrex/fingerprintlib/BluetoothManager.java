package com.syrex.fingerprintlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aids.syrex.ocfa.R;
import com.wellcom.verify.GfpInterface;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Ilya on 24.11.2015.
 */
public class BluetoothManager implements GfpInterfaceResposeListener {

    public static final String TAG = "BluetoothManager";

    private final static int GET_FEATURE = 1;
    private final static int GET_TEMPLATE = 2;
    private final static int NOTHING = 0;

    private final ArrayList<String> mDevices = new ArrayList<>();
    private final ArrayAdapter<String> mArrayAdapter;
    private final Activity mActivity;
    private final DeviceListener mDeviceListener;
    private final GfpInterface mGPFInterface;

    private int mWaitFor = NOTHING;
    private String mDeviceName = null;
    boolean mDialogShowed = false;
    boolean mDialogCanceled = false;

    public BluetoothManager(Activity activity, DeviceListener deviceListener) {
        mActivity = activity;
        mDeviceListener = deviceListener;
        mGPFInterface = new GfpInterface(activity, new GfpInterfaceHandler(this));
        mArrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);

                return view;
            }
        };
    }

    public boolean getFeature() {
        Log.d(TAG, "getFeature");
        mWaitFor = GET_FEATURE;

        return openBt();
    }

    public boolean getTemplate() {
        Log.d(TAG, "getTemplate");
        mWaitFor = GET_TEMPLATE;

        return openBt();
    }

    public boolean match(String feature, String template) {
        Log.d(TAG, "match");
        byte[] bTemplate = mGPFInterface.CGfpArithApp.CDataTrans.fpStrToByte(template);
        byte[] bFeature = mGPFInterface.CGfpArithApp.CDataTrans.fpStrToByte(feature);
        return doMatch(bTemplate, bFeature);
    }

    public void closeBt() {
        mGPFInterface.fpiDisconnectBT();
        mGPFInterface.sysCloseBT();
    }

    private boolean openBt() {
        Log.d(TAG, "openBt");
        if (!mGPFInterface.sysCheckBTOpened()) {
            if (!mGPFInterface.fpiOpenBT()) {
                mDeviceListener.onError(DeviceListener.ERROR);
                return false;
            }
        }
        if (mDeviceName == null || mDeviceName.isEmpty()) {
            startDiscoveryDevices();
            return true;
        } else {
            Integer r = connect();
            if (r == 1) {
                if (mWaitFor == GET_FEATURE)
                    mGPFInterface.fpiGetFeature(10);
                else if (mWaitFor == GET_TEMPLATE)
                    mGPFInterface.fpiGetTemplate(10);

                return true;
            }
            return r > -1;
        }
    }

    private void startDiscoveryDevices() {
        mArrayAdapter.clear();
        Set<BluetoothDevice> pairedDevices = mGPFInterface.CGfpArithApp.mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            final ArrayList<String> names = new ArrayList<>(pairedDevices.size());
            for (BluetoothDevice device : pairedDevices) {
                String dn = device.getName();
                if (names.indexOf(dn) == -1)
                    names.add(device.getName());
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mArrayAdapter.addAll(names);
                    showDevicesList();
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        mActivity.registerReceiver(mDeviceDiscoverReceiver, filter);
        mGPFInterface.CGfpArithApp.mBluetoothAdapter.startDiscovery();
    }

    private void stropDiscoveryDevices() {
        if (mGPFInterface.CGfpArithApp.mBluetoothAdapter != null &&
                mGPFInterface.CGfpArithApp.mBluetoothAdapter.isDiscovering())
            mGPFInterface.CGfpArithApp.mBluetoothAdapter.cancelDiscovery();
    }

    private int connect() {
        int r = mGPFInterface.fpiConnectBT(mDeviceName);
        Log.d(TAG, "connect: " + r);
        if (r == -1) {
            mDeviceListener.onError(DeviceListener.ERROR);
            return -1;
        }
        return 1;
    }

    private void addFoundDevice() {
        boolean flag = false;
        for (String name : mDevices) {
            if (mArrayAdapter.getPosition(name) < 0) {
                mArrayAdapter.add(name);
                flag = true;
            }
        }
        if (flag)
            mArrayAdapter.notifyDataSetChanged();
    }

    private boolean doMatch(byte[] feature, byte[] template) {

//            if (feature.length != 256)
        return mGPFInterface.sysOneMatch(template, feature) > 0;
    }

    private BroadcastReceiver mDeviceDiscoverReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                if (mDeviceName == null || mDeviceName.isEmpty()) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String name = device.getName();
                    if (name != null && !name.isEmpty()) {
                        mDevices.add(name);
                        if (mArrayAdapter.getPosition(name) == -1) {
                            addDevice(name);
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mDeviceName == null || mDeviceName.isEmpty()) {
                    stropDiscoveryDevices();
                    //showNoFoundMessage();
                    mDeviceListener.onError(DeviceListener.CANCEL);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                closeBt();
            }
        }
    };

    private void addDevice(final String name) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mArrayAdapter.add(name);
                mArrayAdapter.notifyDataSetChanged();
                showDevicesList();
            }
        });
    }

    private void showDevicesList() {
        if (mDialogShowed || mArrayAdapter.isEmpty())
            return;

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mActivity);
        dlgBuilder.setTitle("Найденые устройства:");
        dlgBuilder.setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stropDiscoveryDevices();
                        mDeviceListener.onCancel();
                        dialog.dismiss();
                        mDialogCanceled = true;
                        mDialogShowed = false;
                    }
                });
        dlgBuilder.setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeviceName = mArrayAdapter.getItem(which);
                Log.d(TAG, "Device selected: " + mDeviceName);
                stropDiscoveryDevices();
                // We should wait a little to connect successfully
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                connect();
                dialog.dismiss();
                mDialogShowed = false;
            }
        });
//        try {
        AlertDialog alertDlg = dlgBuilder.create();
        alertDlg.show();
        mDialogShowed = true;
        mDialogCanceled = false;
//        } catch (Throwable e) {
//            Log.e(TAG, "", e);
//            mDeviceListener.onError(DeviceListener.ERROR);
//        }
    }

    private void showNoFoundMessage() {
        if (mDialogShowed || mDialogCanceled)
            return;
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mActivity);
        dlgBuilder.setMessage("Ни одно устройство не было найдено");
        dlgBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mDeviceListener.onError(DeviceListener.CANCEL);
            }
        });
//        try {
        AlertDialog alertDlg = dlgBuilder.create();
        alertDlg.show();
//        } catch (Throwable e) {
//            Log.e(TAG, "", e);
//            mDeviceListener.onError(DeviceListener.ERROR);
//        }
    }

    private void sendCommand() {
        Integer state = mGPFInterface.CGfpArithApp.mBtService.getState();
        Log.d(TAG, "sendCommand: mBtService state = " + state);

        if (mWaitFor == GET_FEATURE)
            mGPFInterface.fpiGetFeature(10);
        else if (mWaitFor == GET_TEMPLATE)
            mGPFInterface.fpiGetTemplate(10);
    }

    @Override
    public void onResponse(int statusCode, Bundle data) {
        Log.d(TAG, "onResponse: " + statusCode);
        Log.d(TAG, "WaitFor: " + mWaitFor);
        switch (statusCode) {
//            case GfpInterfaceHandler.CANCEL:
//                mDeviceListener.onCancel();
//                break;
            case GfpInterfaceHandler.OK:
                sendCommand();
                break;

            case GfpInterfaceHandler.CODE_FEATURE:
                mDeviceListener.onGetFeature(data.getString(GfpInterfaceHandler.FEATURE));
                mGPFInterface.fpiDisconnectBT();
                break;

            case GfpInterfaceHandler.CODE_TEMPlATE:
                mDeviceListener.onGetTemplate(data.getString(GfpInterfaceHandler.TEMPLATE));
                mGPFInterface.fpiDisconnectBT();
                break;
            default:
                if (statusCode < 0) {
                    mDeviceListener.onError(statusCode);
                }
                break;
        }
    }
}
