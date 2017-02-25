package com.smartmadre.smartdns;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.smartmadre.smartdns.preferences.PreferenceManager;

public class ChangeDNSActivity extends AppCompatActivity {
    EditText dnsIpAddress;
    Button enableDNSButton;
    CheckBox onlyForCurrentWiFi;

    private void updateUI() {
        dnsIpAddress.setText(PreferenceManager.getDNS());
        if (PreferenceManager.getLimitedToWiFi() != null) {
            onlyForCurrentWiFi.setChecked(true);
        }

        if (PreferenceManager.getEnabled()) {
            dnsIpAddress.setEnabled(false);
            onlyForCurrentWiFi.setEnabled(false);

            if (PreferenceManager.getLimitedToWiFi() != null) {
                onlyForCurrentWiFi.setText("Only for Wi-Fi " + PreferenceManager.getLimitedToWiFi());
            } else {
                onlyForCurrentWiFi.setText("Only for current Wi-Fi");
            }
            enableDNSButton.setText("Disable DNS");
        } else {
            dnsIpAddress.setEnabled(true);
            onlyForCurrentWiFi.setEnabled(true);

            if(NetworkMonitor.getCurrentWiFiSSID() != null) {
                onlyForCurrentWiFi.setVisibility(View.VISIBLE);
                onlyForCurrentWiFi.setEnabled(true);
                onlyForCurrentWiFi.setText("Only for Wi-Fi " + NetworkMonitor.getCurrentWiFiSSID());
            } else {
                onlyForCurrentWiFi.setVisibility(View.INVISIBLE);
            }
            enableDNSButton.setText("Enable DNS");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_dns);

        dnsIpAddress = (EditText) findViewById(R.id.dnsIpAddress);
        onlyForCurrentWiFi = (CheckBox) findViewById(R.id.onlyForCurrentWiFi);
        enableDNSButton = (Button) findViewById(R.id.enableDNSButton);

        updateUI();

        enableDNSButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ip = String.valueOf(dnsIpAddress.getText());
                PreferenceManager.setDNS(ip);

                if (PreferenceManager.getEnabled()) {
                    ServiceManager.stop();
                    PreferenceManager.setEnabled(false);
                    PreferenceManager.setLimitedToWiFi(null);
                    updateUI();
                    return;
                }

                PreferenceManager.setEnabled(true);

                if (onlyForCurrentWiFi.isChecked()) {
                    PreferenceManager.setLimitedToWiFi(NetworkMonitor.getCurrentWiFiSSID());
                }

                ServiceManager.start();
                updateUI();
            }
        });
    }


}
