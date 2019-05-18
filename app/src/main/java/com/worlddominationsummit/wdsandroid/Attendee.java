package com.worlddominationsummit.wdsandroid;

import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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
    public String ticket_type = "";
    public String email;
    public String site;
    public String twitter;
    public String facebook;
    public String instagram;
    public String pic;
    public String pre19 = "0";
    public String location = "";
    public String lat;
    public String lon;
    public String distance = "";
    public String qnaStr;
    public String firetoken;
    public Boolean receivesMsgs = false;
    public JSONObject card;
    public Boolean isQna;
    public Boolean hasPic;
    public String academy = "0";

    public static Attendee fromHashMap(HashMap<String, String> params) {
        return Attendee.fromJson(new JSONObject(params));
    }
    public static Attendee fromJson(JSONObject params) {
        Attendee atn = new Attendee();
        atn.user_id = params.optString("user_id");
        atn.first_name = params.optString("first_name");
        atn.last_name = params.optString("last_name");
        atn.user_name = params.optString("user_name");
        atn.ticket_type = params.optString("ticket_type");
        atn.email = params.optString("email");
        atn.site = params.optString("site");
        atn.twitter = params.optString("twitter");
        atn.facebook = params.optString("facebook");
        atn.instagram = params.optString("instagram");
        atn.pic = params.optString("pic");
        atn.pre19 = params.optString("pre19");
        atn.location = params.optString("location");
        atn.lat = params.optString("lat");
        atn.lon = params.optString("lon");
        atn.distance = params.optString("distance");
        atn.academy = params.optString("academy", "0");
        atn.card = params.optJSONObject("card");
        atn.firetoken = params.optString("firetoken");
        atn.init();
        return atn;
    }

    public Attendee() { }
    public Attendee(String user_id, String first_name, String last_name, String user_name, String email, String ticket_type, String site, String twitter, String facebook, String instagram, String pic, String location, String lat, String lon, String distance, String pre19) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.user_name = user_name;
        this.ticket_type = ticket_type;
        this.email = email;
        this.twitter = twitter;
        this.facebook = facebook;
        this.site = site;
        this.pre19 = pre19;
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

    public void readyForMessages(final Runnable r) {
        final Attendee self = this;
        Fire.get("/users/" + this.user_id + "/version", new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    self.receivesMsgs = ((String) dataSnapshot.getValue()).compareTo("16.3") >= 0;
                    r.run();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
            qs.put(""); // Skip question_id 0
            qs.put("Why did you decide to travel " + this.distance + " miles from " + this.location + " to the World Domination Summit?");
            qs.put("What are you excited about these days?");
            qs.put("What's your super-power?");
            qs.put("What's your goal for WDS 2019?");
            qs.put("What's your favorite song?");
            qs.put("What's your favorite treat?");
            qs.put("What's your favorite beverage?");
            qs.put("What's your favorite quote?");
            qs.put("What are you looking forward to during your time in Portland?");
            qs.put("What is something you'd love help with from the WDS community?");
            qs.put("What is something you can offer to another WDS attendee?");
            qs.put("If you had one wish, what would you wish for?");
            qs.put("What's one project or goal you're working on now?");
            qs.put(""); // Skip question_id 13
            qs.put("What book has had the biggest impact on your life?");
            qs.put("What's your favorite song to get pumped up?");
            qs.put("What's your favorite treat to celebrate a job well-done?");
            qs.put("What's your favorite beverage to kick-back and relax?");
            qs.put("What's your favorite way to contribute to society at large?");

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

    public String qnaHtml() {
        return "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "@font-face {\n" +
                "    font-family: Karla;" +
                "    src: url(\"file:///android_asset/fonts/KarlaRegular.ttf\")" +
                "}"+
                "body {" +
                "    font-family: Karla;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                this.qnaStr +
                "</body>" +
                "</html>";
    }
    public Spanned qna() {
        HtmlSpanner htmlspanner = new HtmlSpanner();
        String str = this.qnaStr.replace("\n", "<br>");
        Spannable text = htmlspanner.fromHtml(str);
        return text;
    }

    public HashMap toSimpleHashMap() {
        HashMap<String, Object> out = new HashMap<>();
        out.put("first_name", first_name);
        out.put("last_name", last_name);
        out.put("user_id", user_id);
        return out;
    }
}
