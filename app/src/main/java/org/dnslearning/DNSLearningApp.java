package org.dnslearning;

import android.app.Application;

import com.android.volley.toolbox.Volley;

import org.dnslearning.helper.StaticContext;

public class DNSLearningApp extends Application {
    private DNSLearningAPI api;

    @Override
    public void onCreate() {
        super.onCreate();
        StaticContext.AppContext = getApplicationContext();

        api = new DNSLearningAPI(Volley.newRequestQueue(this));
    }

    public DNSLearningAPI getAPI() {
        return api;
    }
}
