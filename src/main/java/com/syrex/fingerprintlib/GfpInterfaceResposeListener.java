package com.syrex.fingerprintlib;

import android.os.Bundle;

/**
 * Created by Ilya on 24.11.2015.
 */
public interface GfpInterfaceResposeListener {

    void onResponse(int statusCode, Bundle data);
}
