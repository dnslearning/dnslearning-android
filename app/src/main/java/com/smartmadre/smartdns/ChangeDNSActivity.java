package com.smartmadre.smartdns;

import android.content.Intent;
import android.net.VpnService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String ip = String.valueOf(text.getText());
                PreferenceManager.setDNS(ip);

                Intent intent = VPNService.prepare(getApplicationContext());
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
            Intent intent = new Intent(this, VPNService.class);
            startService(intent);
        }
    }
}
