package com.syrex.fingerprintlib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wellcom.verify.FormatTransfer;

/**
 * Created by Ilya on 24.11.2015.
 */
public class GfpInterfaceHandler extends Handler {
    public static final String TAG = "GfpInterfaceHandler";

    public static final int CODE_STATUS = 161; //160;
    public static final int CODE_VERSION = 176;
    public static final int CODE_IMAGE = 179;
    public static final int CODE_FEATURE = 164;//180;
    public static final int CODE_TEMPlATE = 165; //181;

    public static final int ERROR = -1;
    public static final int OK = 0;
    public static final int  UNKNOWN = -100;
    public static final String FEATURE = "Feature";
    public static final String TEMPLATE = "Template";

    private final GfpInterfaceResposeListener mGfpInterfaceResposeListener;
    private final FormatTransfer mFormatTransfer;

    public GfpInterfaceHandler(GfpInterfaceResposeListener gfpInterfaceResposeListener){
        mGfpInterfaceResposeListener = gfpInterfaceResposeListener;
        mFormatTransfer = new FormatTransfer();
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, "handleMessage");
        if (msg == null) {
            mGfpInterfaceResposeListener.onResponse(UNKNOWN, Bundle.EMPTY);
            return;
        }
        Bundle bundle = msg.getData();
        if (bundle == null) {
            mGfpInterfaceResposeListener.onResponse(UNKNOWN, Bundle.EMPTY);
            return;
        }
        int error = bundle.getInt("intGetError");
        if (error != 0) {
            Log.d(TAG, "handleMessage: error=" + error);
            mGfpInterfaceResposeListener.onResponse(error, Bundle.EMPTY);
            return;
        }
        Log.d(TAG, "handleMessage: code=" + msg.what);
        switch (msg.what) {
            case 160:
//                mGfpInterfaceResposeListener.onResponse(CANCEL, Bundle.EMPTY);
                error = bundle.getInt("FPIGetError");
                if (error != 0){
                    Log.d(TAG, "handleMessage: status=160; FPIGetError=" + error);
                    mGfpInterfaceResposeListener.onResponse(error, Bundle.EMPTY);
                }
                break;

            case CODE_STATUS:
                int status = bundle.getInt("FPIBTStatus");
                Log.d(TAG, "handleMessage: status=161; FPIBTStatus=" + status);
                if (status == 3)// || status == 2)
                    mGfpInterfaceResposeListener.onResponse(OK, Bundle.EMPTY);
                break;

            case CODE_FEATURE:
            case CODE_TEMPlATE: {
                byte[] byteArryFpFTR = (byte[]) msg.obj;
                int intTPTLen = msg.arg1;
                byte[] arrayTemplate = new byte[intTPTLen];
                System.arraycopy(byteArryFpFTR, 0, arrayTemplate, 0, intTPTLen);
                String dataStr = mFormatTransfer.fpByteToStr(arrayTemplate);
                Bundle b = new Bundle();
                b.putString(msg.what == CODE_FEATURE ? FEATURE : TEMPLATE, dataStr);
                mGfpInterfaceResposeListener.onResponse(msg.what, b);
                break;
            }
            default:
                mGfpInterfaceResposeListener.onResponse(UNKNOWN, Bundle.EMPTY);
                break;
        }
    }
}
