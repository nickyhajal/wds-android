package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.os.Handler;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Created by nicky on 5/17/15.
 */
public class AttendeeSearcher implements Runnable{
    private MainActivity context;
    private EditText inp;
    private Handler timer;
    private String query = "";

    public AttendeeSearcher(MainActivity context, EditText inp) {
        this.context = context;
        this.inp = inp;
        this.timer = new Handler();
    }

    public void startSearch(String query) {
        this.query = query;
        String[] special = {
            "friends",
            "friended me",
            "match me"
        };
        if(Arrays.asList(special).contains(query)) {
            Puts.i("SPECIAL");
            specialSearch();
        }
        else {
            this.timer.removeCallbacks(this);
            this.timer.postDelayed(this, 750);
        }
    }

    public void specialSearch() {
        JSONObject params = new JSONObject();
        if (this.query.length() > 0) {
            try {
                try {
                    params.put("type", URLEncoder.encode(this.query, "utf-8"));
                    params.put("include_user", "1");
                } catch (UnsupportedEncodingException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Api.get("user/friends_by_type", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    try {
                        context.update_search(rsp.getJSONArray("user"));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    MainActivity.offlineAlert();
                }
            });
        }
    }

    public void run() {
        final AttendeeSearcher ref = this;
        JSONObject params = new JSONObject();
        if (this.query.length() > 0) {

            try {
                try {
                    params.put("search", URLEncoder.encode(this.query, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Api.get("users", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    try {
                        ref.context.update_search(rsp.getJSONArray("users"));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    MainActivity.offlineAlert();
                }
            });
        }
    }
}
