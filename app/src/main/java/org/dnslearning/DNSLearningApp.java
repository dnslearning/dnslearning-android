package org.dnslearning;

import android.app.Application;

import org.dnslearning.helper.StaticContext;

public class DNSLearningApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StaticContext.AppContext = getApplicationContext();
    }
}
