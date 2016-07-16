package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class CheckinFragment extends Fragment {
    public View mView;
    public ListView mPlacesList;
    public CheckinAdapter mAdapter;
    public ArrayList<HashMap> mItems;
    public int mPlaceType;
    public Location mCurrentLocation;
    public String mLastCheckin = "999999";
    public long mLastCheckinTime = 0;
    public Switch mAutoSwitch;
    public JSONObject mCheckins;
    public Boolean mCheckingIn = false;

    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        update_places();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        update_places();
    }

    public void update_places() {
        if (mView == null) {
            return;
        }
        Assets.getSmart("places", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                if (mView != null) {
                    try {
                        JSONArray filtered = new JSONArray();
                        JSONArray places = rsp.getJSONArray("data");
                        int len = places.length();
                        for (int i = 0; i < len; i++) {
                            JSONObject place = places.optJSONObject(i);
                            String pick = place.optString("pick");
                            place.put("address", ((String) place.get("address")).replaceAll(", Portland[,]? OR[\\s0-9]*", ""));
                            int type = Integer.parseInt(place.optString("place_type"));
                            if (mPlaceType == 0 || type == mPlaceType || (mPlaceType == 999 && pick != null && !pick.equals("null") && pick.length() > 0)) {
                                if (mCurrentLocation != null) {
                                    float[] result = {0};
                                    Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), Double.parseDouble(place.optString("lat")), Double.parseDouble(place.optString("lon")), result);
                                    float distance = (float) (result[0] * 3.28);
                                    if (distance > 750) { continue; }
                                    place.put("order_distance", distance);
                                    place.put("distance", distance);
                                    place.put("units", "ft");
                                    if (distance > 1000) {
                                        place.put("distance", (distance / 5280));
                                        place.put("units", "mi");
                                    }
                                }
                                if (mCheckins != null) {
                                    JSONObject checkin_obj = mCheckins.optJSONObject(place.optString("place_id"));
                                    if (checkin_obj != null) {
                                        place.put("checkins", checkin_obj.optString("num_checkins"));
                                    }
                                    else {
                                        place.put("checkins", "0");
                                    }
                                }
                                filtered.put(place);
                            }
                        }
                        ArrayList<HashMap> filtered_list = ((ArrayList<HashMap>) JsonHelper.toList(filtered));
                        if (mCurrentLocation != null) {
                            Collections.sort(filtered_list, new Comparator<HashMap>() {
                                public int compare(HashMap o1, HashMap o2) {
                                    if (((Double) o1.get("order_distance")) > ((Double) o2.get("order_distance"))) {
                                        return 1;
                                    } else if (((Double) o1.get("order_distance")) == ((Double) o2.get("order_distance"))) {
                                        return 0;
                                    } else {
                                        return -1;
                                    }
                                }
                            });
                        }
                        update_items(filtered_list);
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }

    public void init() {
        JSONObject params = new JSONObject();
        try {
            params.put("channel_type", "global");
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        mItems = new ArrayList<HashMap>();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.checkin, container, false);
            mAutoSwitch = (Switch) mView.findViewById(R.id.autoSwitch);
            mPlacesList = (ListView) mView.findViewById(R.id.placesList);
            if (Store.get("auto-checkin", "yes").equals("yes")) {
                mAutoSwitch.setChecked(true);
            }
            else {
                mAutoSwitch.setChecked(false);
            }
            mAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Store.set("auto-checkin", "yes");
                    }
                    else {
                        Store.set("auto-checkin", "no");
                    }
                }
            });
            Font.applyTo(mView);
            update_places();
        }
        return mView;
    }

    public void update_items(ArrayList<HashMap> items) {
        mItems = items;
        this.update_items();
    }
    public void update_items() {
        if (mAdapter == null && mPlacesList != null) {
            mAdapter = new CheckinAdapter(this.getActivity(), mItems);
            mAdapter.mContext = this;
            mPlacesList.setAdapter(mAdapter);
        }
        else {
            mAdapter.refill(mItems);
        }
    }

    @Override
    public void onDestroyView() {
        mCheckingIn = false;
        super.onDestroyView();
    }

    public void checkin(HashMap<String,String> place, final Button btn) {
        if (!mCheckingIn) {
            mCheckingIn = true;
            long now = System.currentTimeMillis() / 1000;
            btn.setText("Checking In");
            if (!mLastCheckin.equals((place.get("place_id"))) || now - mLastCheckinTime > 300) {
                mLastCheckin = String.valueOf(place.get("place_id"));
                mLastCheckinTime = now;
                JSONObject params = new JSONObject();
                try {
                    params.put("location_id", place.get("place_id"));
                    params.put("location_type", "place");
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
                Api.post("user/checkin", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                        btn.setText("Check In");
                    }
                }, 750);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
