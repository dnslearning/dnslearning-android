package org.dnslearning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.dnslearning.helper.StaticContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.dnslearning.R.id.progressLabel;

public class StartupActivity extends AppCompatActivity {
    private DNSLearningAPI api;
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StaticContext.AppContext = getApplicationContext();
        api = new DNSLearningAPI(Volley.newRequestQueue(this));

        setContentView(R.layout.activity_startup);
        label = (TextView)findViewById(progressLabel);

        JSONObject req = new JSONObject();
        String hardwareId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String token = StaticContext.getLoginToken();

        // TODO run a hash function on the hardware IDs

        try {
            req.put("hardwareId", hardwareId);
            req.put("token", token);
            req.put("name", android.os.Build.MODEL);
        } catch (JSONException e) {
            showError("Unable to encode request");
            return;
        }

        label.setText("Connecting...");
        api.call("android", req, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedPreferences.Editor editor = StaticContext.getPrefs().edit();
                String token = "";
                String error = null;

                try {
                    if (response.has("error")) {
                        error = response.getString("error");
                    } else if (response.has("token")) {
                        token = response.getString("token");
                        StaticContext.pairCode = response.getString("pairCode");
                        StaticContext.ipv4 = response.getString("ipv4");
                        StaticContext.ipv6 = response.getString("ipv6");
                        editor.putString("ipv4", StaticContext.ipv4);
                        editor.putString("ipv6", StaticContext.ipv6);
                    } else {
                        error = "No response";
                    }
                } catch (JSONException e) {
                    error = "Bad Response";
                }

                if (error == null && (token == null || token.isEmpty())) {
                    error = "Missing Token";
                }

                if (error != null) {
                    editor.remove("token");
                    editor.commit();
                    showError(error);
                    finish();
                    return;
                }

                editor.putString("token", token);
                editor.commit();

                Intent intent = new Intent(StartupActivity.this, HudActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showError(String message) {
        Log.d("dnslearning", message);

        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }
}
