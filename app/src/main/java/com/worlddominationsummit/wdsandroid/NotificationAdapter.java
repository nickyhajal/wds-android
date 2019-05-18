package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


/**
 * Created by nicky on 08/07/16.
 */
public class NotificationAdapter extends ArrayAdapter<HashMap>{
    public ImageLoader mImageLoader;
    public Boolean isLoading = false;
    public ChatFragment mContext;


    public NotificationAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final NotificationAdapter ref = this;
        final ViewHolder holder;
        final HashMap<String, Object> item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notn_row, parent, false);
            holder = new ViewHolder();
            holder.card = (LinearLayout) convertView.findViewById(R.id.card);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Karla_Bold"));
            holder.timestamp = (RelativeTimeTextView) convertView.findViewById(R.id.timestamp);
            holder.timestamp.setTypeface(Font.use("Karla"));
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.content.setVisibility(View.GONE);
            holder.content.setTypeface(Font.use("Karla"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (item.get("clicked").equals("1")) {
            holder.card.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.tan));
        } else {
            holder.card.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sel_green));
        }
        mImageLoader.displayImage("https://avatar.wds.fm/"+((HashMap)item.get("from")).get("user_id")+"?width=78", holder.avatar);
        holder.name.setText((String) item.get("text"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        long created_at = 0;
        try {
            TimeZone tz = TimeZone.getDefault();
            Date now = new Date();
            long offsetFromUtc = tz.getOffset(now.getTime());
            created_at = formatter.parse(((String) item.get("created_at"))).getTime() + offsetFromUtc - 3000;
        } catch (ParseException e) {
            Log.e("WDS", "Parse Exception", e);
        }
        holder.timestamp.setReferenceTime(created_at);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Notification n = Notification.fromHashMap((HashMap) item);
                n.open();
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private TextView name;
        private TextView channel;
        private LinearLayout card;
        private LinearLayout buttons;
        private RelativeTimeTextView timestamp;
        private ImageView avatar;
        private TextView content;
    }

}

