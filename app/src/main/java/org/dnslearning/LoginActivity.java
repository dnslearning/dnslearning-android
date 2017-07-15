package org.dnslearning;

import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import org.dnslearning.helper.StaticContext;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends DNSLearningActivity {
    private AutoCompleteTextView email;
    private EditText password;
    private Button login;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = StaticContext.getPrefs();
        email = (AutoCompleteTextView)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        login = getButton(R.id.login);

        try {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_EMAIL);
            password.requestFocus();
        } catch (ActivityNotFoundException e) {
            toast("Enter email manually");
        }

        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    protected void login() {
        JSONObject req = new JSONObject();

        try {
            req.put("email", email.getText().toString());
            req.put("password", password.getText().toString());
        } catch (JSONException e) {
            toast("Unable to create request");
            return;
        }

        toast("Logging in...");

        getAPI().call("login", req, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.optInt("ok") <= 0) {
                    toast(response.optString("reason", "Server Error"));
                    return;
                }

                String token = response.optString("token", "");

                if (token.isEmpty()) {
                    toast("Missing Token");
                    return;
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("parentToken", token);
                editor.commit();

                Intent intent = new Intent(LoginActivity.this, ParentActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private static final int REQUEST_CODE_EMAIL = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            email.setText(accountName);
        }
    }
}

