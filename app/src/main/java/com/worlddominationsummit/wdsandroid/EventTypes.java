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

public class EventTypes {

    public static JSONArray list;
    public static JSONObject byId;
    public static JSONArray types;

    public static void init() {
        try {
            EventTypes.types = new JSONArray();
            EventTypes.types.put("meetup");
            EventTypes.types.put("academy");
            EventTypes.types.put("activity");
            EventTypes.types.put("spark_session");
            EventTypes.list = new JSONArray();
            EventTypes.byId = new JSONObject();
            JSONObject meetup = new JSONObject();
            meetup.put("title", "Meetups");
            meetup.put("singular", "Meetup");
            meetup.put("plural", "Meetups");
            meetup.put("id", "meetup");
            meetup.put("descr", "Informal hangouts and attendee-led gatherings");
            EventTypes.list.put(meetup);
            JSONObject academy = new JSONObject();
            academy.put("title", "Academies");
            academy.put("singular", "Academy");
            academy.put("plural", "Academies");
            academy.put("id", "academy");
            academy.put("descr", "Half-day workshops taught by alumni speakers and other experts");
            EventTypes.list.put(academy);
            JSONObject spark_session = new JSONObject();
            spark_session.put("title", "Spark Sessions");
            spark_session.put("singular", "Spark Session");
            spark_session.put("plural", "Spark Sessions");
            spark_session.put("id", "spark_session");
            spark_session.put("descr", "Open-ended conversations on specific topics");
            EventTypes.list.put(spark_session);
            JSONObject activity = new JSONObject();
            activity.put("title", "Activities");
            activity.put("singular", "Activity");
            activity.put("plural", "Activities");
            activity.put("id", "activity");
            activity.put("descr", "Special adventures just for WDS attendees");
            EventTypes.list.put(activity);
            for (int i = 0; i < EventTypes.list.length(); i++) {
                JSONObject ev = EventTypes.list.getJSONObject(i);
                try {
                    EventTypes.byId.put(ev.getString("id"), ev);
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
    }

}
