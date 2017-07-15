package org.dnslearning;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.dnslearning.helper.StaticContext;

public class ParentActivity extends DNSLearningActivity {
    WebView webview;
    SharedPreferences prefs;
    String token;
    WebViewClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        prefs = StaticContext.getPrefs();
        webview = (WebView)findViewById(R.id.webview);
        token = prefs.getString("parentToken", "");

        if (token.isEmpty()) {
            toast("Unable to find token");
            return;
        }

        client = new WebViewClient();
        webview.setWebViewClient(client);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.loadUrl("http://kris.smartmadre.com/login?hash=" + token);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}
