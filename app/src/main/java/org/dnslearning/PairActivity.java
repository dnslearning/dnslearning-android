package org.dnslearning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Response;

import org.dnslearning.helper.StaticContext;
import org.json.JSONException;
import org.json.JSONObject;

public class PairActivity extends DNSLearningActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);
        prefs = StaticContext.getPrefs();

        getButton(R.id.pairButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    protected void submit() {
        String code = getTextView(R.id.codeText).getText().toString().trim();

        if (code.isEmpty()) {
            toast("Missing code");
            return;
        }

        JSONObject req = new JSONObject();

        try {
            req.put("code", code);
        } catch (JSONException e) {
            toast("Bad code");
            return;
        }

        getButton(R.id.pairButton).setEnabled(false);
        toast("Checking...");

        callAPI("pairDevice", req, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.optInt("ok") <= 0) {
                    finishError(response.optString("reason", "Server Error"));
                    return;
                }

                String hudhash = response.optString("hudhash");

                if (hudhash == null || hudhash.isEmpty()) {
                    finishError("Unknown code");
                    return;
                }

                String dns = response.optString("dns");

                if (dns == null || dns.isEmpty()) {
                    finishError("No server found");
                    return;
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("childToken", hudhash);
                editor.putString("dns", dns);
                editor.commit();

                Intent intent = new Intent(PairActivity.this, ChildActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void finishError(String s) {
        toast(s);
        getButton(R.id.pairButton).setEnabled(true);
    }
}
