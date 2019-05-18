package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
public class CommunitiesAdapter extends ArrayAdapter<HashMap>{
    public Context mContext;
    public CommunitiesAdapter(Context context, ArrayList<HashMap> items) {
        super(context, 0, items);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        HashMap<String, String> item = getItem(position);
        final String interest_id = String.valueOf(item.get("interest_id"));
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.community_row, parent, false);
            holder = new ViewHolder();
            holder.content = (LinearLayout) convertView.findViewById(R.id.content);
            holder.interest = (TextView) convertView.findViewById(R.id.interest);
            holder.interest.setTypeface(Font.use("Vitesse_Bold"));
            holder.size = (TextView) convertView.findViewById(R.id.size);
            holder.size.setTypeface(Font.use("Karla"));
            holder.join = (Button) convertView.findViewById(R.id.join);
            holder.join.setTypeface(Font.use("Karla_Bold"));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.content.setTag(interest_id);
        holder.interest.setText(item.get("interest"));
        holder.size.setText(String.valueOf(item.get("members")) + " members");
        updateJoinButton(holder.join, interest_id);
        holder.join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Me.joinCommunity(interest_id, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        holder.size.setText(jsonObject.optString("members", "100") +" members");
                        updateJoinButton(holder.join, interest_id);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        MainActivity.offlineAlert();
                    }
                });
            }
        });
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.self.open_community((String) v.getTag());
            }
        });
        return convertView;
    }

    public void updateJoinButton(Button btn, String interest_id) {
        if (Me.isInterested(interest_id)) {
            btn.setText("Joined");
            btn.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        }
        else {
            btn.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            btn.setText("Join");
        }
    }

    private class ViewHolder {
        private TextView interest;
        private TextView size;
        private Button join;
        private LinearLayout content;
    }

}

