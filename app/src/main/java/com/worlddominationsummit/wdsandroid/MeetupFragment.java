package com.worlddominationsummit.wdsandroid;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

/**
 * Created by nicky on 5/18/15.
 */
public class MeetupFragment extends Fragment implements OnMapReadyCallback {
    public View view;
    MapView mapView;
    public LinearLayout slidingLayout;
    public DominationScrollView content;
    public TextView what;
    public TextView time;
    public TextView who;
    public TextView descr;
    public TextView mHostedBy;
    public TextView mHostName;
    public Button mRsvp;
    public Button mAttendees;
    public Button mFeed;
    public ImageView mHostAvatar;
    public ImageLoader mImgLoader = new ImageLoader(getActivity());
    public Event event;
    public GoogleMap map;
    static final int MIN_DISTANCE = 50;
    private float downY, upY;
    private static final int ANIMATION_DURATION = 200;
    private boolean animating = false;
    private boolean map_open = true;
    private float contentStartWeight;
    private float weightSum;
    private Handler animationHandler = new Handler();
    private long animationTime;

    private Runnable stepAnimation = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            float animationStep = (currentTime - animationTime) * 1f / ANIMATION_DURATION;
            float weightOffset = animationStep * (weightSum - contentStartWeight);
            LinearLayout.LayoutParams mapParams = (LinearLayout.LayoutParams) mapView.getLayoutParams();
            LinearLayout.LayoutParams contentParams = (LinearLayout.LayoutParams) content.getLayoutParams();
            contentParams.weight += map_open ? -weightOffset : weightOffset;
            mapParams.weight += map_open ? weightOffset : -weightOffset;
            if (contentParams.weight >= weightSum) {
                animating = false;
                contentParams.weight = weightSum;
                mapParams.weight = 0;
            } else if (contentParams.weight <= contentStartWeight) {
                animating = false;
                contentParams.weight = contentStartWeight;
                mapParams.weight = weightSum - contentStartWeight;
            }
            slidingLayout.requestLayout();
            animationTime = currentTime;
            if (animating) {
                animationHandler.postDelayed(stepAnimation, 30);
            }
        }
    };

    private void toggleMap(boolean open) {
        if (!animating) {
            content.setScroll(!open);
            map_open = open;
            animating = true;
            animationTime = System.currentTimeMillis();
            animationHandler.postDelayed(stepAnimation, 30);
        }
    }

    public void setEvent(Event event) {
        this.event = event;
        if (this.view != null) {
            this.what.setText(event.what);
            this.time.setText(event.timeStr);
            this.who.setText(event.whoStr);
            this.descr.setText(event.descrWithHtmlParsed());
            mHostName.setText(event.host.first_name+"\n"+event.host.last_name);
            mImgLoader.DisplayImage(event.host.pic, mHostAvatar);
            //this.descr.setText(event.descr);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateRsvpButton();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.view == null) {
            this.view = inflater.inflate(R.layout.meetup, container, false);
            this.what = (TextView) this.view.findViewById(R.id.meetupWhat);
            this.what.setTypeface(Font.use("Vitesse_Medium"));
            this.time = (TextView) this.view.findViewById(R.id.meetupTime);
            this.time.setTypeface(Font.use("Karla"));
            this.who = (TextView) this.view.findViewById(R.id.meetupWho);
            this.who.setTypeface(Font.use("Karla_Bold"));
            this.descr = (TextView) this.view.findViewById(R.id.meetupDescr);
            this.descr.setTypeface(Font.use("Karla"));
            mRsvp = (Button) this.view.findViewById(R.id.rsvp);
            mRsvp.setTypeface(Font.use("Karla_Bold"));
            mAttendees = (Button) this.view.findViewById(R.id.attendees);
            mAttendees.setTypeface(Font.use("Karla"));
            mFeed = (Button) this.view.findViewById(R.id.feed);
            mFeed.setTypeface(Font.use("Karla"));
            mHostedBy = (TextView) this.view.findViewById(R.id.hostedBy);
            mHostedBy.setTypeface(Font.use("Vitesse_Bold"));
            mHostName = (TextView) this.view.findViewById(R.id.hostName);
            mHostName.setTypeface(Font.use("Karla_Bold"));
            mHostName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_profile(event.host);
                }
            });
            mHostAvatar = (ImageView) this.view.findViewById(R.id.hostAvatar);
            content = (DominationScrollView) this.view.findViewById(R.id.content);
            contentStartWeight = ((LinearLayout.LayoutParams) content.getLayoutParams()).weight;
            slidingLayout = (LinearLayout) this.view.findViewById(R.id.slidingLayout);
            weightSum = slidingLayout.getWeightSum();
            content.setScroll(false);
            mapView = (MapView) this.view.findViewById(R.id.map);
            if (this.event != null) {
                setEvent(this.event);
            }
            mAttendees.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_meetup_attendees(event.event_id);
                }
            });
            mFeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.homeFragment.setChannel("meetup", event.event_id);
                    MainActivity.self.open_dispatch();
                }
            });
            mRsvp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean isAttending = Me.isAttendingEvent(event);
                    Boolean isFull = event.isFull();
                    if (!isFull || isAttending) {
                        Me.toggleRsvp(event, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                updateRsvpButton();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                MainActivity.offlineAlert();
                            }
                        });
                    }
                }
            });
            final MeetupFragment ref = this;
            content.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downY = event.getY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            upY = event.getY();
                            float deltaY = downY - upY;
                            if (Math.abs(deltaY) > MIN_DISTANCE) {
                                if (deltaY > 0) {
                                    ref.toggleMap(false);
                                    return true;
                                }
                                if (deltaY < 0 && ref.content.getScrollY() < 1) {
                                    ref.toggleMap(true);
                                    return true;
                                }
                            } else {
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        // Setting a custom info window adapter for the google map



        return this.view;
    }

    public void updateRsvpButton() {
        mRsvp.setBackgroundColor(getActivity().getResources().getColor(R.color.blue));
        mRsvp.setTextColor(getActivity().getResources().getColor(R.color.light_tan));
        if (Me.isAttendingEvent(event)){
            mRsvp.setText("You'll be there!");
        }
        else {
            if (event.isFull()) {
                mRsvp.setText("Event Full");
                mRsvp.setBackgroundColor(getActivity().getResources().getColor(R.color.light_gray));
                mRsvp.setTextColor(getActivity().getResources().getColor(R.color.dark_gray));
            }
            else {
                mRsvp.setText("RSVP");
            }
        }
    }

    public void updateMap() {
        if (this.map != null) {
            final MeetupFragment ref = this;
            //http://wptrafficanalyzer.in/blog/customizing-infowindow-contents-in-google-map-android-api-v2-using-infowindowadapter/
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                // Use default InfoWindow frame
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                // Defines the contents of the InfoWindow
                @Override
                public View getInfoContents(Marker arg0) {
                    View v = getActivity().getLayoutInflater().inflate(R.layout.event_location_window, null);
                    TextView venue = (TextView) v.findViewById(R.id.venue);
                    venue.setTypeface(Font.use("Vitesse_Medium"));
                    TextView address = (TextView) v.findViewById(R.id.address);
                    address.setTypeface(Font.use("Karla_Italic"));
                    venue.setText(event.place);
                    address.setText(event.address);
                    return v;
                }
            });
            map.clear();
            if(event.lat != null && event.lon != null && event.address != null
                    && !event.lat.equals("null") && !event.lon.equals("null") && !event.address.equals("null")) {
                LatLng markerPos = new LatLng(Double.parseDouble(ref.event.lat), Double.parseDouble(ref.event.lon));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPos, 16));
                Marker marker = map.addMarker(new MarkerOptions()
                .title(ref.event.place)
                .snippet(ref.event.address)
                .anchor(0.5f, 1.0f)
                .position(markerPos));
                marker.showInfoWindow();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
//        this.map.setMyLocationEnabled(true);
        this.updateMap();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
