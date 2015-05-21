package com.worlddominationsummit.wds;
/**
 * Created by nicky on 5/18/15.
 */

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class Event{
    public String event_id;
    public String what = "░░░░░░░░░";
    public String who = "░░░░░░░";
    public String place = "░░░░░";
    public String descr = "░░░░░ ░░░░░░░░░░ ░░░░░";
    public String start;
    public String type;
    public String lat;
    public String lon;
    public String address;
    public String startTime;
    public String startStr;
    public String dayStr;
    public String because;
    public String becauseStr;
    public int num_rsvps;
    public JSONArray hostsJSON;
    public ArrayList<Attendee> hosts;


    public static Event fromHashMap(HashMap<String, String> params) {
        return Event.fromJson(new JSONObject(params));
    }
    public static Event fromJson(JSONObject params) {
        Event ev = new Event();
        ev.event_id = params.optString("event_id");
        ev.what = params.optString("what");
        ev.who = params.optString("who");
        ev.place = params.optString("place");
        ev.descr = params.optString("descr");
        ev.start = params.optString("start");
        ev.type = params.optString("type");
        ev.lat = params.optString("lat");
        ev.lon = params.optString("lon");
        ev.address = params.optString("address");
        ev.startTime = params.optString("startTime");
        ev.startStr = params.optString("startStr");
        ev.dayStr = params.optString("dayStr");
        ev.hostsJSON = params.optJSONArray("hostsJSON");
        ev.init();
        return ev;
    }

    public Event() { }
    public Event(String event_id, String what, String who, String place, String descr, String start, String type, String lat, String lon, String address, String startTime, String startStr, String dayStr, JSONArray hostsJSON) {
        this.event_id = event_id;
        this.what = what;
        this.who = who;
        this.place = place;
        this.descr = descr;
        this.type = type;
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

    }



}
