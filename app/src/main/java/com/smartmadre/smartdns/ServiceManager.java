package com.smartmadre.smartdns;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.smartmadre.smartdns.helper.StaticContext;

/**
 * Created by dzmitry on 19/02/2017.
 */

public class ServiceManager {
    public static boolean isWorking() {
        return isServiceRunning(StaticContext.AppContext, VPNService.class);
    }

    private static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void stop()
    {
        StaticContext.AppContext.sendBroadcast(new Intent("STOP_SMART_DNS"));
    }
}
