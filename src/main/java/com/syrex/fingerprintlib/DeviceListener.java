package com.syrex.fingerprintlib;

import java.util.ArrayList;

/**
 * Created by Ilya on 24.11.2015.
 */
public interface DeviceListener {

//    int UNKNOWN = -100;
    int OK = 0;
    int CANCEL = 1;
    int ERROR = -1; //DATA_CHECK_ERR //data check is error
//    int NOT_CONNECTED = -11;
    //int DATA_CHECK_ERR       = -1; //data check is error
    int DATA_NOT_END_ERR     = -2; //data without finish mark
    int CMD_ERR_OR_BUSY      = -3; //command return error or transfer busy
    int BT_RECONNECT_OR_BUSY = -4; //bluetooth disconnect, reconnect or transfer busy
    int DATA_LENGTH_ERR       = -5; //data length is error
    int ALGORITHM_VERIFY_ERR = -6; //algorithm check error
    int GEN_TEMPLATE_ERR      = -7; //fingerprint image synthetise template is failed
    int GET_FEATURE_ERR       = -8; //fingerprint image extract

    void onCancel();

    void onGetFeature(String feature);

    void onGetTemplate(String template);

    void onError(int status);

    void onMatch(boolean matched);
}
