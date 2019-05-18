package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by nicky on 5/19/15.
 */
public class PlacesAdapter extends ArrayAdapter<HashMap>{
    public Boolean isLoading = false;
    public ExploreFragment mContext;
    public PlacesAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
    }
    public void refill(ArrayList<HashMap> items) {
        clear();
        addAll(items);
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final HashMap place = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.place_row, parent, false);
            Font.applyTo(convertView);
            holder = new ViewHolder();
            holder.card = (LinearLayout) convertView.findViewById(R.id.card);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.moreBtn = (ImageButton) convertView.findViewById(R.id.moreBtn);
            holder.navBtn = (ImageButton) convertView.findViewById(R.id.navBtn);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        String distance = "";
        String checkins = "";
        if (place.get("distance") != null) {
            if (place.get("units").equals("ft")) {
                distance = String.valueOf(Math.round((Double) place.get("distance")));
            }
            else {
               distance = String.format("%.2f", place.get("distance"));
            }
            distance += " "+place.get("units")+" away";
        }
        if (place.get("checkins") != null  && Integer.parseInt((String)place.get("checkins")) > 1) {
            checkins = (String) place.get("checkins");
            if (checkins.length() > 0) {
                if (!distance.equals("")) {
                   checkins = " - "+checkins;
                }
                checkins += " WDSers there now";
            }
        }
        holder.name.setText((String) place.get("name"));
        holder.address.setText((String) place.get("address"));
        if (distance.length() > 0) {
            holder.distance.setText((distance+checkins));
            holder.distance.setVisibility(View.VISIBLE);
        } else {
            holder.distance.setVisibility(View.GONE);
        }
        if (place.get("descr") != null && ((String) place.get("descr")).length() > 0) {
            holder.moreBtn.setVisibility(View.VISIBLE);
        } else {
            holder.moreBtn.setVisibility(View.GONE);
        }
        View.OnClickListener navListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = place.get("lat") == null ? "" : (String) place.get("lat");
                String lon = place.get("lon") == null ? "" : (String) place.get("lon");

                if (lat.length() > 0 && lon.length() > 0) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lon));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    MainActivity.self.startActivity(intent);
                }
            }
        };
        holder.navBtn.setOnClickListener(navListener);
        holder.address.setOnClickListener(navListener);
        if (position == 0) {
            convertView.setPadding(0, (int) (4 * MainActivity.density), 0,0);
        }
        else {
            convertView.setPadding(0, 0, 0,0);
        }
        return convertView;
    }

    private class ViewHolder {
        private LinearLayout card;
        private TextView name;
        private ImageButton moreBtn;
        private ImageButton navBtn;
        private TextView address;
        private TextView distance;
    }

}

