package org.dnslearning;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.dnslearning.helper.StaticContext;

/**
 * Launches the VpnService asking the user for the needed permissions
 * if needed
 */
public class VpnHelperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("VPNService", "Creating Vpn helper activity");
        Intent intent = DNSLearningVpnService.prepare(StaticContext.AppContext);

        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }

        // Finishing here seems to sometimes not start the service
        // for the first time
        //finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("VPNService", "User responded to the dialog");

        if (resultCode == RESULT_OK) {
            Log.d("VPNService", "Helper activity is staritng service");
            Intent intent = new Intent(this, DNSLearningVpnService.class);
            startService(intent);
        }

        finish();
    }
}
