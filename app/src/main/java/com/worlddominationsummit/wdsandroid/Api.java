package com.worlddominationsummit.wdsandroid;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import android.content.Context;
import android.util.Log;


/**
 * Created by nicky on 5/17/15.
 */
public class Api {

    public static RequestQueue queue;

//    private static String url = "http://wds.nky";
    private static String url = "http://worlddominationsummit.com";

    public static void init(Context context){
        Api.queue = Volley.newRequestQueue(context);
    }

    public static void delete(String path, JSONObject raw_params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Api.request(Request.Method.DELETE, path, raw_params, successListener, errorListener);
    }

    public static void post(String path, JSONObject raw_params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Api.request(Request.Method.POST, path, raw_params, successListener, errorListener);
    }

    public static void get(String path, JSONObject raw_params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Api.request(Request.Method.GET, path, raw_params, successListener, errorListener);
    }

    public static void request(int method, String path, JSONObject params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        String url = Api.url+"/api/"+path;
        if(Me.user_token != null && Me.user_token.length() > 0) {
            if (params == null) {
                params = new JSONObject();
            }
            try {
                params.put("user_token", Me.user_token);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        if((method == Request.Method.GET || method == Request.Method.DELETE) && params != null) {
            url += "?"+JsonHelper.UrlEncode(params);
        }
        Puts.i(url);
        JsonObjectRequest request = new JsonObjectRequest(method, url, params, successListener, errorListener);
        Api.queue.add(request);
    }
}
