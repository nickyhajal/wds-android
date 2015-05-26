package com.worlddominationsummit.wds;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.volley.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class HomeFragment extends Fragment{
    public View view;
    public ListView dispatchContent;
    public DispatchAdapter adapter;
    public ArrayList<HashMap> items;
    public Dispatch mDispatch;
    public SwipeRefreshLayout swipeRefresh;

    public void init() {
        mDispatch = new Dispatch();
        mDispatch.context = this;
        JSONObject params = new JSONObject();
        try {
            params.put("channel_type", "global");
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        mDispatch.initParams(params);
        mDispatch.initFilters();
        this.items = new ArrayList<HashMap>();
    }
    public void willDisplay() {
        final HomeFragment ref = this;
        mDispatch.fetch(new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                try {
                    ref.update_items((ArrayList<HashMap>) JsonHelper.toList(rsp.getJSONArray("feed_contents")));
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("WDS", "VOLEY ER", error);
//                ref.tabsStarted = true;
//                ref.open_tabs();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.view == null) {
            this.view = inflater.inflate(R.layout.dispatch, container, false);
            this.dispatchContent = (ListView) this.view.findViewById(R.id.dispatchContent);
            this.swipeRefresh = (SwipeRefreshLayout) this.view.findViewById(R.id.swipeRefresh);
            Puts.i(swipeRefresh.toString());
            this.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    update_dispatch();
                }
            });

        }
        return this.view;
    }

    public void update_dispatch() {
        Puts.i("UPDATE");
    }
    public void update_items(ArrayList<HashMap> items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        this.adapter = new DispatchAdapter(this.getActivity(), this.items);
        if (this.dispatchContent != null) {
            this.dispatchContent.setAdapter(this.adapter);
        }
    }
}
