package com.worlddominationsummit.wds;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by nicky on 5/18/15.
 */
public class Attendee {
    public String user_id;
    public String first_name = "░░░░░░░░░";
    public String last_name = "░░░░░░░";
    public String full_name;
    public String user_name;
    public String email;
    public String site;
    public String twitter;
    public String facebook;
    public String instagram;
    public String pic;
    public String location = "░░░░░░░░";
    public String lat;
    public String lon;
    public String distance = "░░░░░░";
    public String qnaStr;
    public Boolean isQna;
    public Boolean hasPic;

    public static Attendee fromHashMap(HashMap<String, String> params) {
        return Attendee.fromJson(new JSONObject(params));
    }
    public static Attendee fromJson(JSONObject params) {
        Attendee atn = new Attendee();
        atn.user_id = params.optString("user_id");
        atn.first_name = params.optString("first_name");
        atn.last_name = params.optString("last_name");
        atn.user_name = params.optString("user_name");
        atn.email = params.optString("email");
        atn.site = params.optString("site");
        atn.twitter = params.optString("twitter");
        atn.facebook = params.optString("facebook");
        atn.instagram = params.optString("instagram");
        atn.pic = params.optString("pic");
        atn.location = params.optString("location");
        atn.lat = params.optString("lat");
        atn.lon = params.optString("lon");
        atn.distance = params.optString("distance");
        atn.init();
        return atn;
    }

    public Attendee() { }
    public Attendee(String user_id, String first_name, String last_name, String user_name, String email, String site, String twitter, String facebook, String instagram, String pic, String location, String lat, String lon, String distance) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.user_name = user_name;
        this.email = email;
        this.twitter = twitter;
        this.facebook = facebook;
        this.site = site;
        this.instagram = instagram;
        this.pic = pic;
        this.location = location;
        this.lat = lon;
        this.distance = distance;
    }
    public void init() {
        this.full_name = first_name + " " + last_name;
        this.initDistance();
        this.initPic();
    }

    private void initDistance() {
        double distance = Double.parseDouble(this.distance);
        if(this.distance != "") {
            if(distance < 2) {
                distance = 4;
            }
        }
        this.distance = Double.toString(Math.ceil(distance));
    }
    private void initPic() {
        this.hasPic = true;
        if(this.pic == "") {
            this.pic = "/images/default-avatar.png";
            this.hasPic = false;
        }
        if(!this.pic.contains("http")) {
            this.pic = "http://worlddominationsummit.com"+this.pic;
        }
        this.pic = this.pic.replace("_normal", "");
    }
    public String clean_site() {
        return this.site.replace("https://", "").replace("http://", "").replace("www.", "").toLowerCase();
    }


}
