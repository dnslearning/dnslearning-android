package org.dnslearning;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import org.dnslearning.helper.StaticContext;

import static org.dnslearning.R.id.progressLabel;

public class StartupActivity extends DNSLearningActivity {
    SharedPreferences prefs;
    TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        label = (TextView)findViewById(progressLabel);

        label.setText("Reading...");
        prefs = StaticContext.getPrefs();
        String childToken = prefs.getString("childToken", "").trim();
        String dns = prefs.getString("dns", "").trim();
        String parentToken = prefs.getString("parentToken", "").trim();

        if (!parentToken.isEmpty()) {
            showActivity(ParentActivity.class);
        } else if (!childToken.isEmpty() && !dns.isEmpty()) {
            showActivity(ChildActivity.class);
        } else {
            showActivity(FirstTimeActivity.class);
        }
    }

    protected void showActivity(Class<? extends Activity> c) {
        Intent intent = new Intent(StartupActivity.this, c);
        startActivity(intent);
        finish();
    }
}
