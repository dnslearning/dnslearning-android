package org.dnslearning.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class StaticContext {
    public static Context AppContext;

    private static SharedPreferences prefs;

    public static SharedPreferences getPrefs() {
        if (prefs != null) {
            return prefs;
        }

        if (AppContext == null) {
            throw new IllegalArgumentException("Context not set yet");
        }

        prefs = AppContext.getSharedPreferences("DNSLearningSettings", 0);
        return prefs;
    }
}
