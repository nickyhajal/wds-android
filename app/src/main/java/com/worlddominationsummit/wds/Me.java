/**
 * Created by nicky on 5/17/15.
 */

package com.worlddominationsummit.wds;

import android.util.Log;
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
            Log.e("WDS", "JSON Exception", e);
            return "";
        }
    }
    public static JSONArray getJSONArray (String key) {
        try {
            return Me.params.getJSONArray(key);
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
                        Puts.i("token valid");
                        Me.context.open_tabs();
                    }
                    else {
                        Puts.i("token not valid");
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

    public static int checkWalkthrough() {
        String step = Store.get("walkthrough");
        return step.equals("") ? 0 : Integer.parseInt(step);
    }

    public static void saveUserToken(String user_token) {
        Store.set("user_token", user_token);
        Me.checkLoggedIn();
    }

    public static boolean isFriend(int user_id) {
        try {
            JSONArray friends = new JSONArray(Me.get("connections"));
            int len = friends.length();
            for (int i=0; i < len; i++){
                if(friends.getInt(i) == user_id) {
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

    public static void toggleLike(String feed_id, final Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        final String fid = feed_id;
        JSONObject params = new JSONObject();
        try {
            params.put("feed_id", feed_id);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        if (!Me.likesFeedItem(feed_id)) {
            Puts.i("POST LIKE");
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

    public static boolean isInterested(int interest_id) {
        try {
            JSONArray ints = new JSONArray(Me.get("interests"));
            int len = ints.length();
            for (int i=0; i < len; i++){
                if(ints.getInt(i) == interest_id) {
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
            return true;
        }
        else {
            try {
                JSONArray rsvps = new JSONArray(Me.get("rsvps"));
                int len = rsvps.length();
                for (int i = 0; i < len; i++) {
                    if (rsvps.getString(i).equals(event.get("event_id"))) {
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

    public static boolean isAttendingEvent(Event event) {
        if(event.type == "program") {
            return true;
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
                    JsonHelper.deleteVal(rsvps, event.event_id);
                }
                else {
                    rsvps.put(event.event_id);
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


//    public static boolean isInterestedInEvent(Event event) {
//        try {
//            JSONArray ints = new JSONArray(Me.get("interests"));
//            int ints_len = ints.length();
//
//        } catch (JSONException e) {
//            Log.e("WDS", "JSON Exception", e);
//            return false;
//        }
//    }


}
