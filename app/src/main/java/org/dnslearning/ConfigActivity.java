package org.dnslearning;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.appcompat.*;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dnslearning.helper.StaticContext;

public class ConfigActivity extends AppCompatActivity {
    private TextView pairCode;
    private TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        pairCode = (TextView)findViewById(R.id.pairCode);
        pairCode.setText(StaticContext.pairCode);

        versionText = (TextView)findViewById(R.id.versionText);
        versionText.setText("Version: " + BuildConfig.VERSION_NAME);
    }

    public void saveConfig(View view) {
        finish();
    }
}
