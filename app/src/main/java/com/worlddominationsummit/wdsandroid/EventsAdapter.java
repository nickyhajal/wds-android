package com.worlddominationsummit.wdsandroid;
/**
 * Created by nicky on 5/19/15.
 */

import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.applidium.headerlistview.SectionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class EventsAdapter extends SectionAdapter {

    public String day;
    public int numItems;
    public ArrayList<String> sections;
    public JSONArray items;
    public FragmentActivity context;
    public String mState;
    public String mType;
    public String mMeetupType;

    public EventsAdapter(FragmentActivity activity) {
        this.context = activity;
        this.sections = new ArrayList<String>();
        this.numItems = 0;
        this.day = "2014-07-10";
        mType = "meetup";
        mMeetupType = "all";
    }

    public void setState(String state) {
        mState = state;
    }
    public void setType(String typeId) {
        mType = typeId;
    }
    public void setMeetupType(String meetupType) {
        mMeetupType = meetupType;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public void setItems(JSONArray items) {
        this.items = new JSONArray();
        if (items == null) {
            this.numItems = 0;
            return;
        }
        this.numItems = items.length();
        int sectionIndex = -1;
        String lastTime = "";
        for(int i = 0; i < this.numItems; i++) {
            JSONObject ev = items.optJSONObject(i);
            Event evo = Event.fromJson(ev);
            HashMap<String, String> evh = new HashMap<String, String>();
            try {
                evh = (HashMap) JsonHelper.toMap(ev);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            if (ev.optString("startDay").equals(this.day)) {
                if(mState.equals("attending") && !Me.isAttendingEvent(evh)) {
                    continue;
                }
                if(mState.equals("suggested")) {
                    JSONArray because = Me.isInterestedInEvent(evo);
                    if (because.length() > 0) {
                        try {
                            ev.put("because", because);
                        } catch (JSONException e) {
                            Log.e("WDS", "Json Exception", e);
                        }
                    }
                    else {
                        continue;
                    }
                }
                else {
                    ev.remove("because");
                }
                if (ev.optString("type", "meetup").equals(mType)) {
                    if (mType.equals("meetup") && (!mMeetupType.equals("all") && !ev.optString("format", "discover").equals(mMeetupType))) {
                        continue;
                    }
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
    }


    @Override
    public int numberOfSections() {
        return this.sections.size();
    }

    @Override
    public int numberOfRows(int section) {
        if(section > - 1 && section < this.sections.size()) {
            return this.items.optJSONArray(section).length();
        }
        else {
            return 0;
        }
    }

    @Override
    public JSONObject getRowItem(int section, int row) {
        return this.items.optJSONArray(section).optJSONObject(row);
    }

    @Override
    public boolean hasSectionHeaderView(int section) {
        return true;
    }

    @Override
    public View getRowView(int section, int row, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        JSONObject ev = getRowItem(section, row);
        final Event event = Event.fromJson(ev);
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.event_row, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Medium"));
            holder.who = (TextView) convertView.findViewById(R.id.who);
            holder.who.setTypeface(Font.use("Karla"));
            holder.because = (TextView) convertView.findViewById(R.id.because);
            holder.because.setTypeface(Font.use("Karla_Italic"));
            holder.rsvp = (Button) convertView.findViewById(R.id.rsvp);
            holder.rsvp.setTypeface(Font.use("Karla_Bold"));
            holder.details = (Button) convertView.findViewById(R.id.more_details);
            holder.details.setTypeface(Font.use("Karla_Bold"));
            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(event.what);
        holder.who.setText(this.getWho(event));
        holder.rsvp.setTextColor(context.getResources().getColor(R.color.orange));
        if (Me.isAttendingEvent(event)){
            if (event.type.equals("academy")) {
                holder.rsvp.setText("You're Attending!");
            }
            else {
                holder.rsvp.setText("unRSVP");
            }
        }
        else {
            if (event.isFull()) {
                holder.rsvp.setText("Event Full");
                holder.rsvp.setTextColor(context.getResources().getColor(R.color.dark_gray));
            }
            else {
                if (event.type.equals("academy")) {
                    holder.rsvp.setText("Attend");
                } else {
                    holder.rsvp.setText("RSVP");
                }
            }
        }
        if (ev.has("because")) {
            event.setBecause(ev.optJSONArray("because"));
            holder.because.setText("Because you're interested in "+event.becauseStr);
            holder.because.setVisibility(View.VISIBLE);
        } else {
            holder.because.setVisibility(View.GONE);
        }

        final Button rsvp = holder.rsvp;

        holder.rsvp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isAttending = Me.isAttendingEvent(event);
                Boolean isFull = event.isFull();
                RsvpDialog dialog = new RsvpDialog();
                dialog.setEvent(event, isAttending);
                if (!isFull || !event.type.equals("academy")) {
                    dialog.show(MainActivity.self.getFragmentManager(), "rsvpdialog");
                }
            }
        });
        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.self.open_event(event);
            }
        });
        return convertView;
    }

    private String getWho(Event event) {
        String typeId = event.type;
        String eventType = EventTypes.byId.optString("singular", "meetup").toLowerCase();
        if (typeId.equals("academy")) {
            if (event.descr != null && event.descr.length() > 0) {
                String descr = event.descr;
                int max = 200;
                if (descr.length() > max) {
                    descr = descr.substring(0, (max-3))+"...";
                }
                return descr;
            }
        }
        return event.whoStr;
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
        }
        else {
            holder = (HeaderHolder) convertView.getTag();
        }
        if (this.sections != null && section < this.sections.size() && this.sections.get(section) != null) {
            holder.time.setText(this.sections.get(section));
        }
        return convertView;
    }



    @Override
    public void onRowItemClick(AdapterView<?> parent, View view, int section, int row, long id) {
        super.onRowItemClick(parent, view, section, row, id);
    }

    private class ViewHolder {
        private TextView name;
        private TextView who;
        private TextView because;
        private Button rsvp;
        private Button details;
    }

    private class HeaderHolder {
        private TextView time;
    }

}
