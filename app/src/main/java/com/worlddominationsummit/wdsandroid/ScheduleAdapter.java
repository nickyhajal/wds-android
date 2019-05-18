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
import android.widget.BaseAdapter;
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

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ScheduleAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    public String day;
    public int numItems;
    public ArrayList<String> sections;
    public JSONArray items;
    public FragmentActivity context;

    public ScheduleAdapter(FragmentActivity activity) {
        this.context = activity;
        this.sections = new ArrayList<String>();
        this.numItems = 0;
        this.day = "2014-07-10";
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setItems(JSONArray allItems) {
        this.items = new JSONArray();
        String lastTime = "";
        if (allItems != null) {
            for (int i = 0; i < allItems.length(); i++) {
                JSONObject ev = allItems.optJSONObject(i);
                Event evo = Event.Companion.fromJson(ev);
                HashMap<String, String> evh = new HashMap<String, String>();
                try {
                    evh = (HashMap) JsonHelper.toMap(ev);
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
                if (ev.optString("startDay").equals(this.day) && Me.isAttendingEvent(evh)) {
                    this.items.put(ev);
                    if (!lastTime.equals(ev.optString("startStr"))) {
                        this.sections.add(ev.optString("startStr"));
                    }
                    lastTime = ev.optString("startStr");
                }
            }
        }
    }


    @Override
    public int getCount() {
        return this.items.length();
    }

    @Override
    public Object getItem(int position) {
        return this.items.opt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public long getHeaderId(int position) {
        JSONObject evo = this.items.optJSONObject(position);
        Event ev = Event.Companion.fromJson(evo);

        return this.sections.indexOf(ev.getStartStr());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        final Event event = Event.Companion.fromJson((JSONObject) this.items.opt(position));
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
                Uri.parse("http://maps.google.com/maps?daddr="+ event.getLat() +","+ event.getLon()));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                context.startActivity(intent);
            }
        };
        holder.navBtn.setOnClickListener(navListener);
        holder.place.setOnClickListener(navListener);
        String name = event.getWhat();
        if (event.getDescr().length() > 0 || event.getType().equals("meetup") || event.getType().equals("academy") || event.getType().equals("spark_session") || event.getType().equals("activity")) {
            String type = event.getType();
            if (!type.equals("program")) {
                JSONObject typeObj = EventTypes.byId.optJSONObject(type);
                String typeName = typeObj.optString("singular");
                if (typeName.length() > 0) {
                    name = typeName + ": " + name;
                }
            }
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
        holder.place.setText(event.getPlace());
        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
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
        long id = getHeaderId(position);
        holder.time.setText(this.sections.get((int) id));
        return convertView;
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
