package com.worlddominationsummit.wds;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.os.Handler;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by nicky on 5/17/15.
 */
public class AttendeeSearcher implements Runnable{
    private MainActivity context;
    private EditText inp;
    private Handler timer;
    private String query;

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

        }
        else {
            this.timer.removeCallbacks(this);
            this.timer.postDelayed(this, 750);
        }
    }

    public void run() {
        final AttendeeSearcher ref = this;
        JSONObject params = new JSONObject();
        try {
            params.put("search", this.query);
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
                Puts.i("Search problem");
            }
        });
    }
}
