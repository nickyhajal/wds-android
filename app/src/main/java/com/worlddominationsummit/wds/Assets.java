package com.worlddominationsummit.wds;

import android.util.Log;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nicky on 5/18/15.
 */
public class Assets {
    public static MainActivity context;
    public static String core;
    public static JSONObject expires;
    public static void init(MainActivity context) {
        Assets.context = context;
        Assets.core = "me,events,interests,places";
        Assets.expires = new JSONObject();
        try {
            Assets.expires.put("me", 0);
            Assets.expires.put("events", 5);
            Assets.expires.put("interests", 300);
            Assets.expires.put("places", 300);
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
        }
    }

    public static void sync(Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Assets.pull(Assets.core, successListener, errorListener);
    }

    public static void pull(String includeStr, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        final Response.Listener<JSONObject> finalSuccessListener = successListener;
        final JSONArray include = new JSONArray(Arrays.asList(includeStr.split(",")));
        JSONObject params = new JSONObject();
        try {
            params.put("assets", includeStr);
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
        }
        Api.get("assets", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                int inc_len = include.length();
                for (int i = 0; i < inc_len; i++) {
                    String key = "";
                    try {
                        key = include.getString(i);
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    if (rsp.has(key)) {
                        Puts.i(key);
                        if (key.equals("me")) {
                            Puts.i("LETS DO ME");
                            try {
                                Assets.process_me(rsp.getJSONObject(key));
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        } else if (key.equals("events")) {
                            try {
                                Assets.process_events(rsp.getJSONArray(key));
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        } else if (key.equals("interests")) {
                            try {
                                Store.set("interests", rsp.getJSONArray(key));
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        } else if (key.equals("places")) {
                            try {
                                Store.set("places", rsp.getJSONArray(key));
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        }
                    }
                }
                finalSuccessListener.onResponse(rsp);
                Log.i("WDS", rsp.toString());
            }
        }, errorListener);
    }

    public static void process_me(JSONObject rsp) {
        Puts.i(">>>>>>> UPDATE ME!!");
        Me.update(rsp);
        Store.set("me", rsp);
    }
    public static void process_events(JSONArray events) {
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < events.length(); i++) {
            try {
                jsonValues.add(events.getJSONObject(i));
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            public int compare(JSONObject o1, JSONObject o2) {
                return o1.optString("start").compareTo(o2.optString("start"));
            }
        });
        events = new JSONArray(jsonValues);
        int ev_length = events.length();
        JSONArray meetups = new JSONArray();
        for (int i = 0; i < ev_length; i++) {
            try {
                if (events.getJSONObject(i).getString("type").equals("meetup")) {
                    meetups.put(events.getJSONObject(i));
                }
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception --", e);
            }
        }
        Store.set("events", events);
        Store.set("meetups", meetups);
    }
}
