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
    public static ArrayList<Integer> regs;

    public static void init() {
        try {
            EventTypes.types = new JSONArray();
            EventTypes.regs = new ArrayList<Integer>();
            EventTypes.regs.add(651);
            EventTypes.regs.add(771);
            EventTypes.regs.add(772);
            EventTypes.regs.add(773);
            EventTypes.regs.add(774);
            EventTypes.regs.add(775);
            EventTypes.regs.add(776);
            EventTypes.regs.add(778);
            EventTypes.regs.add(781);
//            EventTypes.types.put("trust");
            EventTypes.types.put("academy");
            EventTypes.types.put("activity");
            EventTypes.types.put("meetup");
//            EventTypes.types.put("spark_session");
//            EventTypes.types.put("expedition");
//            EventTypes.types.put("registration");
            EventTypes.list = new JSONArray();
            EventTypes.byId = new JSONObject();
//            JSONObject registration = new JSONObject();
//            registration.put("title", "Registration");
//            registration.put("singular", "Registration");
//            registration.put("plural", "Registration");
//            registration.put("id", "registration");
//            registration.put("descr", "Let us know which registration session works for you");
//            EventTypes.list.put(registration);
            JSONObject amb = new JSONObject();
            amb.put("title", "Ambassador");
            amb.put("singular", "Ambassador");
            amb.put("plural", "Ambassador");
            amb.put("id", "ambassador");
            amb.put("descr", "Ambassador");
            EventTypes.list.put(amb);
//            JSONObject trust = new JSONObject();
//            trust.put("title", "Trust Issues");
//            trust.put("singular", "Trust Issues");
//            trust.put("plural", "Trust Issues");
//            trust.put("id", "trust");
//            trust.put("descr", "Informal hangouts and attendee-led gatherings");
//            EventTypes.list.put(trust);
//            JSONObject expedition = new JSONObject();
//            expedition.put("title", "Expeditions");
//            expedition.put("singular", "Expedition");
//            expedition.put("plural", "Expeditions");
//            expedition.put("id", "expedition");
//            expedition.put("descr", "Unique adventures crafted just for WDS Attendees");
//            EventTypes.list.put(expedition);
            JSONObject academy = new JSONObject();
            academy.put("title", "Academies");
            academy.put("singular", "Academy");
            academy.put("plural", "Academies");
            academy.put("id", "academy");
            academy.put("descr", "Half-day workshops taught by alumni speakers and other experts");
            EventTypes.list.put(academy);
//            JSONObject spark_session = new JSONObject();
//            spark_session.put("title", "Spark Sessions");
//            spark_session.put("singular", "Spark Session");
//            spark_session.put("plural", "Spark Sessions");
//            spark_session.put("id", "spark_session");
//            spark_session.put("descr", "Open-ended conversations on specific topics");
//            EventTypes.list.put(spark_session);
            JSONObject activity = new JSONObject();
            activity.put("title", "WDS HQ");
            activity.put("singular", "Activity");
            activity.put("plural", "Activities");
            activity.put("id", "activity");
            activity.put("descr", "Special activities to share with your fellow attendees");
            EventTypes.list.put(activity);
            JSONObject meetup = new JSONObject();
            meetup.put("title", "Meetups");
            meetup.put("singular", "Meetup");
            meetup.put("plural", "Meetups");
            meetup.put("id", "meetup");
            meetup.put("descr", "Informal hangouts and attendee-led gatherings");
//            meetup.put("descr", "Coming soon!");
            EventTypes.list.put(meetup);
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
