package com.worlddominationsummit.wdsandroid;
/**
 * Created by nicky on 5/19/15.
 */

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.applidium.headerlistview.SectionAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class EventsAdapter extends BaseAdapter implements StickyListHeadersAdapter {


    public String day;
    public int numItems;
    public ArrayList<String> sections;
    public JSONArray items;
    public FragmentActivity context;
    public String mState;
    public String mType;
    public String mMeetupType;
    public Boolean mClearHeader = false;

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
    public void setItems(JSONArray allItems) {
        this.items = new JSONArray();
        if (items == null) {
            this.numItems = 0;
            return;
        }
        this.numItems = allItems.length();
        String lastTime = "";
        for(int i = 0; i < this.numItems; i++) {
            JSONObject ev = allItems.optJSONObject(i);
            Event evo = Event.Companion.fromJson(ev);
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
                        this.sections.add(ev.optString("startStr"));
                    }
                    this.items.put(ev);
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
        JSONObject ev = this.items.optJSONObject(position);
        final Event event = Event.Companion.fromJson(ev);
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.event_row, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Medium"));
            holder.who = (TextView) convertView.findViewById(R.id.who);
            holder.who.setTypeface(Font.use("Karla"));
            holder.tag = (TextView) convertView.findViewById(R.id.tag);
            holder.tag.setTypeface(Font.use("Karla_Italic"));
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
        int margin = 0;
        String type = event.getType();
        if (event.isPurchase() || event.getType().compareTo("meetup") == 0) {
            String tagText = "";
            if (type.compareTo("meetup") == 0) {
                tagText = event.getTitledFormat();
                holder.tag.setBackgroundColor(context.getResources().getColor(R.color.dark_yellow_tan_50));
                holder.tag.setTextColor(context.getResources().getColor(R.color.dark_gray));
            } else {
                holder.tag.setBackgroundColor(context.getResources().getColor(R.color.green));
                holder.tag.setTextColor(context.getResources().getColor(R.color.white));
                if (type.compareTo("academy") == 0) {
                    tagText = "$29";
                } else {
                    int price = Integer.parseInt(event.getPrice()) / 100;

                    tagText = "$"+Integer.toString(price);
                }
            }
            holder.tag.setVisibility(View.VISIBLE);
            holder.tag.setText(tagText);
            int tagWidth = textWidth(holder.tag) + 30;
            holder.tag.getLayoutParams().width = tagWidth;
            margin = tagWidth + 15;
        } else {
            holder.tag.setVisibility(View.GONE);
        }
        holder.name.setText(event.getWhat());
        holder.who.setText(createIndentedText(Html.fromHtml(this.getWho(event)).toString(), margin, 0));
        holder.rsvp.setTextColor(context.getResources().getColor(R.color.orange));
        if (Me.isAttendingEvent(event)){
            if (event.getType().equals("academy") || event.getType().equals("expedition") || event.isPurchase()) {
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
                if (event.getType().equals("academy")) {
                    holder.rsvp.setText("Attend");
                }
                else if (event.getType().equals("activity")) {
                    holder.rsvp.setText("Add to Schedule");
                }
                else {
                    holder.rsvp.setText("RSVP");
                }
            }
        }
        if (ev.has("because")) {
            event.setBecause(ev.optJSONArray("because"));
            holder.because.setText("Because you're interested in "+ event.getBecauseStr());
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
                if (!isAttending || (isAttending && event.isCancelable())) {
                    if ((isAttending) || (!isAttending && !isFull)) {
                        dialog.show(MainActivity.self.getFragmentManager(), "rsvpdialog");
                    }
                }
            }
        });
        holder.details.setOnClickListener(new View.OnClickListener() {
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
        return convertView;
    }

    static int textWidth(TextView view) {
        Rect bounds = new Rect();
        view.getPaint().getTextBounds(view.getText().toString(), 0, view.getText().length(), bounds);
        return bounds.width();
    }

    static SpannableString createIndentedText(String text, int marginFirstLine, int marginNextLines) {
        SpannableString result=new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0,text.length(),0);
        return result;
    }
    private String getWho(Event event) {
        String typeId = event.getType();
        String eventType = EventTypes.byId.optString("singular", "meetup").toLowerCase();
        String out = "";
        if (typeId.equals("academy")) {
            if (event.getDescrOneline() != null && event.getDescrOneline().length() > 0) {
                out = event.getDescrOneline();

            }
        } else {
            out = event.getWhoStr();
        }
        int max = 200;
        out = out.replace("\n", "");
        if (out.length() > max) {
            out = out.substring(0, (max-3))+"...";
        }
        return out;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        final HeaderHolder holder;
        if (convertView == null || mClearHeader) {
            mClearHeader = true;
            holder = new HeaderHolder();
            convertView = this.context.getLayoutInflater().inflate(this.context.getResources().getLayout(R.layout.list_header), null);
            holder.time = ((TextView) convertView.findViewById(R.id.time));
            holder.time.setTypeface(Font.use("Vitesse_Medium"));
            convertView.setTag(holder);
        }
        else {
            holder = (HeaderHolder) convertView.getTag();
        }
        long id = getHeaderId(position);
        holder.time.setText(this.sections.get((int) id));
//        holder.time.setText(sections.get(innerSect));
////        final int innerSect = section;
//        if (this.sections != null && section < this.sections.size() && this.sections.get(section) != null) {
//            holder.time.setText(sections.get(section));
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    holder.time.setText(sections.get(innerSect));
//                }
//            }, 40);
//        } else {
//        }
        return convertView;
    }



    private class ViewHolder {
        private TextView name;
        private TextView tag;
        private TextView who;
        private TextView because;
        private Button rsvp;
        private Button details;
    }

    private class HeaderHolder {
        private TextView time;
    }

}
