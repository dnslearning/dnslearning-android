package com.smartmadre.smartdns.preferences;

import android.content.SharedPreferences;

import com.smartmadre.smartdns.helper.StaticContext;

/**
 * Created by dzmitry on 19/02/2017.
 */

public class PreferenceManager {
    private static SharedPreferences.Editor edit;
    private static SharedPreferences preferences;

    public static String getDNS()
    {
        return preferences.getString("dns", "8.8.8.8");
    }
    public static Boolean getVpnServiceEnabled() {
        return preferences.getBoolean("vpnServiceEnabled", false);
    }
    public static String getLimitedToWiFi() {
        return preferences.getString("onlyForWiFi", null);
    }


    public static void setDNS(String ip) {
        edit.putString("dns", ip);
        edit.commit();
    }

    public static void setVpnServiceEnabled(Boolean enabled) {
        edit.putBoolean("vpnServiceEnabled", enabled);
        edit.commit();
    }

    public static void setLimitedToWiFi(String ssid) {
        edit.putString("onlyForWiFi", ssid);
        edit.commit();
    }

    public static void prepare() {
        preferences = StaticContext.AppContext.getSharedPreferences("prefs", 0);
        edit = preferences.edit();
    }
}
