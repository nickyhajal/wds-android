package com.worlddominationsummit.wdsandroid;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicky on 5/24/15.
 */
public class Dispatch {
    public Fragment context;
    public JSONObject mFilters;
    public JSONObject mParams;
    public String mSince;
    public Boolean mNoMoreBefore;

    public void initParams(JSONObject params) {
        mSince = "0";
        mNoMoreBefore = false;
        mParams = new JSONObject();
        try {
            mParams.put("include_author", "1");
            if (params.has("channel_type")) {
                mParams.put("channel_type", params.getString("channel_type"));
            }
            if (params.has("channel_id")) {
                mParams.put("channel_id", params.getString("channel_id"));
            }
            if (params.has("user_id")) {
                mParams.put("user_id", params.getString("user_id"));
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
    }
    public void initFilters() {
        JSONObject filters = Store.getJsonObject("dispatch_filters");
        if (!filters.has("twitter")) {
            try {
                filters.put("twitter", "1");
                filters.put("following" , "0");
                filters.put("communities" , "0");
                filters.put("meetups" , "1");
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        setFilters(filters);
    }
    public void setFilter(String name, String val) {
        try {
            mFilters.put(name, val);
            MainActivity.self.filtersFragment.setFilters(mFilters);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
    }
    public void setFilters(JSONObject filters) {
        mFilters = filters;
        MainActivity.self.filtersFragment.setFilters(mFilters);
    }
    public void setChannel(String channel_type) {
        setChannel(channel_type, "");
    }
    public void setChannel(String channel_type, String channel_id) {
        mSince = "0";
        mNoMoreBefore = false;
        try {
            mParams.put("channel_type", channel_type);
            if (!channel_id.equals("")) {
                mParams.put("channel_id", channel_id);
            }
            else {
                mParams.remove("channel_id");
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
    }
    public void leaveChannel(){
        setChannel("global");
    }
    public void fetch(com.android.volley.Response.Listener<JSONObject> successListener, com.android.volley.Response.ErrorListener errorListener) {
        fetch("", successListener, errorListener);
    }
    public void fetch(final String before, final com.android.volley.Response.Listener<JSONObject> successListener, com.android.volley.Response.ErrorListener errorListener) {
        JSONObject params = mParams;
        Boolean halt = false;
        try {
            params.put("filters", mFilters);
            params.put("since", mSince);
            if (!before.equals("")) {
                halt = mNoMoreBefore;
                params.put("before", before);
            }
            else {
                params.remove("before");
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        if (!halt) {
            Api.get("feed", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    if (!before.equals("")) {
                        JSONArray feedContents = jsonObject.optJSONArray("feed_contents");
                        if (feedContents.length() == 0) {
                            mNoMoreBefore = true;
                        }
                    }
                    successListener.onResponse(jsonObject);
                }
            }, errorListener);
        }
    }
    public void post(String text, Response.Listener successListener, Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        try {
            params.put("content", text);
            params.put("channel_type", mParams.get("channel_type"));
            if(mParams.has("channel_id")) {
                params.put("channel_id", mParams.get("channel_id"));
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("feed", params, successListener, errorListener);
    }
    public static String getCommunityFromInterest(String interest_id) {
        JSONArray ints = Store.getJsonArray("interests");
        int len = ints.length();
        for(int i = 0; i < len; i++) {
            JSONObject interest = new JSONObject();
            try {
                interest = ints.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            if (interest_id.equals(interest.optString("interest_id"))) {
                return interest.optString("interest");
            }
        }
        return "";
    }
    public static String getMeetupNameFromEventId(String event_id) {
        JSONArray ints = Store.getJsonArray("meetups");
        int len = ints.length();
        for(int i = 0; i < len; i++) {
            JSONObject meetup = new JSONObject();
            try {
                meetup = ints.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            if (event_id.equals(meetup.optString("event_id"))) {
                return meetup.optString("what");
            }
        }
        return "";
    }
}
