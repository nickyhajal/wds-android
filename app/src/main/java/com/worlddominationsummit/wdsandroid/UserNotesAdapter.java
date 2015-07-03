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
 * Created by nicky on 5/19/15.
 */
public class UserNotesAdapter extends ArrayAdapter<HashMap>{
    public Boolean isLoading = false;
    public UserNotesFragment mContext;
    public UserNotesAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UserNotesAdapter ref = this;
        final ViewHolder holder;
        final HashMap unote = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_note, parent, false);
            holder = new ViewHolder();
            holder.card = (LinearLayout) convertView.findViewById(R.id.card);
            holder.note = (TextView) convertView.findViewById(R.id.note);
            holder.note.setTypeface(Font.use("Karla"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        Puts.i(unote.toString());
        Puts.i((String) unote.get("note"));
        holder.note.setText((String) unote.get("note"));
        return convertView;
    }

    private class ViewHolder {
        private LinearLayout card;
        private TextView note;
    }

}

