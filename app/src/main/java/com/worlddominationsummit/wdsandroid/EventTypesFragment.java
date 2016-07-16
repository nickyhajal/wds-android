package com.worlddominationsummit.wdsandroid;

import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import com.applidium.headerlistview.HeaderListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class EventTypesFragment extends Fragment{
    public View mView;
    public EventTypesAdapter mAdapter;
    public JSONArray mItems;
    public ListView mTypesList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.update_items(EventTypes.list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            final EventTypesFragment ref = this;
            mView = inflater.inflate(R.layout.event_types, container, false);
            mTypesList = (ListView) mView.findViewById(R.id.eventTypesList);
        }
        return mView;
    }

    public void update_items(JSONArray items) {
        mItems = items;
        this.update_items();
    }
    public void update_items() {
        try {
            mAdapter = new EventTypesAdapter(this.getActivity(), (ArrayList<HashMap>) JsonHelper.toList(mItems));
            if (mTypesList != null) {
                mTypesList.setAdapter(mAdapter);
            }
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
    }
}
