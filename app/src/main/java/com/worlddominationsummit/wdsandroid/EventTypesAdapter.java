package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by nicky on 5/19/15.
 */
public class EventTypesAdapter extends ArrayAdapter<HashMap>{
    public EventTypesFragment mContext;
    public ImageLoader mImageLoader = ImageLoader.getInstance();

    public EventTypesAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
    }
    public void refill(ArrayList<HashMap> items) {
        clear();
        addAll(items);
        notifyDataSetChanged();
    }
    public static JSONObject getEventFromEventId(String event_id) {
        JSONArray ints = Store.getJsonArray("events");
        int len = ints.length();
        for(int i = 0; i < len; i++) {
            JSONObject event = new JSONObject();
            try {
                event = ints.getJSONObject(i);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            if (event_id.equals(event.optString("event_id"))) {
                return event;
            }
        }
        return null;
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
            holder.overlay = (ImageView) convertView.findViewById(R.id.overlay);
            holder.openBtn = (ImageButton) convertView.findViewById(R.id.openBtn);
            holder.moreBtn = (ImageButton) convertView.findViewById(R.id.moreBtn);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.title.setTypeface(Font.use("Vitesse_Bold"));
            holder.descr = (TextView) convertView.findViewById(R.id.descr);
            holder.descr.setTypeface(Font.use("Karla_Italic"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (type.get("id").equals("trust")) {
            holder.openBtn.setVisibility(View.VISIBLE);
            holder.moreBtn.setVisibility(View.GONE);
        } else {
            holder.openBtn.setVisibility(View.GONE);
            holder.moreBtn.setVisibility(View.VISIBLE);
        }
        holder.title.setText((String) type.get("title"));
        holder.descr.setText((String) type.get("descr"));
        String url = "tile_"+type.get("id");
        int img = MainActivity.getImage(url);
        Display display = MainActivity.self.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = Math.round(width * 0.67f);
//        Glide.with(MainActivity.self).load(img)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.background);
//        Glide.with(MainActivity.self).load(R.drawable.faded_overlay)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(holder.overlay);
        holder.background.setImageBitmap(decodeSampledBitmapFromResource(MainActivity.self.getResources(), img, width, height));
        holder.background.setImageResource(img);
        View.OnClickListener tapListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (type.get("id").equals("meetup")) {
//                } else {
                MainActivity.self.open_events(type);
//                }
            }
        };
        holder.background.setOnClickListener(tapListener);
        convertView.setPadding(0, 0, 0,0);
        return convertView;
    }

    private class ViewHolder {
        private ImageView background;
        private ImageView overlay;
        private ImageButton moreBtn;
        private ImageButton openBtn;
        private TextView title;
        private TextView descr;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}

