package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
public class EventsFragment extends Fragment{
    public View view;
    public HeaderListView listview;
    public EventsAdapter adapter;
    public JSONArray items;
    public String day = "";
    public String mState = "browse";
    public String mDayStr = "Monda, August 8th";
    public TextView mNullMsg;
    public String mMeetupType = "all";
    public LinearLayout mMeetupNav;
    public String mType = "meetup";
    public String mTitle;
    public String mSingular;
    public String mPlural;
    public Spinner mViewSpinner;
    public Spinner mTypeSpinner;


    public void willDisplay() {
        if(day.equals("")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String today = dateFormat.format(date);
            if (today.compareTo("2016-08-08") < 0) {
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
        final EventsFragment ref = this;
        Assets.getSmart(mType, new Response.Listener<JSONObject>() {
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
            final EventsFragment ref = this;
            this.view = inflater.inflate(R.layout.events, container, false);
            this.listview = (HeaderListView) this.view.findViewById(R.id.meetupList);
            this.listview.getListView().setId(R.id.meetupListview);
            mMeetupNav = (LinearLayout) this.view.findViewById(R.id.meetupNav);
            mNullMsg = (TextView) this.view.findViewById(R.id.nullMsg);
            mNullMsg.setTypeface(Font.use("Karla_Bold"));
            mViewSpinner = (Spinner) this.view.findViewById(R.id.view_spinner);
            mTypeSpinner = (Spinner) this.view.findViewById(R.id.type_spinner);
            ArrayList<String> viewSections = new ArrayList<String>();
            viewSections.add("Browse Meetups");
            viewSections.add("Attending Meetups");
            viewSections.add("Suggested Meetups");
            TitleSpinner viewSpinnerAdapter = new TitleSpinner(getContext(), R.layout.title_spinner, viewSections);
            mViewSpinner.setAdapter(viewSpinnerAdapter);
            mViewSpinner.setSelection(0);
            ArrayList<String> typeSections = new ArrayList<String>();
            typeSections.add("All Types");
            typeSections.add("Type: Discover");
            typeSections.add("Type: Experience");
            typeSections.add("Type: Network");
            TitleSpinner typeSpinnerAdapter = new TitleSpinner(getContext(), R.layout.title_spinner, typeSections);
            mTypeSpinner.setAdapter(typeSpinnerAdapter);
            mTypeSpinner.setSelection(0);

            mViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    changeState(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    changeType(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

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

    public void setType(HashMap<String, String> type) {
        mType = type.get("id");
        mSingular = type.get("singular");
        mPlural = type.get("plural");
        mTitle = type.get("title");
        if (mMeetupNav != null) {
            mMeetupNav.setVisibility(View.GONE);
            if (mType.equals("meetup")) {
                mMeetupNav.setVisibility(View.VISIBLE);
            }
        }
        update_items();
    }
    public void changeDay(String day, String dayStr) {
        this.day = day;
        mDayStr = dayStr;
        TextView dayView = (TextView) this.view.findViewById(R.id.selectLabel);
        dayView.setText(dayStr);
        update_items();
    }
    public void changeState(int id) {
        String[] states = {"browse", "attending", "suggested"};
        mState = states[id];
        update_items();
    }
    public void changeType(int id) {
        String[] types = {"all", "discover", "experience", "network"};
        mMeetupType = types[id];
        update_items();
    }

    public void update_items(JSONArray items) {
        this.items = items;
        this.update_items();
    }
    public void update_items() {
        this.adapter = new EventsAdapter(this.getActivity());
        this.adapter.setDay(this.day);
        this.adapter.setState(mState);
        this.adapter.setType(mType);
        this.adapter.setMeetupType(mMeetupType);
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
            String[] parts = mDayStr.split(" ");
            String typelow = EventTypes.byId.optJSONObject(mType).optString("plural").toLowerCase();
            String plural = EventTypes.byId.optJSONObject(mType).optString("plural");
            if (!mType.equals("spark_session")) {
                plural = plural.toLowerCase();
            }
            if (mType.equals("meetup") && !mMeetupType.equals("all")) {
                plural = mMeetupType+' '+plural;
            }
            String end = " for "+parts[1]+" "+parts[2]+"...yet.";
            String text = "There are no "+plural+end;
            if (mState.equals("attending")) {
                text = "You haven't RSVPd to any "+plural+end;
            }
            else if (mState.equals("suggested")) {
                text = "Join more communities for more suggestions.";
            }
            if (mNullMsg != null) {
                mNullMsg.setText(text);
                this.listview.setVisibility(View.GONE);
                mNullMsg.setVisibility(View.VISIBLE);
            }
        }
    }
    private static class TitleSpinner extends ArrayAdapter<String> {
        private TitleSpinner(Context context, int resource, ArrayList<String> items) {
            super(context, resource, items);
        }

        // Affects default (closed) state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            float density = MainActivity.density;
            int p = (int) density * 2;
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTypeface(Font.use("Vitesse_Medium"));
            view.setPadding(0,p,0,0);
            view.setTextSize(17.0f);
            view.setTextColor(MainActivity.self.getResources().getColor(R.color.dark_gray));
            return view;
        }

        // Affects opened state of the spinner
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            float density = MainActivity.density;
            int p = (int) density * 10;
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTypeface(Font.use("Vitesse_Medium"));
            view.setPadding(p, p, p, p);
            view.setTextSize(17.0f);
            view.setBackgroundColor(MainActivity.self.getResources().getColor(R.color.coffee));
            return view;
        }
    }
}
