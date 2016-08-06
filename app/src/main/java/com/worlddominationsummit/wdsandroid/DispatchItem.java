package com.worlddominationsummit.wdsandroid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Created by nicky on 5/18/15.
 */
public class DispatchItem {
    public String feed_id;
    public String content = "Loading";
    public String num_comments = "░░";
    public String num_likes = "░░";
    public String channel_type = "loading";
    public String channel_id = "loading";
    public String channel = "loading";
    public String created_at_str = "loading";
    public String num_likes_str = "loading";
    public String num_comments_str = "loading";
    public long created_at;
    public Attendee author = new Attendee();

    public static DispatchItem fromHashMap(HashMap<String, String> params) {
        return DispatchItem.fromJson(new JSONObject(params));
    }
    public static DispatchItem fromJson(JSONObject params) {
        DispatchItem it = new DispatchItem();
        it.feed_id = params.optString("feed_id");
        if (params.has("content")) {
            it.content = params.optString("content");
        } else if (params.has("comment")) {
            it.content = params.optString("comment");
        }
        it.num_comments = params.optString("num_comments");
        it.num_likes = params.optString("num_likes");
        it.channel_type = params.optString("channel_type");
        it.channel_id = params.optString("channel_id");
        it.created_at_str = params.optString("created_at");
        it.author = Attendee.fromJson(params);
        if (it.created_at_str != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                TimeZone tz = TimeZone.getDefault();
                Date now = new Date();
                long offsetFromUtc = tz.getOffset(now.getTime());
                it.created_at = formatter.parse(it.created_at_str).getTime() + offsetFromUtc - 3000;
            } catch (ParseException e) {
                Log.e("WDS", "Parse Exception", e);
            }

        }
        it.channel = it.channel_type;
        if (it.channel != null && it.channel.length() > 0) {
            if (it.channel.equals("interest")) {
                it.channel = Dispatch.getCommunityFromInterest(it.channel_id);
            } else if (it.channel.equals("event") || EventTypes.list.toString().contains(it.channel)) {
//                Puts.i(it.channel_id);
                JSONObject event = Dispatch.getEventFromEventId(it.channel_id);
                if (event != null) {
                    if (EventTypes.types.toString().contains(event.optString("type"))) {
                        String type = EventTypes.byId.optJSONObject(event.optString("type")).optString("singular");
                        String name = event.optString("what");
                        if (name.length() > 40) {
                            name = name.substring(0, 37)+"...";
                        }
                        it.channel = type + ": " + name;
                    }
                }
            }
        }
        it.initNums();
        return it;
    }

    public DispatchItem() { }

    public void initNums() {
        int num_l = 0;
        int num_c = 0;
        if (this.num_likes.length() > 0) {
            num_l = Integer.parseInt(this.num_likes);
        }
        if (this.num_comments.length() > 0) {
            num_c = Integer.parseInt(this.num_comments);
        }
        this.num_likes_str = "Like";
        this.num_comments_str = "Comment";
        if (num_l == 1) {
            this.num_likes_str = "1 Like";
        } else if (num_l > 1) {
            this.num_likes_str = this.num_likes+" Likes";
        }
        if (Me.likesFeedItem(this.feed_id)) {
            this.num_likes_str += " | Liked!";
        }
        if (num_c == 1) {
            this.num_comments_str = "1 Comment";
        } else if (num_c > 1) {
            this.num_comments_str = this.num_comments+" Comments";
        }
    }
}
