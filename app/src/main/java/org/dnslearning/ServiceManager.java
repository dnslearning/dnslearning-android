package org.dnslearning;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.dnslearning.helper.StaticContext;

/**
 * Helper methods for controlling the VpnService
 */
public class ServiceManager {
    public static boolean isWorking() {
        return isServiceRunning(StaticContext.AppContext, DNSLearningVpnService.class);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    public static void stop() {
        StaticContext.AppContext.sendBroadcast(new Intent("STOP_SMART_DNS"));
    }

    public static void start() {
        if (isWorking()) {
            return;
        }

        Intent intent = new Intent(StaticContext.AppContext, VpnHelperActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        StaticContext.AppContext.startActivity(intent);
    }

    public static void ensureService() {
        Log.d("VPNService", "Ensuring service is running");

        SharedPreferences prefs = StaticContext.getPrefs();
        String dns = prefs.getString("dns", "").trim();

        if (dns.isEmpty()) {
            stop();
        } else {
            start();
        }
    }
}
