package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nicky on 5/18/15.
 */
public class MeetupAttendeesFragment extends Fragment{
    public View view;
    public ListView listview;
    public AttendeeSearchAdapter adapter;
    public ArrayList<HashMap> items;
    public TextView mLoading;
    public TextView mNullMsg;
    public String mMeetupId;

    public void setMeetup(String meetup_id) {
        mMeetupId = meetup_id;
        if (listview != null) {
            listview.setVisibility(View.GONE);
            mNullMsg.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);
            JSONObject params = new JSONObject();
            try {
                params.put("event_id", meetup_id);
                params.put("include_users", "1");
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Api.get("event/attendees", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    JSONArray atns = jsonObject.optJSONArray("attendees");
                    if (atns != null && atns.length() > 0) {
                        update_items(jsonObject.optJSONArray("attendees"));
                        listview.setVisibility(View.VISIBLE);
                        mLoading.setVisibility(View.GONE);
                        mNullMsg.setVisibility(View.GONE);
                    }
                    else {
                        listview.setVisibility(View.GONE);
                        mLoading.setVisibility(View.GONE);
                        mNullMsg.setVisibility(View.VISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    MainActivity.offlineAlert();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.meetup_attendees, container, false);
            this.listview = (ListView) this.view.findViewById(R.id.results);
            mLoading = (TextView) this.view.findViewById(R.id.loading);
            mNullMsg= (TextView) this.view.findViewById(R.id.nullMsg);
            this.update_items(new ArrayList<HashMap>());
            Font.applyTo(view);
            setMeetup(mMeetupId);
        }
        return this.view;
    }

    public void update_items(JSONArray items) {
        try {
            this.update_items((ArrayList<HashMap>) JsonHelper.toList(items));
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception in attendee search update items", e);
        }
    }
    public void update_items(ArrayList<HashMap> items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        this.adapter = new AttendeeSearchAdapter(this.getActivity(), this.items);
        if (this.listview != null) {
            this.listview.setAdapter(this.adapter);
        }
    }
}
