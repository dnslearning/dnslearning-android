package com.smartmadre.smartdns;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.smartmadre.smartdns.helper.StaticContext;
import com.smartmadre.smartdns.preferences.PreferenceManager;

public class ChangeDNSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_dns);

        final EditText text = (EditText) findViewById(R.id.editIpAddress);
        if (savedInstanceState == null) {
            text.setText(PreferenceManager.getDNS());
        }
        final Button button = (Button) findViewById(R.id.AddDNSButton);

        if (ServiceManager.isWorking()) {
            text.setEnabled(false);
            button.setText("Remove DNS");
        } else {
            text.setEnabled(true);
            button.setText("Add DNS");
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String ip = String.valueOf(text.getText());
                PreferenceManager.setDNS(ip);

                if (ServiceManager.isWorking()) {
                    ServiceManager.stop();
                    text.setEnabled(true);
                    button.setText("Add DNS");
                    return;
                }

                text.setEnabled(false);
                button.setText("Disable DNS");

                Intent intent = VPNService.prepare(StaticContext.AppContext);
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d("ChangeDNSActivity", "Start VPNService");
            Intent intent = new Intent(this, VPNService.class);
            startService(intent);
        }
    }
}
