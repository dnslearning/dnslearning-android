package org.dnslearning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.dnslearning.helper.StaticContext;

public class NetworkMonitor extends BroadcastReceiver {
    public static Boolean isConnectedToWiFi() {
        WifiManager wifiManager = (WifiManager) StaticContext.AppContext.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public static String getCurrentWiFiSSID() {
        WifiManager wifiManager = (WifiManager) StaticContext.AppContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = wifiManager.getConnectionInfo();
        if (currentWifi != null) {
            String ssid = currentWifi.getSSID();
            if (ssid == null)
                return null;
            return ssid.replaceAll("^\"|\"$", "");
        }
        return null;
    }

    public NetworkMonitor() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ServiceManager.ensureService();
    }
}
