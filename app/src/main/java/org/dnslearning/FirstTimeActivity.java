package org.dnslearning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FirstTimeActivity extends DNSLearningActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);

        bindButton(R.id.newParentButton, SignupActivity.class);
        bindButton(R.id.addChildButton, PairActivity.class);
        bindButton(R.id.loginButton, LoginActivity.class);
    }

    protected void bindButton(int id, final Class<? extends Activity> c) {
        getButton(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstTimeActivity.this, c);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                //finish();
            }
        });
    }
}
