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
public class AttendeeSearchFragment extends Fragment{
    public View view;
    public ListView listview;
    public AttendeeSearchAdapter adapter;
    public ArrayList<HashMap<String, String>> items;
    public RelativeLayout mSearchControls;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.attendee_search_results, container, false);
            this.listview = (ListView) this.view.findViewById(R.id.results);
            mSearchControls = (RelativeLayout) this.view.findViewById(R.id.search_controls);
            this.update_items(new ArrayList<HashMap<String, String>>());
            int count = mSearchControls.getChildCount();
            View.OnClickListener searchControlClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Button t = (Button) v;
                String action = t.getTag().toString();
                MainActivity.self.search_inp.setText(action);
                    MainActivity.self.run_search(action);
                }
            };
            for (int i = 0; i <= count; i++) {
                View v = mSearchControls.getChildAt(i);
                if (v instanceof Button) {
                    ((Button) v).setTypeface(Font.use("Vitesse_Medium"));
                    v.setOnClickListener(searchControlClick);
                }
            }
        }
        return this.view;
    }

    public void update_items(JSONArray items) {
        try {
            this.update_items((ArrayList<HashMap<String, String>>) JsonHelper.toList(items));
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception in attendee search update items", e);
        }
    }
    public void update_items(ArrayList<HashMap<String, String>> items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        if (getActivity() != null) {
            this.adapter = new AttendeeSearchAdapter(this.getActivity(), this.items);
            if (this.listview != null) {
                this.listview.setAdapter(this.adapter);
            }
        }
    }
}
