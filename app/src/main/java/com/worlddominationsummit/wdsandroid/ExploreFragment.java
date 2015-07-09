package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
public class ExploreFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public View mView;
    public ListView mPlacesList;
    public PlacesAdapter mAdapter;
    public LinearLayout mSelectBtns;
    public ArrayList<HashMap> mItems;
    public TextView mLabel;
    public int mPlaceType;
    public LocationRequest mLocationRequest;
    public Boolean mRequestingLocationUpdates = true;
    public String mLastUpdateTime;
    public Location mCurrentLocation;
    public JSONObject mLastClosestPlace;
    public float mLastClosestDistance;
    private GoogleApiClient mGoogleApiClient;
    public String mLastCheckin = "999999";
    public long mLastCheckinTime = 0;
    public JSONObject mCheckins;
    public Boolean mUpdatingCheckins = false;
    public String mSort = "distance";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        update_checkins();
        MainActivity.self.checkinFragment.onLocationChanged(location);
//        update_places();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        update_places();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void update_places() {
        Assets.getSmart("places", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                if (mView != null) {
                    try {
                        JSONArray filtered = new JSONArray();
                        JSONArray places = rsp.getJSONArray("data");
                        int len = places.length();
                        mLastClosestDistance = 99999999;
                        mLastClosestPlace = null;
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
                                    place.put("order_distance", distance);
                                    place.put("distance", distance);
                                    place.put("units", "ft");
                                    if (distance > 1000) {
                                        place.put("distance", (distance / 5280));
                                        place.put("units", "mi");
                                    }
                                    if (mLastClosestDistance > distance) {
                                        mLastClosestDistance = distance;
                                        mLastClosestPlace = place;
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
                        if (mCurrentLocation != null && mSort == "distance") {
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
                        } else if (mCheckins != null && mSort == "checkins") {
                            Collections.sort(filtered_list, new Comparator<HashMap>() {
                                public int compare(HashMap o1, HashMap o2) {
                                    int a = Integer.parseInt((String) o1.get("checkins"));
                                    int b = Integer.parseInt((String) o2.get("checkins"));
                                    if (a > b) {
                                        return -1;
                                    } else if (a == b) {
                                        return 0;
                                    } else {
                                        return 1;
                                    }
                                }
                            });
                        }
                        update_items(filtered_list);
                        // TODO: IF AUTO CHECKIN IS ON
                        checkin();
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

    public void changeState(int pos) {
        if (pos == 0) {
            mSort = "distance";
        } else if (pos == 1) {
            mSort = "checkins";
        }
        update_places();
    }

    public void updatePlaceTypes(JSONArray places) {
        int len = places.length();
        ArrayList<String> types = new ArrayList<String>();
        for(int i = 0; i < len; i++) {
            JSONObject place = new JSONObject();
            try {
                place = places.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            String type = place.optString("place_type");
            if(type != null && type.length() > 0  && !types.contains(type)) {
                types.add(type);
            }
        }
        int count = mSelectBtns.getChildCount();
        int num_types = types.size();
        for (int i = 0; i <= count; i++) {
            TextView v = (TextView) mSelectBtns.getChildAt(i);
            v.setVisibility(View.VISIBLE);
            if (i > 0) {
                if (i < num_types) {
                    v.setText(types.get(i-1));
                }
                else if (i == num_types) {
                    v.setText("Staff Picks");
                }
                else {
                    v.setVisibility(View.GONE);
                }
            }

        }
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
    public void changePlaceType(String inx, String heading) {
        mPlaceType = Integer.parseInt(inx);
        mLabel.setText(heading);
        update_places();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.explore, container, false);
            mPlacesList = (ListView) mView.findViewById(R.id.placesList);
            mLabel = (TextView) mView.findViewById(R.id.selectLabel);
            mLabel.setTypeface(Font.use("Vitesse_Medium"));
            Button btn = (Button) mView.findViewById(R.id.selectBtn);
            btn.setTypeface(Font.use("Vitesse_Medium"));
            mSelectBtns = (LinearLayout) mView.findViewById(R.id.select_buttons);
            mSelectBtns.setVisibility(View.GONE);
            btn.setTypeface(Font.use("Vitesse_Medium"));
            View.OnClickListener selectDayListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) v;
                    changePlaceType((String) t.getTag(), (String) t.getText());
                    mSelectBtns.setVisibility(View.GONE);
                }
            };
            int count = mSelectBtns.getChildCount();
            for (int i = 0; i <= count; i++) {
                View v = mSelectBtns.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTypeface(Font.use("Vitesse_Medium"));
                    v.setOnClickListener(selectDayListener);
                }
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mSelectBtns.setVisibility(View.VISIBLE);
                }
            });
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
            mAdapter = new PlacesAdapter(this.getActivity(), mItems);
            mAdapter.mContext = this;
            mPlacesList.setAdapter(mAdapter);
        }
        else {
            mAdapter.refill(mItems);
        }
    }

    public void checkin() {
        if (mLastClosestPlace != null) {
            Puts.i(mLastClosestDistance);
            Puts.i(mLastClosestPlace.toString());
        }
       if (mLastClosestDistance < 400 ) {
           long now = System.currentTimeMillis() / 1000;
           if (!mLastCheckin.equals(String.valueOf(mLastClosestPlace.optInt("place_id"))) || now - mLastCheckinTime > 300) {
               mLastCheckin = mLastClosestPlace.optString("place_id");
               mLastCheckinTime = now;
               JSONObject params = new JSONObject();
               try {
                   params.put("location_id", mLastClosestPlace.optInt("place_id"));
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
           }
       }
    }

    public void update_checkins() {
        if (!mUpdatingCheckins) {
            mUpdatingCheckins = true;
            try {
                Api.get("checkins/recent", new JSONObject("{by_id: 1}"), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject rsp) {
                        mUpdatingCheckins = false;
                        mCheckins = rsp.optJSONObject("checkins");
                        update_places();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mUpdatingCheckins = false;
                    }
                });
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
