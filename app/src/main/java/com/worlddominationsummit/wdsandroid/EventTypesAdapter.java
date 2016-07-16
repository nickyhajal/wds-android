package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
public class EventTypesAdapter extends ArrayAdapter<HashMap>{
    public EventTypesFragment mContext;
    public ImageLoader mImageLoader = new ImageLoader(MainActivity.self);

    public EventTypesAdapter(Context context, ArrayList<HashMap> items) {
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
        final HashMap type = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_type_row, parent, false);
            Font.applyTo(convertView);
            holder = new ViewHolder();
            holder.background = (ImageView) convertView.findViewById(R.id.background);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.title.setTypeface(Font.use("Vitesse_Bold"));
            holder.descr = (TextView) convertView.findViewById(R.id.descr);
            holder.descr.setTypeface(Font.use("Karla_Italic"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText((String) type.get("title"));
        holder.descr.setText((String) type.get("descr"));
        String url = "tile_"+type.get("id");
        int img = MainActivity.getImage(url);
        holder.background.setImageResource(img);
        View.OnClickListener tapListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.self.open_events(type);
            }
        };
        holder.background.setOnClickListener(tapListener);
        convertView.setPadding(0, 0, 0,0);
        return convertView;
    }

    private class ViewHolder {
        private ImageView background;
        private TextView title;
        private TextView descr;
    }

}

