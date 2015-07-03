package com.worlddominationsummit.wdsandroid;

import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by nicky on 5/18/15.
 */
public class Attendee {
    public static String default_avatar = "http://worlddominationsummit.com/images/default-avatar.png";
    public String user_id;
    public String first_name = "";
    public String last_name = "";
    public String full_name;
    public String user_name;
    public String email;
    public String site;
    public String twitter;
    public String facebook;
    public String instagram;
    public String pic;
    public String location = "";
    public String lat;
    public String lon;
    public String distance = "";
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
        if (this.distance != null && !this.distance.equals("")) {
            double distance = Double.parseDouble(this.distance);
            if (distance < 2) {
                distance = 4;
            }
            this.distance = Double.toString(Math.ceil(distance));
        }
    }
    private void initPic() {
        this.hasPic = true;
        this.pic = Avatar.url(this.user_id, 400);
    }
    public String getPic(int size) {
        return Avatar.url(this.user_id, size);
    }
    public String clean_site() {
        return this.site.replace("https://", "").replace("http://", "").replace("www.", "").toLowerCase();
    }

    public void initQna(JSONArray answers) {
        this.qnaStr = "";
        int len = answers.length();
        if (len > 0) {
            String str = "<style text='text/css'>" +
                    "@font-face {" +
                    " font-family: 'Karla';" +
                    " src: url('/assets/fonts/KarlaRegular.ttf')" +
                    "}" +
                    "body { margin: 0; padding:0 40px 0 0; background:#FCFCFA; font-family: Karla; font-size:15px; }" +
                    "      a { color: #E99533; text-decoration: underline; }" +
                    "    </style><body>";
            JSONArray qs = new JSONArray();
            qs.put("");
            qs.put("Why did you decide to travel " + this.distance + " miles from " + this.location + " to the World Domination Summit?");
            qs.put("What are you excited about these days?");
            qs.put("What's your super-power?");
            qs.put("What's your goal for WDS 2015?");
            for (int i = 0; i < len; i++) {
                String q = "";
                JSONObject answer = answers.optJSONObject(i);
                try {
                    q = qs.getString(answer.optInt("question_id"));
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
                str += "<b style='margin-bottom:5px'>"+q+"</b><div style='margin-bottom:15px'>"+answer.optString("answer")+"</div>";
            }
            str += "</body>";
            this.qnaStr = str;
        }
    }

    public Spanned qna() {
        HtmlSpanner htmlspanner = new HtmlSpanner();
        String str = this.qnaStr.replace("\n", "<br>");
        Spannable text = htmlspanner.fromHtml(str);
        return text;
    }
}