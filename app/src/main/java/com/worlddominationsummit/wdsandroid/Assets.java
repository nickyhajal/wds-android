package com.worlddominationsummit.wdsandroid;

import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
                        if (key.equals("me")) {
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
                        track(key);
                    }
                }
                finalSuccessListener.onResponse(rsp);
                Log.i("WDS", rsp.toString());
            }
        }, errorListener);
    }

    public static void getSmart(final String asset, final Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        JSONObject _tracker = tracker();
        Boolean doPull = false;
        String pullAsset = asset;
        JSONArray existing = Store.getJsonArray(asset);
        JSONObject rsp = new JSONObject();
        if (asset.equals("meetups") || asset.equals("academy") || asset.equals("spark_session") || asset.equals("activity")) {
            pullAsset = "events";
        }
        Puts.i(pullAsset);
        long tracked = 0;
        try {
            tracked = _tracker.getLong(pullAsset);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        long now = System.currentTimeMillis() / 1000L;
        int diff =(int) (now - tracked)/60;
        if (existing.length() > 0) {
            try {
                rsp.put("data", existing);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            successListener.onResponse(rsp);
            if (diff > Assets.expires.optInt(pullAsset)) {
                doPull = true;
            }
        } else {
            doPull = true;
        }
        if(doPull) {
            Assets.pull(pullAsset, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    JSONArray pulled = Store.getJsonArray(asset);
                    JSONObject rsp = new JSONObject();
                    try {
                        rsp.put("data", pulled);
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    successListener.onResponse(rsp);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
        }
    }

    public static JSONObject tracker() {
        return Store.getJsonObject("tracker");
    }

    public static void track(String asset) {
        JSONObject _tracker = tracker();
        long now = System.currentTimeMillis() / 1000L;
        try {
            _tracker.put(asset, now);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Store.set("tracker", _tracker);
    }


    public static void process_me(JSONObject rsp) {
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
        JSONArray allowed_events = new JSONArray();
        int ev_length = events.length();
        JSONObject types = new JSONObject();
        for (int t = 0; t < EventTypes.list.length(); t++ ) {
            try {
                JSONObject e = EventTypes.list.getJSONObject(t);
                types.put(e.getString("id"), new JSONArray());
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        for (int i = 0; i < ev_length; i++) {
            try {
                JSONObject event = events.getJSONObject(i);
                if (Me.hasPermissionForEvent(event)) {
                    allowed_events.put(event);
                    for (int t = 0; t < EventTypes.list.length(); t++ ) {
                        JSONObject e = EventTypes.list.getJSONObject(t);
                        String type = e.getString("id");
                        if (events.getJSONObject(i).getString("type").equals(type)) {
                            JSONArray evsOfType = types.getJSONArray(type);
                            evsOfType.put(events.getJSONObject(i));
                            types.put(type, evsOfType);
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception --", e);
            }
        }
        Store.set("events", allowed_events);
        Iterator<?> keys = types.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONArray evsOfType = new JSONArray();
            try {
                evsOfType = types.getJSONArray(key);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Store.set(key, evsOfType);
        }
    }
}
