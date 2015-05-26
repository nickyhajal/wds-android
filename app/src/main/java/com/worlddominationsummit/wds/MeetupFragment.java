package com.worlddominationsummit.wds;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
            Puts.i(event.descrWithHtmlParsed().toString());
            this.descr.setText(event.descrWithHtmlParsed());
            this.descr.setText(event.descr);
        }
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
            content = (DominationScrollView) this.view.findViewById(R.id.content);
            contentStartWeight = ((LinearLayout.LayoutParams) content.getLayoutParams()).weight;
            slidingLayout = (LinearLayout) this.view.findViewById(R.id.slidingLayout);
            weightSum = slidingLayout.getWeightSum();
            content.setScroll(false);
            mapView = (MapView) this.view.findViewById(R.id.map);
            if (this.event != null) {
                setEvent(this.event);
            }
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
                            Puts.i(ref.content.getScrollY());
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
        return this.view;
    }

    public void updateMap() {
        if (this.map != null) {
            final MeetupFragment ref = this;
            LatLng sydney = new LatLng(Double.parseDouble(ref.event.lat), Double.parseDouble(ref.event.lon));
            ref.map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));
            ref.map.addMarker(new MarkerOptions()
                    .title(ref.event.place)
                    .snippet(ref.event.address)
                    .position(sydney));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setMyLocationEnabled(true);
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
