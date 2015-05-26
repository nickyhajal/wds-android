package com.worlddominationsummit.wds;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
            final MeetupsFragment ref = this;
            this.view = inflater.inflate(R.layout.meetups, container, false);
            this.listview = (HeaderListView) this.view.findViewById(R.id.meetupList);
            this.listview.getListView().setId(R.id.meetupListview);

            // DAY SELECT
            TextView name = (TextView) this.view.findViewById(R.id.selectLabel);
            name.setTypeface(Font.use("Vitesse_Medium"));
            Button btn = (Button) this.view.findViewById(R.id.selectBtn);
            btn.setTypeface(Font.use("Vitesse_Medium"));
            final LinearLayout selectBtns= (LinearLayout) this.view.findViewById(R.id.select_buttons);
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

    public void update_items(ArrayList<HashMap> items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        this.adapter = new MeetupsAdapter(this.getActivity());
        this.adapter.setDay(this.day);
        this.adapter.setItems(this.items);
        if (this.listview != null) {
            this.listview.setAdapter(this.adapter);
        }
    }
}
