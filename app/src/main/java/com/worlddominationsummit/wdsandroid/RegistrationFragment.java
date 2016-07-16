package com.worlddominationsummit.wdsandroid;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
public class RegistrationFragment extends Fragment{
    public View view;
    public HeaderListView listview;
    public RegistrationAdapter adapter;
    public JSONArray items;
    public String day = "";
    public TextView mNullMsg;

    public void willDisplay() {
        if(day.equals("")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String today = dateFormat.format(date);
            if (today.compareTo("2016-08-09") < 0) {
                day = "2016-08-11";
            }
            else {
                day = today;
            }
        }
        if (this.items == null) {
            this.items = new JSONArray();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final RegistrationFragment ref = this;
        Assets.getSmart("events", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                try {
                    ref.update_items(rsp.getJSONArray("data"));
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
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
            final RegistrationFragment ref = this;
            this.view = inflater.inflate(R.layout.registration, container, false);
            this.listview = (HeaderListView) this.view.findViewById(R.id.scheduleList);
            this.listview.getListView().setId(R.id.scheduleListview);
            mNullMsg = (TextView) this.view.findViewById(R.id.nullMsg);
            mNullMsg.setTypeface(Font.use("Karla_Bold"));

            // DAY SELECT
            TextView name = (TextView) this.view.findViewById(R.id.selectLabel);
            name.setTypeface(Font.use("Vitesse_Medium"));
            Button btn = (Button) this.view.findViewById(R.id.selectBtn);
            btn.setTypeface(Font.use("Vitesse_Medium"));
            final LinearLayout selectBtns = (LinearLayout) this.view.findViewById(R.id.select_buttons);
            selectBtns.setVisibility(View.GONE);
            btn.setTypeface(Font.use("Vitesse_Medium"));
            View.OnClickListener selectDayListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) v;
                    String day = t.getTag().toString();
                    String dayStr = t.getText().toString();
                    ref.changeDay(day, dayStr);
                    selectBtns.setVisibility(View.GONE);
                }
            };
            int count = selectBtns.getChildCount();
            for (int i = 0; i <= count; i++) {
                View v = selectBtns.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTypeface(Font.use("Vitesse_Medium"));
                    v.setOnClickListener(selectDayListener);
                }
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    selectBtns.setVisibility(View.VISIBLE);
                }
            });
            this.update_items();
        }
        return this.view;
    }

    public void changeDay(String day, String dayStr) {
        this.day = day;
        TextView dayView = (TextView) this.view.findViewById(R.id.selectLabel);
        dayView.setText(dayStr);
        update_items();
    }

    public void update_items(JSONArray items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        this.adapter = new RegistrationAdapter(this.getActivity());
        this.adapter.setDay(this.day);
        this.adapter.setItems(this.items);
        checkIfNull();
        if (this.listview != null) {
            this.listview.setAdapter(this.adapter);
        }
    }
    public void checkIfNull() {
        if (this.adapter.getCount() > 0) {
            this.listview.setVisibility(View.VISIBLE);
            mNullMsg.setVisibility(View.GONE);
        }
        else {
            this.listview.setVisibility(View.GONE);
            mNullMsg.setVisibility(View.VISIBLE);
        }
    }
}
