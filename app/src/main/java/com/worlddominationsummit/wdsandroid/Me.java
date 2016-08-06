/**
 * Created by nicky on 5/17/15.
 */

package com.worlddominationsummit.wdsandroid;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Me {

    private static MainActivity context;
    public static String user_token;
    private static JSONObject params;
    public static Attendee atn;

    public static void init(MainActivity context) {
        Me.context = context;
        Me.user_token = "";
        String me = Store.get("me", false);
        if(me.length() > 0) {
            try {
                Me.update(new JSONObject(me));
            } catch (JSONException e) {
                Log.e("WDS", "JSON Exception", e);
            }
        }
    }

    public static void update(JSONObject params) {
        Me.atn = Attendee.fromJson(params);
        Me.params = params;
    }

    public static void sync (Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Assets.pull("me", successListener, errorListener);
    }

    public static String get (String key) {
        try {
            return Me.params.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }
    public static JSONArray getJSONArray (String key) {
        try {
            return new JSONArray(Me.params.get(key).toString());
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
            return new JSONArray();
        }
    }

    public static void set (String key, String val) {
        try {
            Me.params.put(key, val);
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
        }
    }

    public static void set (String key, HashMap val) {
        try {
            Me.params.put(key, new JSONObject(val).toString());
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
        }
    }

    public static void set (String key, JSONObject val) {
        try {
            Me.params.put(key, val.toString());
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
        }
    }

    public static void set (String key, JSONArray val) {
        try {
            Me.params.put(key, val.toString());
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
        }
    }

    public static void checkLoggedIn() {
        Me.user_token = Store.get("user_token");
        if(!Me.user_token.equals("")) {
            Api.get("user/validate", null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    Log.i("WDS", rsp.toString());
                    if(rsp.has("valid")) {
                        Me.context.open_tabs();
                        Me.checkDeviceRegistered();
                    }
                    else {
                        Me.context.open_login();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Puts.i("connect err");
                    Me.context.open_tabs();
                }
            });
        }
        else {
            Puts.i("not token");
            Me.context.open_login();
        }
    }

    public static void checkDeviceRegistered() {
        String registered = Store.get("saved_device_token");
        if (registered.length() == 0) {
            Intent intent = new Intent(MainActivity.self, MyGcmRegistrationService.class);
            MainActivity.self.startService(intent);
        }
    }

    public static int checkWalkthrough() {
        String step = Store.get("walkthrough");
        return step.equals("") ? 0 : Integer.parseInt(step);
    }

    public static boolean claimedAcademy() {
        return !Me.atn.academy.equals("0");
    }

    public static void claimAcademy(final String event_id, final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener) {
        JSONObject p = new JSONObject();
        try {
            p.put("event_id", event_id);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("event/claim-academy", p, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Me.addRsvp(event_id);
                successListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorListener.onErrorResponse(error);
            }
        });
    }

    public static void saveUserToken(String user_token) {
        Store.set("user_token", user_token);
        Me.checkLoggedIn();
    }

    public static boolean isFriend(String user_id) {
        try {
            JSONArray friends = new JSONArray(Me.get("connected_ids"));
            int len = friends.length();
            for (int i=0; i < len; i++){
                if(friends.getString(i).equals(user_id)) {
                    return true;
                }
            }
            return false;
        }
        catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
            return false;
        }
    }

    public static boolean likesFeedItem(String item_id) {
        try {
            JSONArray likes = new JSONArray(Me.get("feed_likes"));
            int len = likes.length();
            for (int i=0; i < len; i++){
                if(likes.getString(i).equals(item_id)) {
                    return true;
                }
            }
            return false;
        }
        catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
            return false;
        }
    }

    public static void addFriend(String user_id) {
        JSONArray friends = new JSONArray();
        try {
            friends = new JSONArray(Me.get("connected_ids"));
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        friends.put(Integer.parseInt(user_id));
        Me.set("connected_ids", friends);
    }
    public static void removeFriend(String user_id) {
        JSONArray tmp = new JSONArray();
        JSONArray friends = new JSONArray();
        try {
            friends = new JSONArray(Me.get("connected_ids"));
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        int len = friends.length();
        for(int i = 0; i < len; i++) {
            if(!(friends.optInt(i) == Integer.parseInt(user_id))) {
                try {
                    tmp.put(friends.get(i));
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        }
        Me.set("connected_ids", tmp);
    }

    public static void toggleLike(String feed_id, final Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        final String fid = feed_id;
        JSONObject params = new JSONObject();
        try {
            params.put("feed_id", feed_id);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        if (!Me.likesFeedItem(feed_id)) {
            Api.post("feed/like", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    JSONArray feed_likes = new JSONArray();
                    try {
                        feed_likes = new JSONArray(Me.get("feed_likes"));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    feed_likes.put(fid);
                    Me.set("feed_likes", feed_likes);
                    successListener.onResponse(rsp);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Server error response
                }
            });
        }
        else {
            Api.delete("feed/like", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    JSONArray tmp = new JSONArray();
                    JSONArray feed_likes = new JSONArray();
                    try {
                        feed_likes = new JSONArray(Me.get("feed_likes"));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    int len = feed_likes.length();
                    for(int i = 0; i < len; i++) {
                        if(!feed_likes.optString(i).equals(fid)) {
                            try {
                                tmp.put(feed_likes.get(i));
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        }
                    }
                    Me.set("feed_likes", feed_likes);
                    successListener.onResponse(rsp);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Server error response
                }
            });
        }
    }

    public static void joinCommunity(final String interest_id, final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        try {
            params.put("interest_id", interest_id);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        if (!Me.isInterested(interest_id)) {
            Api.post("user/interest", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    JSONArray ints = new JSONArray();
                    try {
                        ints = new JSONArray(Me.get("interests"));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    ints.put(interest_id);
                    Me.set("interests", ints);
                    successListener.onResponse(rsp);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    errorListener.onErrorResponse(error);
                    // TODO: Server error response
                }
            });
        }
        else {
            Api.delete("user/interest", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    JSONArray tmp = new JSONArray();
                    JSONArray ints = new JSONArray();
                    try {
                        ints = new JSONArray(Me.get("interests"));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    int len = ints.length();
                    for(int i = 0; i < len; i++) {
                        if(!String.valueOf(ints.optString(i)).equals(interest_id)) {
                            try {
                                tmp.put(String.valueOf(ints.get(i)));
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        }
                    }
                    Me.set("interests", tmp);
                    successListener.onResponse(rsp);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Server error response
                    errorListener.onErrorResponse(error);
                }
            });
        }
    }

    public static boolean isInterested(String interest_id) {
        try {
            JSONArray ints = new JSONArray(Me.get("interests"));
            int len = ints.length();
            for (int i=0; i < len; i++){
                if(ints.getString(i).equals(interest_id)) {
                    return true;
                }
            }
            return false;
        }
        catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
            return false;
        }
    }

    public static boolean isAttendingEvent(HashMap<String, String> event) {
        if(event.get("type").equals("program")) {
            return Me.hasPermissionForEvent(event);
        }
        else {
            try {
                JSONArray rsvps = new JSONArray(Me.get("rsvps"));
                int len = rsvps.length();
                for (int i = 0; i < len; i++) {
                    if (rsvps.getString(i).equals(String.valueOf(event.get("event_id")))) {
                        return true;
                    }
                }
                return false;
            } catch (JSONException e) {
                Log.e("WDS", "JSON Exception", e);
                return false;
            }
        }
    }

    public static boolean hasPermissionForEvent(JSONObject event) {
        try {
            return Me.hasPermissionForEvent((HashMap) JsonHelper.toMap(event));
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        return false;
    }
    public static boolean hasPermissionForEvent(HashMap<String, String> event) {
        String ftype = event.get("for_type");
        if (ftype == null) {
            ftype = "all";
        }
        return ftype.equals("all") || ftype.equals(Me.atn.ticket_type);
    }
    public static boolean hasPermissionForEvent(Event event) {
        String ftype = event.for_type;
        if (ftype == null) {
            ftype = "all";
        }
        return ftype.equals("all") || ftype.equals(Me.atn.ticket_type);
    }

    public static boolean isAttendingEvent(Event event) {
        if(event.type.equals("program")) {
            return Me.hasPermissionForEvent(event);
        }
        else {
            try {
                JSONArray rsvps = new JSONArray(Me.get("rsvps"));
                int len = rsvps.length();
                for (int i = 0; i < len; i++) {
                    if (rsvps.getInt(i) == Integer.parseInt(event.event_id)) {
                        return true;
                    }
                }
                return false;
            } catch (JSONException e) {
                Log.e("WDS", "JSON Exception", e);
                return false;
            }
        }
    }
    public static void toggleRsvp(final Event event, final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener) {
        String event_id = String.valueOf(event.event_id);
        JSONObject params = new JSONObject();
        try {
            params.put("event_id", event_id);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("event/rsvp", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                JSONArray rsvps = Me.getJSONArray("rsvps");
                if(Me.isAttendingEvent(event)) {
                    rsvps = JsonHelper.deleteVal(rsvps, event.event_id);
                }
                else {
                    rsvps.put(Integer.valueOf(event.event_id));
                }
                Me.set("rsvps", rsvps);
                successListener.onResponse(rsp);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorListener.onErrorResponse(error);
            }
        });
    }

    public static void addRsvp(String event_id) {
        JSONArray rsvps = Me.getJSONArray("rsvps");
        rsvps.put(Integer.valueOf(event_id));
        Me.set("rsvps", rsvps);
    }


    public static JSONArray isInterestedInEvent(Event event) {
        try {
            JSONArray all_ints = Store.getJsonArray("interests");
            String intStr = Me.get("interests");
            JSONArray my_ints = new JSONArray();
            if (intStr.equals("")) {
                intStr = "[]";
            }
            try {
                my_ints = new JSONArray(intStr);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            JSONArray ev_ints = event.ints;
            int my_len = my_ints.length();
            int ev_len = ev_ints.length();
            int all_len = all_ints.length();
            JSONArray interested = new JSONArray();
            for(int i = 0; i < my_len; i++) {
                String my_int_id = my_ints.getString(i);
                for(int j = 0; j < ev_len; j++) {
                    String ev_int_id = ev_ints.getString(j);
                    if (ev_int_id.equals(my_int_id) && !Me.isAttendingEvent(event)) {
                        for (int a = 0; a < all_len; a++) {
                            JSONObject intst = all_ints.optJSONObject(a);
                            if(intst != null && intst.optString("interest_id").equals(ev_int_id)) {
                                interested.put(intst.optString("interest"));
                            }
                        }
                    }
                }
            }
            return interested;
        } catch (JSONException e) {
            Log.e("WDS", "JSON Exception", e);
            return new JSONArray();
        }
    }


}
