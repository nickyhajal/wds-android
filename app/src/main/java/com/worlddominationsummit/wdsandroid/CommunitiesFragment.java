package com.worlddominationsummit.wdsandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by nicky on 5/28/15.
 */
public class CommunitiesFragment extends Fragment {
    public View view;
    public ListView listview;
    public CommunitiesAdapter adapter;
    public ArrayList<HashMap> communities;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.communities = new ArrayList<HashMap>();
            try {
                this.communities = (ArrayList<HashMap>) JsonHelper.toList(Store.getJsonArray("interests"));
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            this.view = inflater.inflate(R.layout.communities, container, false);
            this.listview = (ListView) this.view.findViewById(R.id.commmunitiesList);
            Collections.sort(this.communities, new Comparator<HashMap>() {
                public int compare(HashMap o1, HashMap o2) {
                    String isO1 = String.valueOf(!Me.isInterested(String.valueOf(o1.get("interest_id"))));
                    String isO2 = String.valueOf(!Me.isInterested(String.valueOf(o2.get("interest_id"))));
                    return isO1.compareTo(isO2);
                }
            });
            this.adapter = new CommunitiesAdapter(this.getActivity(), this.communities);
            this.listview.setAdapter(this.adapter);


        }
        return this.view;
    }
}
