package com.worlddominationsummit.wds;

import android.support.v4.app.Fragment;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicky on 5/24/15.
 */
public class Dispatch {
    public Fragment context;
    public JSONObject mFilters;
    public JSONObject mParams;
    public int mSince;

    public void initParams(JSONObject params) {
        mSince = 0;
        mParams = new JSONObject();
        try {
            mParams.put("include_author", "1");
            if (params.has("channel_type")) {
                Puts.i("HAS CHANNEL");
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
        Puts.i(mParams.toString());
    }
    public void initFilters() {
        JSONObject filters = Store.getJsonObject("dispatch_filters");
        if (!filters.has("twitter")) {
            try {
                filters.put("twitter", "1");
                filters.put("following" , "1");
                filters.put("communities" , "0");
                filters.put("meetups" , "0");
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        setFilters(filters);
    }
    public void setFilters(JSONObject filters) {
        mFilters = filters;
    }
    public void setChannel(String channel_type) {
        setChannel(channel_type, "");
    }
    public void setChannel(String channel_type, String channel_id) {
        mSince = 0;
        try {
            mParams.put("channel_type", channel_type);
            if (channel_id.equals("")) {
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
        fetch(successListener, errorListener, "");
    }
    public void fetch(com.android.volley.Response.Listener<JSONObject> successListener, com.android.volley.Response.ErrorListener errorListener, String before) {
        JSONObject params = mParams;
        try {
            params.put("filters", mFilters);
            params.put("since", mSince);
            if (!before.equals("")) {
                params.put("before", before);
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.get("feed", params, successListener, errorListener);
    }
}
