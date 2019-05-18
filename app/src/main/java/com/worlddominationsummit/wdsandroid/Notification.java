package com.worlddominationsummit.wdsandroid;

import android.os.Handler;
import android.util.Log;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nicky on 8/10/16.
 */
public class Notification {

    public String notificationId;
    public String user_id;
    public HashMap from;
    public String text;
    public String type;
    public String content;
    public String channel_type;
    public String channel_id;
    public String link;
    public String clicked;
    public String read;
    public String created_at;
    public String updated_at;

    public static Notification fromJson(JSONObject n) {
        Notification nn = new Notification();
        nn.notificationId = n.optString("notification_id");
        nn.user_id = n.optString("user_id");
//        nn.from = n.optString("from");
        nn.text = n.optString("text");
        nn.type = n.optString("type");
        nn.content = n.optString("content");
        nn.channel_type = n.optString("channel_type");
        nn.channel_id = n.optString("channel_id");
        nn.link = n.optString("link");
        nn.clicked = n.optString("clicked");
        nn.read = n.optString("read");
        nn.created_at = n.optString("created_at");
        nn.updated_at = n.optString("updated_at");
        return nn;
    }

    public static Notification fromHashMap(HashMap<String, Object> n) {
        Notification nn = new Notification();
        nn.notificationId = String.valueOf(n.get("notification_id"));
        nn.user_id = String.valueOf(n.get("user_id"));
        nn.from = (HashMap) n.get("from");
        nn.text = (String) n.get("text");
        nn.type = (String) n.get("type");
        nn.content = (String)n.get("content");
        nn.channel_type = (String) n.get("channel_type");
        nn.channel_id = (String) n.get("channel_id");
        nn.link = (String) n.get("link");
        nn.clicked = (String) n.get("clicked");
        nn.read = (String) n.get("read");
        nn.created_at = (String) n.get("created_at");
        nn.updated_at = (String) n.get("updated_at");
        return nn;
    }

    public void open() {
        JSONObject cont = new JSONObject();
        try {
            cont = new JSONObject(this.content);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        final String link = this.link;
        final String from_id = cont.optString("from_id");
        if (link.substring(0,1).equals("~")) {
            Attendee atn = new Attendee();
            atn.user_id = from_id;
            MainActivity.self.open_profile(atn);
        } else if (link.contains("dispatch")) {
            String[] bits = link.split("/");
            String id = bits[bits.length - 1];
            JSONObject params = new JSONObject();
            try {
                params.put("channel_id", id);
                params.put("channel_type", "feed_item");
                params.put("include_author", true);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Api.get("feed", params, new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    try {
                        JSONObject jsonItem = rsp.getJSONArray("feed_contents").getJSONObject(0);
                        HashMap item = (HashMap) JsonHelper.toMap(jsonItem);
                        MainActivity.self.open_dispatch_item(item);
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
        } else if (link.contains("message")) {
            String[] bits = link.split("/");
            String id = bits[bits.length - 1];
            MainActivity.self.open_chat(id);
        }
        JSONObject params = new JSONObject();
        JSONArray ns = new JSONArray();
        JSONObject n = new JSONObject();
        try {
            n.put("notification_id", notificationId);
            n.put("clicked", "1");
            ns.put(n);
            params.put("notifications", ns);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("user/notifications", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                MainActivity.self.notificationFragment.sync();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }


}
