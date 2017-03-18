package com.smartmadre.smartdns;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.smartmadre.smartdns.helper.StaticContext;
import com.smartmadre.smartdns.preferences.PreferenceManager;

public class ChangeDNSActivity extends AppCompatActivity {
    EditText dnsIpAddress;
    Button enableDNSButton;
    CheckBox onlyForCurrentWiFi;
    private static final int ADMIN_RECEIVER_REQUEST_CODE = 999;

    private void updateUI() {
        dnsIpAddress.setText(PreferenceManager.getDNS());
        if (PreferenceManager.getLimitedToWiFi() != null) {
            onlyForCurrentWiFi.setChecked(true);
        }

        if (PreferenceManager.getVpnServiceEnabled()) {
            dnsIpAddress.setEnabled(false);
            onlyForCurrentWiFi.setEnabled(false);

            if (PreferenceManager.getLimitedToWiFi() != null) {
                onlyForCurrentWiFi.setText("Only for Wi-Fi \"" + PreferenceManager.getLimitedToWiFi() + "\"");
            } else {
                onlyForCurrentWiFi.setText(R.string.only_for_current_wifi);
            }
            enableDNSButton.setText(R.string.disable_dns);
            enableDNSButton.setTextColor(getResources().getColor(R.color.colorAccentRed));
        } else {
            dnsIpAddress.setEnabled(true);
            onlyForCurrentWiFi.setEnabled(true);

            if (NetworkMonitor.isConnectedToWiFi()) {
                onlyForCurrentWiFi.setVisibility(View.VISIBLE);
                onlyForCurrentWiFi.setEnabled(true);
                onlyForCurrentWiFi.setText("Only for Wi-Fi \"" + NetworkMonitor.getCurrentWiFiSSID() + "\"");
            } else {
                onlyForCurrentWiFi.setVisibility(View.INVISIBLE);
            }
            enableDNSButton.setText(R.string.enable_dns);
            enableDNSButton.setTextColor(getResources().getColor(R.color.colorPrimary));
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

        registerReceiver(new ConnectivityChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        enableDNSButton.setOnClickListener(enableDNSButtonListener());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADMIN_RECEIVER_REQUEST_CODE) {
            setupVPNService();
        } else if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, VPNService.class);
            startService(intent);
            PreferenceManager.setVpnServiceEnabled(true);
            updateUI();
        }
    }

    protected View.OnClickListener enableDNSButtonListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                String ip = String.valueOf(dnsIpAddress.getText());
                PreferenceManager.setDNS(ip);

                if (PreferenceManager.getVpnServiceEnabled()) {
                    ServiceManager.stop();
                    PreferenceManager.setVpnServiceEnabled(false);
                    PreferenceManager.setLimitedToWiFi(null);
                    updateUI();
                    return;
                }

                if (onlyForCurrentWiFi.isChecked()) {
                    PreferenceManager.setLimitedToWiFi(NetworkMonitor.getCurrentWiFiSSID());
                }

                setupDeviceAdminReceiver();
            }
        };
    }

    private void setupVPNService() {
        Intent intent = VPNService.prepare(StaticContext.AppContext);
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    private void setupDeviceAdminReceiver() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, AltaDNSDeviceAdminReceiver.class);

        if (devicePolicyManager.isAdminActive(componentName) || !PreferenceManager.getSuggestDeviceAdmin()) {
            setupVPNService();
        } else {
            showDeviceAdminInfoPopup();
        }
    }

    private void showDeviceAdminInfoPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.device_admin_activation_notification)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ComponentName componentName = new ComponentName(ChangeDNSActivity.this, AltaDNSDeviceAdminReceiver.class);
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                        startActivityForResult(intent, ADMIN_RECEIVER_REQUEST_CODE);
                    }
                })
                .setNegativeButton(R.string.dont_show_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.setSuggestDeviceAdmin(false);
                        setupVPNService();
                    }
                });
        builder.show();
    }

    class ConnectivityChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (onlyForCurrentWiFi.isChecked() || PreferenceManager.getVpnServiceEnabled())
                return;

            if (NetworkMonitor.isConnectedToWiFi()) {
                onlyForCurrentWiFi.setVisibility(View.VISIBLE);
                onlyForCurrentWiFi.setText("Only for Wi-Fi \"" + NetworkMonitor.getCurrentWiFiSSID() + "\"");
            } else {
                onlyForCurrentWiFi.setVisibility(View.INVISIBLE);
                onlyForCurrentWiFi.setChecked(false);
            }
        }
    }
}
