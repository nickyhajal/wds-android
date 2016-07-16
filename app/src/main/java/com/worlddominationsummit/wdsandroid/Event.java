package com.worlddominationsummit.wdsandroid;
/**
 * Created by nicky on 5/18/15.
 */

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import net.nightwhistler.htmlspanner.HtmlSpanner;

public class Event{
    public String event_id;
    public String slug;
    public String what = "░░░░░░░░░";
    public String who = "░░░░░░░";
    public String place = "░░░░░";
    public String descr = "░░░░░ ░░░░░░░░░░ ░░░░░";
    public String start;
    public String type;
    public String for_type = "all";
    public String format;
    public String lat;
    public String lon;
    public String address;
    public String venueNote = "";
    public String startTime;
    public String startStr;
    public String dayStr;
    public String timeStr;
    public String whoStr;
    public String becauseStr;
    public String max;
    public String num_rsvps;
    public JSONArray ints;
    public JSONArray hostsJSON;
    public ArrayList<Attendee> hosts;
    public JSONArray because;
    public Attendee host;


    public static Event fromHashMap(HashMap<String, String> params) {
        return Event.fromJson(new JSONObject(params));
    }
    public static Event fromJson(JSONObject params) {
        Event ev = new Event();
        ev.event_id = params.optString("event_id");
        ev.slug = params.optString("slug");
        ev.what = params.optString("what");
        ev.who = params.optString("who");
        ev.place = params.optString("place");
        ev.descr = params.optString("descr");
        ev.start = params.optString("start");
        ev.type = params.optString("type");
        ev.format = params.optString("format");
        ev.for_type = params.optString("for_type");
        ev.lat = params.optString("lat");
        ev.lon = params.optString("lon");
        ev.address = params.optString("address");
        ev.venueNote = params.optString("venue_note");
        ev.startTime = params.optString("startTime");
        ev.startStr = params.optString("startStr");
        ev.dayStr = params.optString("dayStr");
        ev.max = params.optString("max");
        ev.num_rsvps = params.optString("num_rsvps");
        try {
            JSONArray hosts = params.getJSONArray("hosts");
            ev.hostsJSON = hosts;
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        try {
            String ints = params.optString("ints");
            ev.ints = new JSONArray(ints);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        try {
            if (ev.hostsJSON.length() > 0) {
                ev.host = Attendee.fromJson(ev.hostsJSON.getJSONObject(0));
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        ev.init();
        return ev;
    }

    public Event() { }
    public Event(String event_id, String slug, String what, String who, String place, String descr, String start, String type, String for_type, String lat, String lon, String address, String startTime, String startStr, String dayStr, JSONArray hostsJSON) {
        this.event_id = event_id;
        this.slug = slug;
        this.what = what;
        this.who = who;
        this.place = place;
        this.descr = descr;
        this.type = type;
        this.for_type = for_type;
        this.start = start;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.startTime = startTime;
        this.startStr = startStr;
        this.dayStr = dayStr;
        this.hostsJSON = hostsJSON;
    }
    public void init() {
        hosts = new ArrayList<Attendee>();
        if(this.hostsJSON != null) {
            int len = this.hostsJSON.length();
            for(int i = 0; i < len; i++) {
                try {
                    hosts.add(Attendee.fromJson(this.hostsJSON.getJSONObject(i)));
                } catch (JSONException e) {
                    Log.e("WDS", "JSON Exception", e);
                }
            }
        }
        try {
            if (this.what != null) {
                this.what = new String(this.what.getBytes("ISO-8859-1"), "UTF-8");
    //            this.what = Html.fromHtml(this.what).toString();
            }
            if (this.who != null) {
                this.who = new String(this.who.getBytes("ISO-8859-1"), "UTF-8");
            }
            if (this.descr != null) {
                this.descr = new String(this.descr.getBytes("ISO-8859-1"), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.timeStr = this.dayStr+" at "+this.startStr;
        if (this.type.equals("academy")) {
        }
        else {
            this.whoStr = "";
            if (this.who != null && this.who.length() > 0) {
                if (EventTypes.byId.has(this.type)) {
                    String typelow = EventTypes.byId.optJSONObject(this.type).optString("singular", "event").toLowerCase();
                    String start = "A "+ typelow;
                    if (typelow.equals("activity")) {
                        start = "An "+typelow;
                    }
                    this.whoStr = start + " for " + this.who.replaceFirst(this.who.substring(0, 1), this.who.substring(0, 1).toLowerCase());
                    if (this.type.equals("meetup") && this.format != null && this.format.length() > 2) {
                        String format = ucfirst(this.format);
                        this.whoStr = format + ": " + this.whoStr;
                    }
                }
            }
        }

    }
    final public static String ucfirst(String subject)
    {
        if (subject != null) {
            return Character.toUpperCase(subject.charAt(0)) + subject.substring(1);
        }
        return "";
    }
    public void setBecause(JSONArray because) {
        String str = "";
        this.because = because;
        int len = because.length();
        for (int i = 0; i < len; i++) {
            str += because.optString(i);
            if (i < len-2) {
                str += ", ";
            }
            else if (i == len - 2) {
                str += " & ";
            }

        }
        this.becauseStr = str;
    }
    public Spanned descrWithHtmlParsed() {
        HtmlSpanner htmlspanner = new HtmlSpanner();
        String descr = this.descr.replace("\n", "<br>");
        Spannable text = htmlspanner.fromHtml(descr);
        return text;
    }
    public Boolean isAttending() {
        return Me.isAttendingEvent(this);
    }

    public Boolean isFull() {
        int m = Integer.parseInt(max);
        return Integer.parseInt(num_rsvps) > m; //(int)(m+(float)m*0.1f);
    }
    public Boolean shouldAppearFull() {
        return isFull() && !isAttending();
    }


}
