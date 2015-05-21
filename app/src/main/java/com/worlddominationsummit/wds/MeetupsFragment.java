package com.worlddominationsummit.wds;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.*;
import com.android.volley.Response;
import com.applidium.headerlistview.HeaderListView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class MeetupsFragment extends Fragment{
    public View view;
    public HeaderListView listview;
    public MeetupsAdapter adapter;
    public ArrayList<HashMap> items;
    public String day = "2014-07-12";

    public void willDisplay() {
        this.items = new ArrayList<HashMap>();
        final MeetupsFragment ref = this;
        Puts.i("GET EVENTS");
        Assets.pull("events", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                ref.update_items((ArrayList<HashMap>) Store.getArray("meetups"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                ref.tabsStarted = true;
//                ref.open_tabs();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.meetups, container, false);
            this.listview = (HeaderListView) this.view.findViewById(R.id.meetupList);
            this.listview.getListView().setId(R.id.meetupListview);
            this.update_items();
        }
        return this.view;
    }

    public void update_items(ArrayList<HashMap> items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        Puts.i("UPDATE ITEMS");
        this.adapter = new MeetupsAdapter(this.getActivity());
        this.adapter.setDay(this.day);
        this.adapter.setItems(this.items);
        if (this.listview != null) {
            this.listview.setAdapter(this.adapter);
        }
    }
}
