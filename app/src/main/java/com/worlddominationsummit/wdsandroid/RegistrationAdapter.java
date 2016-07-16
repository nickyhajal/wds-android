package com.worlddominationsummit.wdsandroid;
/**
 * Created by nicky on 5/19/15.
 */

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applidium.headerlistview.SectionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RegistrationAdapter extends SectionAdapter {

    public String day;
    public int numItems;
    public ArrayList<String> sections;
    public JSONArray items;
    public FragmentActivity context;

    public RegistrationAdapter(FragmentActivity activity) {
        this.context = activity;
        this.sections = new ArrayList<String>();
        this.numItems = 0;
        this.day = "2016-08-11";
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setItems(JSONArray items) {
        this.items = new JSONArray();
        this.numItems = items.length();
        int sectionIndex = -1;
        String lastTime = "";
        for (int i = 0; i < this.numItems; i++) {
            JSONObject ev = items.optJSONObject(i);
            Event evo = Event.fromJson(ev);
            HashMap<String, String> evh = new HashMap<String, String>();
            try {
                evh = (HashMap) JsonHelper.toMap(ev);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            if (ev.optString("startDay").equals(this.day) && ev.optString("type").equals("registration")) {
                if (!lastTime.equals(ev.optString("startStr"))) {
                    sectionIndex += 1;
                    this.items.put(new JSONArray());
                    this.sections.add(ev.optString("startStr"));
                }
                this.items.optJSONArray(sectionIndex).put(ev);
                lastTime = ev.optString("startStr");
            }

        }
    }


    @Override
    public int numberOfSections() {
        return this.sections.size();
    }

    @Override
    public int numberOfRows(int section) {
        if (section > -1 && section < this.sections.size()) {
            return this.items.optJSONArray(section).length();
        } else {
            return 0;
        }
    }

    @Override
    public Object getRowItem(int section, int row) {
        return this.items.optJSONArray(section).optJSONObject(row);
    }

    @Override
    public boolean hasSectionHeaderView(int section) {
        return true;
    }

    @Override
    public View getRowView(int section, int row, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        final Event event = Event.fromJson((JSONObject) getRowItem(section, row));
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.schedule_row, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Medium"));
            holder.place = (TextView) convertView.findViewById(R.id.place);
            holder.place.setTypeface(Font.use("Karla"));
            holder.moreBtn = (ImageButton) convertView.findViewById(R.id.moreBtn);
            holder.navBtn = (ImageButton) convertView.findViewById(R.id.navBtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        View.OnClickListener navListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+event.lat+","+event.lon));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                context.startActivity(intent);
            }
        };
        holder.navBtn.setOnClickListener(navListener);
        holder.place.setOnClickListener(navListener);
        String name = event.what;
        if (event.type.equals("meetup")) {
            name = "Meetup: " + name;
        } else if (event.type.equals("academy")) {
            name = "Academy: " + name;
        }
        if (event.type.equals("meetup")) {
            holder.moreBtn.setVisibility(View.VISIBLE);
            holder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_event(event);
                }
            });
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_event(event);
                }
            });
        } else {
            holder.moreBtn.setVisibility(View.GONE);
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        holder.name.setText(name);
        holder.place.setText(event.place);
        return convertView;
    }

    @Override
    public int getSectionHeaderViewTypeCount() {
        return 2;
    }

    @Override
    public int getSectionHeaderItemViewType(int section) {
        return 1;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        HeaderHolder holder;
        if (convertView == null) {
            holder = new HeaderHolder();
            convertView = this.context.getLayoutInflater().inflate(this.context.getResources().getLayout(R.layout.list_header), null);
            holder.time = ((TextView) convertView.findViewById(R.id.time));
            holder.time.setTypeface(Font.use("Vitesse_Medium"));
            convertView.setTag(holder);
        } else {
            holder = (HeaderHolder) convertView.getTag();
        }
        holder.time.setText(this.sections.get(section));
        return convertView;
    }

    @Override
    public void onRowItemClick(AdapterView<?> parent, View view, int section, int row, long id) {
        super.onRowItemClick(parent, view, section, row, id);
    }

    private class ViewHolder {
        private TextView name;
        private ImageButton moreBtn;
        private ImageButton navBtn;
        private TextView place;
    }

    private class HeaderHolder {
        private TextView time;
    }

}
