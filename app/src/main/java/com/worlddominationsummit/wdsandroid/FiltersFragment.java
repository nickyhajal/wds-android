package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nicky on 5/28/15.
 */
public class FiltersFragment extends Fragment {
    public View mView;
    public Switch mAtns;
    public Spinner mComms;
//    public Switch mTwitter;
    public Switch mEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mView = inflater.inflate(R.layout.filters, container, false);
            ViewGroup vg = (ViewGroup) mView;
            mAtns = (Switch) mView.findViewById(R.id.atns);
//            mTwitter = (Switch) mView.findViewById(R.id.twitter);
            mEvents = (Switch) mView.findViewById(R.id.events);
            mComms = (Spinner) mView.findViewById(R.id.comms);
            Font.applyTo(mView);
            ArrayList<String> comms = new ArrayList<String>();
            comms.add("All");
            comms.add("Mine");
            comms.add("None");
            ArrayAdapter CommAdapter =  new FSpinner(getActivity(), android.R.layout.simple_spinner_item, comms);
            mComms.setAdapter(CommAdapter);
            mComms.setSelection(0);
            List views = getAllViews(mView);
            int count = views.size();
            for (int i = 0; i < count; i++) {
                View v = (View) views.get(i);
                if (v instanceof Switch) {
                    ((Switch) v).setTypeface(Font.use("Vitesse_Medium"));
                    ((Switch) v).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            String val;
                            String name = buttonView.getTag().toString();
                            if (isChecked) {
                                val = "0";
//                                if (name.equals("following")) {
//                                    val = "0";
//                                }
//                                else {
//                                    val = "1";
//                                }
                            }
                            else {
                                val = "1";
                            }
//                            else {
////                                if(name.equals("following")) {
////                                    val = "1";
////                                }
////                                else {
////                                    val = "0";
////                                }
//                            }
                            MainActivity.self.homeFragment.mDispatch.setFilter(name, val);
                            MainActivity.self.homeFragment.resetDispatch();
                            MainActivity.self.homeFragment.updateDispatch();
                        }
                    });
                }
                if (v instanceof Spinner) {
                    ((Spinner) v).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            String name = parentView.getTag().toString();
                            String val = String.valueOf(position);
                            MainActivity.self.homeFragment.mDispatch.setFilter(name, val);
                            MainActivity.self.homeFragment.resetDispatch();
                            MainActivity.self.homeFragment.updateDispatch();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    });
                }
            }
            setFilters(MainActivity.self.homeFragment.mDispatch.mFilters);
        }
        return mView;
    }
    private List<View> getAllViews(View v) {
        if (!(v instanceof ViewGroup) || ((ViewGroup) v).getChildCount() == 0) // It's a leaf
        { List<View> r = new ArrayList<View>(); r.add(v); return r; }
        else {
            List<View> list = new ArrayList<View>(); list.add(v); // If it's an internal node add itself
            int children = ((ViewGroup) v).getChildCount();
            for (int i=0;i<children;++i) {
                list.addAll(getAllViews(((ViewGroup) v).getChildAt(i)));
            }
            return list;
        }
    }
    public void setFilters(JSONObject filters) {
        if (mAtns != null) {
            if (filters.has("following") && filters.optString("following").equals("0")) {
                mAtns.setChecked(true);
            } else {
                mAtns.setChecked(false);
            }
            if (filters.has("twitter") && filters.optString("twitter").equals("0")) {
//                mTwitter.setChecked(true);
            } else {
//                mTwitter.setChecked(false);
            }
            if (filters.has("meetups") && filters.optString("meetups").equals("0")) {
                mEvents.setChecked(true);
            } else {
                mEvents.setChecked(false);
            }
            mComms.setSelection(Integer.parseInt(filters.optString("communities")));
            Store.set("dispatch_filters", filters);
        }
    }

    private static class FSpinner extends ArrayAdapter<String> {
        private FSpinner(Context context, int resource, ArrayList<String> items) {
            super(context, resource, items);
        }

        // Affects default (closed) state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            float density = MainActivity.density;
            int p = (int) density * 5;
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTypeface(Font.use("Karla_Bold"));
            view.setPadding(0,p,0,0);
            return view;
        }

        // Affects opened state of the spinner
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            float density = MainActivity.density;
            int p = (int) density * 10;
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTypeface(Font.use("Karla_Bold"));
            view.setPadding(p, p, p, p);
            view.setTextColor(MainActivity.self.getResources().getColor(R.color.white));
            view.setBackgroundColor(MainActivity.self.getResources().getColor(R.color.orange));
            return view;
        }
    }
}
