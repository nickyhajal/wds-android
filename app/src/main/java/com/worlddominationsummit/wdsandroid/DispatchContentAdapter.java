package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Handler;
import android.text.util.Linkify;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by nicky on 5/19/15.
 */
public class DispatchContentAdapter extends ArrayAdapter<HashMap>{
    public ImageLoader mImageLoader;
    public Boolean isLoading = false;
    public DispatchContentFragment mContext;
    public DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .showImageOnLoading(R.drawable.gray_dots)
                .considerExifParams(true)
                .build();
    public DispatchContentAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
        mImageLoader = ImageLoader.getInstance();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DispatchContentAdapter ref = this;
        final ViewHolder holder;
        final int p = position;
        final HashMap itemMap = getItem(position);
        final DispatchItem item = DispatchItem.fromHashMap(getItem(position));
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dispatch_row, parent, false);
            holder = new ViewHolder();
            holder.card = (LinearLayout) convertView.findViewById(R.id.card);
            holder.buttons = (LinearLayout) convertView.findViewById(R.id.buttons);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Bold"));
            holder.timestamp = (RelativeTimeTextView) convertView.findViewById(R.id.timestamp);
            holder.timestamp.setTypeface(Font.use("Karla"));
            holder.channel = (TextView) convertView.findViewById(R.id.channel);
            holder.channel.setTypeface(Font.use("Karla_Italic"));
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.media = (ImageView) convertView.findViewById(R.id.media);
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
        if (item.media != null && !item.media.equals("null") && item.media.length() > 0) {
            mImageLoader.displayImage("https://photos.wds.fm/media/"+item.media+"_large", holder.media, mDisplayImageOptions);
            holder.media.setVisibility(View.VISIBLE);
        } else {
            holder.media.setVisibility(View.GONE);
        }
        mImageLoader.displayImage(item.author.pic, holder.avatar);
        holder.name.setText(item.author.full_name);
        holder.content.setText(item.content);
        Linkify.addLinks(holder.content, Linkify.WEB_URLS);
        if (item.channel.length() > 0) {
            holder.channel.setText(" - " + item.channel);
        }
        else {
            holder.channel.setText("");
        }
        holder.timestamp.setReferenceTime(item.created_at);
        holder.num_likes.setText(item.num_likes_str);
        holder.num_likes.setTag(item.feed_id);
        holder.num_comments.setVisibility(View.GONE);
        holder.num_likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed_id = (String) v.getTag();
                Me.toggleLike(feed_id, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject rsp) {
                        getItem(p).put("num_likes", rsp.optString("num_likes"));
                        ref.notifyDataSetChanged();
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MainActivity.offlineAlert();
                    }
                });
            }
        });
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.self.open_profile(item.author);
            }
        });
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.self.open_profile(item.author);
            }
        });
        if (p == 0) {
            holder.buttons.setVisibility(View.VISIBLE);
            holder.card.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        else {
            holder.buttons.setVisibility(View.GONE);
            holder.card.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        if (p > 1) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.card.getLayoutParams();
            params.topMargin = 0;
            holder.card.setLayoutParams(params);
        }
        else {
            float density = MainActivity.density;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.card.getLayoutParams();
            params.topMargin = (int) (4 * density);
            holder.card.setLayoutParams(params);
        }
        int count = getCount();
        if (count > 1 && p == (count-1)) {
            float density = MainActivity.density;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.card.getLayoutParams();
            params.bottomMargin = (int) (4 * density);
            holder.card.setLayoutParams(params);
        }
        return convertView;
    }

    private class ViewHolder {
        private TextView name;
        private LinearLayout card;
        private LinearLayout buttons;
        private TextView channel;
        private RelativeTimeTextView timestamp;
        private ImageView avatar;
        private ImageView media;
        private TextView content;
        private TextView num_likes;
        private TextView num_comments;
    }

}

