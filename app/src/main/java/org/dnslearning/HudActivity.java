package org.dnslearning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;

import org.dnslearning.helper.StaticContext;

/**
 * Ensures the VPN is running while showing the HUD page
 */
public class HudActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String hudhash;
    private Button unlockButton;
    private ImageButton configButton;
    private WebView childWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("VPNService", "Creating HudActivity");

        setContentView(R.layout.activity_hud);
        prefs = StaticContext.getPrefs();
        childWebView = (WebView)findViewById(R.id.childWebView);
        unlockButton = (Button)findViewById(R.id.unlockButton);
        configButton = (ImageButton)findViewById(R.id.configButton);

        childWebView.getSettings().setJavaScriptEnabled(true);

        childWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emergencyUnlock();
            }
        });
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfig();
            }
        });

        hudhash = prefs.getString("token", "").trim();

        if (hudhash.isEmpty()) {
            bail("Missing storage");
            return;
        }

        childWebView.loadUrl("https://studycity.org/device/" + hudhash);

        ServiceManager.ensureService();
    }

    protected void showConfig() {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

    protected void bail(String reason) {
        Log.d("dnslearning", "Bailing because " + reason);

        /*
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, FirstTimeActivity.class);
        startActivity(intent);
        finish();
        */
    }

    protected void emergencyUnlock() {
        if (ServiceManager.isWorking()) {
            stopAfterPrompt();
        } else {
            ServiceManager.start();
            styleUnlockButton(true);
        }
    }

    protected void styleUnlockButton(boolean locked) {
        if (locked) {
            unlockButton.setText("Emergency Unlock");
            unlockButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorEmergencyUnlock));
        } else {
            unlockButton.setText("Enable");
            unlockButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorEmergencyLock));
        }
    }

    protected void stopAfterPrompt() {
        String title = "Are you sure?";
        String msg = "Stopping will disable all DNS Learning functionality and alert your parents";

        /*
        prompt(title, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ServiceManager.stop();
                styleUnlockButton(false);
            }
        });
        */
    }
}
