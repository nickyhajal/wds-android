package com.worlddominationsummit.wds;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

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
        it.content = params.optString("content");
        it.num_comments = params.optString("num_comments");
        it.num_likes = params.optString("num_likes");
        it.channel_type = params.optString("channel_type");
        it.channel_id = params.optString("channel_id");
        it.created_at_str = params.optString("created_at");
        it.author = Attendee.fromJson(params);
        if (it.created_at_str != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                it.created_at = formatter.parse(it.created_at_str).getTime();
            } catch (ParseException e) {
                Log.e("WDS", "Parse Exception", e);
            }

        }
        it.channel = it.channel_type;
        if (it.channel.equals("interest")) {
            JSONArray ints = Store.getJsonArray("interests");
            int len = ints.length();
            for(int i = 0; i < len; i++) {
                JSONObject interest = new JSONObject();
                try {
                    interest = ints.getJSONObject(i);
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
                if (it.channel_id.equals(interest.optString("interest_id"))) {
                    it.channel = interest.optString("interest");
                }
            }
        }
        it.initNums();
        return it;
    }

    public DispatchItem() { }

    public void initNums() {
        int num_l = Integer.parseInt(this.num_likes);
        int num_c = Integer.parseInt(this.num_comments);
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
