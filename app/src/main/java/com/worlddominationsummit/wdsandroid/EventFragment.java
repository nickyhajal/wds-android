package com.worlddominationsummit.wdsandroid;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

/**
 * Created by nicky on 5/18/15.
 */
public class EventFragment extends Fragment implements OnMapReadyCallback {
    public View view;
    MapView mapView;
    public LinearLayout slidingLayout;
    public LinearLayout mHostedArea;
    public DominationScrollView content;
    public TextView what;
    public TextView time;
    public TextView who;
    public TextView descr;
    public TextView mHostedBy;
    public TextView mHostName;
    public TextView mHost2Name;
    public TextView mVenue;
    public TextView mAddr;
    public TextView mVNotes;
    public Button mRsvp;
    public Button mAttendees;
    public Button mFeed;
    public ImageView mHostAvatar;
    public ImageView mHost2Avatar;
    public ImageLoader mImgLoader = ImageLoader.getInstance();
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
            this.what.setText(event.getWhat());
            this.time.setText(event.getTimeStr()+"\nat "+event.getPlace());
            this.who.setText(Html.fromHtml(event.getWhoStr()).toString());
            this.descr.setText(event.descrSpecial());
//            mVenue.setText("at "+ event.getPlace());
            if (event.getAddress() != null && event.getAddress().length() > 0 && !event.getAddress().equals("null")) {
                mAddr.setText(event.getAddress());
                mAddr.setVisibility(View.VISIBLE);
            } else {
                mAddr.setVisibility(View.GONE);
            }
            if (event.getVenueNote() != null && event.getVenueNote().length() > 0 && !event.getVenueNote().equals("null")) {
                mVNotes.setText(event.getVenueNote());
                mVNotes.setVisibility(View.VISIBLE);

            } else {
                mVNotes.setVisibility(View.GONE);
            }
            if (event.getHost() != null && event.getHost().first_name != null && event.getHost().first_name.length() > 0  && event.getHost().last_name != null) {
                mHostName.setText(event.getHost().first_name+"\n"+ event.getHost().last_name);
                mImgLoader.displayImage(event.getHost().pic, mHostAvatar);
                mHostName.setVisibility(View.VISIBLE);
                mHostAvatar.setVisibility(View.VISIBLE);
                mHostedArea.setVisibility(View.VISIBLE);
            }
            else {
                mHostName.setText("");
                mHostName.setVisibility(View.GONE);
                mHostAvatar.setVisibility(View.GONE);
                mHostedArea.setVisibility(View.GONE);
            }
            if (event.getHost2() != null && event.getHost2().first_name != null && event.getHost2().first_name.length() > 0 && event.getHost2().last_name != null) {
                mHost2Name.setText(event.getHost2().first_name+"\n"+ event.getHost2().last_name);
                mImgLoader.displayImage(event.getHost2().pic, mHost2Avatar);
                mHost2Name.setVisibility(View.VISIBLE);
                mHost2Avatar.setVisibility(View.VISIBLE);
            }
            else {
                mHost2Name.setText("");
                mHost2Name.setVisibility(View.GONE);
                mHost2Avatar.setVisibility(View.GONE);
            }
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
            this.view = inflater.inflate(R.layout.event, container, false);
            this.what = (TextView) this.view.findViewById(R.id.meetupWhat);
            this.what.setTypeface(Font.use("Vitesse_Medium"));
            this.time = (TextView) this.view.findViewById(R.id.meetupTime);
            this.time.setTypeface(Font.use("Karla_Bold"));
//            mVenue = (TextView) this.view.findViewById(R.id.meetupVenue);
//            mVenue.setTypeface(Font.use("Karla_Bold"));
            mHostedArea= (LinearLayout) this.view.findViewById(R.id.hostedArea);
            mAddr = (TextView) this.view.findViewById(R.id.meetupAddr);
            mAddr.setTypeface(Font.use("Karla"));
            mVNotes = (TextView) this.view.findViewById(R.id.meetupVenueNotes);
            mVNotes.setTypeface(Font.use("Karla_Italic"));
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
                    MainActivity.self.open_profile(event.getHost());
                }
            });
            mHostAvatar = (ImageView) this.view.findViewById(R.id.hostAvatar);
            mHost2Name = (TextView) this.view.findViewById(R.id.host2Name);
            mHost2Name.setTypeface(Font.use("Karla_Bold"));
            mHost2Name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_profile(event.getHost2());
                }
            });
            mHost2Avatar = (ImageView) this.view.findViewById(R.id.host2Avatar);
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
                MainActivity.self.open_event_attendees(event.getEvent_id());
                }
            });
            mFeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String channel = event.getType().equals("ambassador") ? "ambassador" : "meetup";
                    MainActivity.self.homeFragment.setChannel(channel, event.getEvent_id());
                    MainActivity.self.open_dispatch();
                }
            });
            if (event.getType().compareTo("program") == 0) {
                mRsvp.setVisibility(View.GONE);
            } else {
                mRsvp.setVisibility(View.VISIBLE);
            }
            mRsvp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean isAttending = Me.isAttendingEvent(event);
                    Boolean isFull = event.isFull();
                    RsvpDialog dialog = new RsvpDialog();
                    dialog.setEvent(event, isAttending);
                    if (!isAttending || (isAttending && event.isCancelable())) {
                        if ((isAttending) || (!isAttending && !isFull)) {
                            dialog.show(MainActivity.self.getFragmentManager(), "rsvpdialog");
                        }
                    }
//
//                    if (!isFull && (!isAttending || !event.getType().equals("academy") || !event.isPurchase())) {
//                        dialog.show(MainActivity.self.getFragmentManager(), "rsvpdialog");
//                    }
                }
            });
            final EventFragment ref = this;
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
        if (mRsvp != null && getActivity() != null && event != null && event.getType() != null) {
            mRsvp.setBackgroundColor(getActivity().getResources().getColor(R.color.blue));
            mRsvp.setTextColor(getActivity().getResources().getColor(R.color.light_tan));
            if (Me.isAttendingEvent(event)) {
                mRsvp.setText(event.isCancelable() ? "Attending! - Tap to unRSVP" : "Attending!");
            } else {
                if (event.isFull()) {
                    mRsvp.setText("Event Full");
                    mRsvp.setBackgroundColor(getActivity().getResources().getColor(R.color.light_gray));
                    mRsvp.setTextColor(getActivity().getResources().getColor(R.color.dark_gray));
                } else {
                    if (event.getType().equals("academy")) {
                        mRsvp.setText("Attend");
                    }
                    else if (event.getType().equals("activity")) {
                        mRsvp.setText("Add to Schedule");
                    } else {
                        mRsvp.setText("RSVP");
                    }
                }
            }
        }
    }

    public void updateMap() {
        if (this.map != null) {
            final EventFragment ref = this;
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
                    venue.setText(event.getPlace());
                    address.setText(event.getAddress());
                    return v;
                }
            });
            map.clear();
            if(event.getLat() != null && event.getLon() != null && event.getAddress() != null
                    && !event.getLat().equals("null") && !event.getLon().equals("null") && !event.getAddress().equals("null")) {
                LatLng markerPos = new LatLng(Double.parseDouble(ref.event.getLat()), Double.parseDouble(ref.event.getLon()));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPos, 16));
                Marker marker = map.addMarker(new MarkerOptions()
                .title(ref.event.getPlace())
                .snippet(ref.event.getAddress())
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
