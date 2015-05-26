package com.worlddominationsummit.wds;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.*;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.loopj.android.image.SmartImage;
import com.loopj.android.image.SmartImageView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by nicky on 5/19/15.
 */
public class DispatchAdapter extends ArrayAdapter<HashMap>{
    public ImageLoader mImageLoader;
    public Boolean isLoading = false;
    public DispatchAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
        mImageLoader = new ImageLoader(context.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DispatchAdapter ref = this;
        final ViewHolder holder;
        final int p = position;
        float percent = p/this.getCount();
        if (percent > 0.75f && !this.isLoading) {
            this.isLoading = false;
            new Handler().post(new Runnable() {
                public void run() {
                    ref.isLoading = true;
                    // FETCH MORE CONTENT
                    // THEN MAKE isLOADING FALSE AGAIN
                }
            });
        }
        final DispatchItem item = DispatchItem.fromHashMap(getItem(position));
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Bold"));
            holder.timestamp = (RelativeTimeTextView) convertView.findViewById(R.id.timestamp);
            holder.timestamp.setTypeface(Font.use("Karla"));
            holder.channel = (TextView) convertView.findViewById(R.id.channel);
            holder.channel.setTypeface(Font.use("Karla_Italic"));
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.content.setTypeface(Font.use("Karla"));
            holder.num_likes = (TextView) convertView.findViewById(R.id.num_likes);
            holder.num_likes.setTypeface(Font.use("Karla_Bold"));
            holder.num_comments = (TextView) convertView.findViewById(R.id.num_comments);
            holder.num_comments.setTypeface(Font.use("Karla_Bold"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        mImageLoader.DisplayImage(item.author.pic, holder.avatar);
        holder.name.setText(item.author.full_name);
        holder.content.setText(item.content);
        holder.channel.setText(" - " + item.channel);
        holder.timestamp.setReferenceTime(item.created_at);
        holder.num_likes.setText(item.num_likes_str);
        holder.num_likes.setTag(item.feed_id);
        holder.num_comments.setText(item.num_comments_str);
        holder.num_comments.setTag(item.feed_id);
        holder.num_likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed_id = (String) v.getTag();
                Me.toggleLike(feed_id, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject rsp) {
                        Puts.i("RESPONSEEE");
                        Puts.i(rsp.optString("num_likes"));
                        getItem(p).put("num_likes", rsp.optString("num_likes"));
                        //holder.num_likes.setText(rsp.optString("num_likes"));
                        //item.num_likes = rsp.optString("num_likes");
                        ref.notifyDataSetChanged();
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Server error response
                    }
                });
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private TextView name;
        private TextView channel;
        private RelativeTimeTextView timestamp;
        private ImageView avatar;
        private TextView content;
        private TextView num_likes;
        private TextView num_comments;
    }

}

