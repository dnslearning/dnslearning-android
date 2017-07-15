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
    private String baseUrl = "http://kris.smartmadre.com";
    private RequestQueue queue;

    public DNSLearningAPI(RequestQueue queue) {
        this.queue = queue;
    }

    // TODO just wrap ErrorListener and pipe ok=false reason=network error

    public JsonObjectRequest call(String method, JSONObject req, Response.Listener<JSONObject> f) {
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Error", error.toString());
            }
        };

        String url = baseUrl + "/api?method=" + method;
        JsonObjectRequest r = new JsonObjectRequest(url, req, f, errorListener);
        queue.add(r);
        return r;
    }
}
