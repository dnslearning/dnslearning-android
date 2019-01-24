package org.dnslearning;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.*;

import android.util.Log;

/**
 * API for posting and getting JSON
 */
public class DNSLearningAPI {
    private String baseUrl = "https://studycity.org";
    private RequestQueue queue;

    public DNSLearningAPI(RequestQueue queue) {
        this.queue = queue;
    }

    // TODO just wrap ErrorListener and pipe ok=false reason=network error

    public JsonObjectRequest call(String method, JSONObject req, final Response.Listener<JSONObject> f) {
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JSONObject response = new JSONObject();

                try {
                    Log.d("dnslearning", "ERROR" + response.toString());
                    response.putOpt("error", "Network error");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v("Error", error.toString());
                f.onResponse(response);
                return;
            }
        };

        String url = baseUrl + "/api/" + method;
        JsonObjectRequest r = new JsonObjectRequest(url, req, f, errorListener);
        queue.add(r);
        return r;
    }
}
