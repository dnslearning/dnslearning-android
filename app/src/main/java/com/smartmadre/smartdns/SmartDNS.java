package com.smartmadre.smartdns;

import android.app.Application;

import com.smartmadre.smartdns.helper.StaticContext;
import com.smartmadre.smartdns.preferences.PreferenceManager;

/**
 * Created by dzmitry on 19/02/2017.
 */

public class SmartDNS extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StaticContext.AppContext = getApplicationContext();
        PreferenceManager.prepare();
    }
}
