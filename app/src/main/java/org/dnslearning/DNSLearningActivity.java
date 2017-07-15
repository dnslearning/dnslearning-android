package org.dnslearning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Base class for all activities in our app
 */
public class DNSLearningActivity extends AppCompatActivity {
    public DNSLearningApp getApp() {
        return (DNSLearningApp)getApplication();
    }

    public Button getButton(int id) {
        return (Button)findViewById(id);
    }

    public TextView getTextView(int id) {
        return (TextView)findViewById(id);
    }

    public JsonObjectRequest callAPI(String method, JSONObject req, Response.Listener<JSONObject> f) {
        return getApp().getAPI().call(method, req, f);
    }

    public void prompt(String title, String msg, DialogInterface.OnClickListener f) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(android.R.string.yes, f);

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected Toast toast(String s) {
        Toast t = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        t.show();
        return t;
    }

    protected DNSLearningAPI getAPI() {
        return getApp().getAPI();
    }
}
