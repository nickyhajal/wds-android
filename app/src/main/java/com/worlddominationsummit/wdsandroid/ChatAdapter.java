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

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by nicky on 08/07/16.
 */
public class ChatAdapter extends ArrayAdapter<HashMap>{
    public ImageLoader mImageLoader;
    public Boolean isLoading = false;
    public ChatFragment mContext;

    public ChatAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
        mImageLoader = new ImageLoader(context.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatAdapter ref = this;
        final ViewHolder holder;
        final HashMap<String, Object> item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_row, parent, false);
            holder = new ViewHolder();
            holder.card = (LinearLayout) convertView.findViewById(R.id.card);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Bold"));
            holder.timestamp = (RelativeTimeTextView) convertView.findViewById(R.id.timestamp);
            holder.timestamp.setTypeface(Font.use("Karla"));
            holder.channel = (TextView) convertView.findViewById(R.id.channel);
            holder.channel.setTypeface(Font.use("Karla_Italic"));
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.content.setTypeface(Font.use("Karla"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        mImageLoader.DisplayImage("http://avatar.wds.fm/"+item.get("user_id")+"?width=78", holder.avatar);
        holder.name.setText(item.get("first_name")+" "+((String)item.get("last_name")).substring(0, 1));
        holder.content.setText((String)item.get("msg"));
        Linkify.addLinks(holder.content, Linkify.WEB_URLS);
        holder.timestamp.setReferenceTime((long) item.get("created_at"));
//        holder.name.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MainActivity.self.open_profile(item.author);
//            }
//        });
//        holder.avatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MainActivity.self.open_profile(item.author);
//            }
//        });
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

