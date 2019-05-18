package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nicky on 08/07/16.
 */
public class NotificationFragment extends Fragment {
    public View mView;
    public NotificationAdapter mAdapter;
    public ArrayList<HashMap> mNotns;
    public String mState = "loading";

    // Views
    public ListView mListview;
    public TextView mMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotns = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mView = inflater.inflate(R.layout.chats, container, false);
            mListview = (ListView) mView.findViewById(R.id.chatsList);
            mMessage = (TextView) mView.findViewById(R.id.msg);
            mAdapter = new NotificationAdapter(getActivity(), mNotns);
            mListview.setAdapter(mAdapter);
            syncState();
            updateItems();
            Font.applyTo(mView);
        }
        return mView;
    }

    public void setState(String state) {
        mState = state;
        syncState();
    }
    public void syncState() {
        if (mMessage != null) {
            switch (mState) {
                case "loading":
                    mMessage.setText("Loading...");
                    mMessage.setVisibility(View.VISIBLE);
                    mListview.setVisibility(View.GONE);
                    break;
                case "null":
                    mMessage.setText("Your notifications will appear here");
                    mMessage.setVisibility(View.VISIBLE);
                    mListview.setVisibility(View.GONE);
                    break;
                case "loaded":
                    mMessage.setVisibility(View.GONE);
                    mListview.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    public void sync() {
        setState("loading");
        Api.get("user/notifications", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray notns = response.optJSONArray("notifications");
                if (notns == null || notns.length() == 0) {
                    setState("null");
                } else {
                    try {
                        updateItems((ArrayList<HashMap>) JsonHelper.toList(notns));
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public void updateItems(ArrayList<HashMap> notns) {
        if (mNotns != null) {
            mNotns = notns;
        }
        updateItems();

    }

    public void updateItems() {
        if (mNotns != null && mNotns.size() > 0 && getActivity() != null) {
            setState("loaded");
            markRead();
            mAdapter = new NotificationAdapter(getActivity(), mNotns);
            if (mListview != null) {
                mListview.setAdapter(mAdapter);
            }
        }
    }

    public void markRead() {
        Api.post("user/notifications/read", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Me.fireSet("notification_count", (long) 0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

}
