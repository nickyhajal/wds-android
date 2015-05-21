package com.worlddominationsummit.wds;
/**
 * Created by nicky on 5/19/15.
 */

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import com.applidium.headerlistview.SectionAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class MeetupsAdapter extends SectionAdapter {

    public String day;
    public int numItems;
    public ArrayList<String> sections;
    public ArrayList<ArrayList<HashMap>> items;
    public FragmentActivity context;

    public MeetupsAdapter(FragmentActivity activity) {
        this.context = activity;
        this.sections = new ArrayList<String>();
        this.numItems = 0;
        this.day = "2014-07-10";
    }

    public void setDay(String day) {
        this.day = day;
    }
    public void setItems(ArrayList<HashMap> items) {
        this.items = new ArrayList<ArrayList<HashMap>>();
        this.numItems = items.size();
        int sectionIndex = -1;
        String lastTime = "";
        for(int i = 0; i < this.numItems; i++) {
            HashMap<String, String> ev = items.get(i);
            if (ev.get("startDay").equals(this.day)) {
                if (!lastTime.equals(ev.get("startStr"))) {
                    sectionIndex += 1;
                    this.items.add(new ArrayList<HashMap>());
                    this.sections.add(ev.get("startStr"));
                }
                this.items.get(sectionIndex).add(ev);
                lastTime = ev.get("startStr");
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
            return this.items.get(section).size();
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getRowItem(int section, int row) {
        return this.items.get(section).get(row);
    }

    @Override
    public boolean hasSectionHeaderView(int section) {
        return true;
    }

    @Override
    public View getRowView(int section, int row, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        Event event = Event.fromHashMap((HashMap<String, String>)getRowItem(section, row));
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.meetup_row, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.name.setTypeface(Font.use("Vitesse_Medium"));
            holder.who = (TextView) convertView.findViewById(R.id.who);
            holder.who.setTypeface(Font.use("Karla"));
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
        holder.who.setText(event.who);
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
            convertView.setTag(holder);
        }
        else {
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
        private TextView who;
        private Button rsvp;
        private Button details;
    }

    private class HeaderHolder {
        private TextView time;
    }

}
