package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.loopj.android.image.SmartImageView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by nicky on 5/19/15.
 */
public class AttendeeSearchAdapter extends ArrayAdapter<HashMap>{
    public ImageLoader mImageLoader = new ImageLoader(MainActivity.self);
    public AttendeeSearchAdapter(Context context, ArrayList<HashMap> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Attendee user = Attendee.fromHashMap(getItem(position));
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.attendee_search_row, parent, false);
        holder = new ViewHolder();
        holder.name = (TextView) convertView.findViewById(R.id.name);
        holder.name.setTypeface(Font.use("Karla"));
        holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
        convertView.setTag(holder);
        mImageLoader.DisplayImage(user.getPic(126), holder.avatar);
        holder.name.setText(user.full_name);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.self.open_profile_from_search(user);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private TextView name;
        private ImageView avatar;
    }

}

