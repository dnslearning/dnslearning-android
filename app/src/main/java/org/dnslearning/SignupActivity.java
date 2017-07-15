package org.dnslearning;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.dnslearning.R;

public class SignupActivity extends DNSLearningActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final Button continueButton = (Button)findViewById(R.id.continueButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickActivityButton();
            }
        });
    }

    protected void clickActivityButton() {

    }
}
