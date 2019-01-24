package org.dnslearning;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;

import org.dnslearning.helper.StaticContext;

/**
 * Ensures the VPN is running while showing the HUD page
 */
public class HudActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String hudhash;//, dns;
    private Button unlockButton;
    private ImageButton configButton;
    private Handler reloadHandler;
    private WebView childWebView;
    private Runnable reloadRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("VPNService", "Creating HudActivity");

        setContentView(R.layout.activity_hud);
        prefs = StaticContext.getPrefs();
        childWebView = (WebView)findViewById(R.id.childWebView);
        unlockButton = (Button)findViewById(R.id.unlockButton);
        configButton = (ImageButton)findViewById(R.id.configButton);

        reloadRunnable =  new Runnable() {
            @Override
            public void run() {
                try {
                    reloadPage();
                    styleUnlockButton(ServiceManager.isWorking());
                } finally {
                    reloadHandler.postDelayed(reloadRunnable, 5000);
                }
            }
        };

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
        //dns = prefs.getString("dns", "").trim();

        if (hudhash.isEmpty()) { // || dns.isEmpty()) {
            bail("Missing storage");
            return;
        }

        // Start a loop of reloading the HUD page
        reloadHandler = new Handler();
        reloadHandler.postDelayed(reloadRunnable, 333);

        ServiceManager.ensureService();
    }

    protected void showConfig() {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

    protected void bail(String reason) {
        /*
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, FirstTimeActivity.class);
        startActivity(intent);
        finish();
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (reloadHandler != null) {
            reloadHandler.removeCallbacks(reloadRunnable);
        }
    }

    protected void reloadPage() {
        childWebView.loadUrl("https://studycity.org/device/" + hudhash);
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
