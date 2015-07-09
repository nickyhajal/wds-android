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
import android.widget.Button;
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
public class CheckinAdapter extends ArrayAdapter<HashMap>{
    public CheckinFragment mContext;
    public CheckinAdapter(Context context, ArrayList<HashMap> items) {
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.checkin_row, parent, false);
            Font.applyTo(convertView);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.checkinBtn = (Button) convertView.findViewById(R.id.checkinBtn);
            holder.checkinBtn.setTypeface(Font.use("Karla_Bold"));
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        String distance = "";
        if (place.get("distance") != null) {
            if (place.get("units").equals("ft")) {
                distance = String.valueOf(Math.round((Double) place.get("distance")));
            }
            else {
                distance = String.format("%.2f", place.get("distance"));
            }
            distance += " "+place.get("units")+" away";
        }
        holder.distance.setText(distance);
        holder.name.setText((String) place.get("name"));
        holder.checkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.checkin(place, holder.checkinBtn);
            }
        });
        if (position == 0) {
            convertView.setPadding(0, (int) (4 * MainActivity.density), 0,0);
        }
        else {
            convertView.setPadding(0, 0, 0,0);
        }
        return convertView;
    }

    private class ViewHolder {
        private TextView name;
        private Button checkinBtn;
        private TextView distance;
    }

}

