package com.smartmadre.smartdns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.smartmadre.smartdns.helper.StaticContext;

public class NetworkMonitor extends BroadcastReceiver {
    public static String getCurrentWiFiSSID() {
        WifiManager wifiManager = (WifiManager) StaticContext.AppContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        if (currentWifi != null) {
            return currentWifi.getSSID();
        }
        return null;
    }

    public NetworkMonitor() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(NetworkMonitor.class.getName(), "onReceive: ");
        ServiceManager.ensureService();
    }
}
