package com.worlddominationsummit.wdsandroid

import android.app.ActionBar
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.*
import com.android.volley.Response
import com.applidium.headerlistview.HeaderListView
//import kotlinx.android.synthetic.main.cart.*

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 * Created by nicky on 5/18/15.
 */
class ScheduleFragment : Fragment() {
    var mView: View? = null
    var listview: StickyListHeadersListView? = null
    lateinit var adapter: ScheduleAdapter
    var items: JSONArray? = null
    var mTappedDay = ""
    var mDay = ""
    lateinit var mNullMsg: TextView
    lateinit var mDaySelector: DaySelector

    fun willDisplay() {
        if (mDay == "") {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = Date()
            val today = dateFormat.format(date)
            if (today.compareTo("2019-06-25") < 0) {
                mDay = "2019-06-27"
            } else {
                mDay = today
            }
        }
        if (this.items == null) {
            this.items = JSONArray()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val ref = this
        Assets.getSmart("events", Response.Listener<JSONObject> { rsp ->
            try {
                ref.update_items(rsp.getJSONArray("data"))
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }
        }, Response.ErrorListener {
            //                ref.tabsStarted = true;
            //                ref.open_tabs();
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            val ref = this
            mView = inflater!!.inflate(R.layout.schedule, container, false)
            listview = mView!!.findViewById(R.id.scheduleList) as StickyListHeadersListView
//            listview!!.listView.id = R.id.scheduleListview
            mNullMsg = mView!!.findViewById(R.id.nullMsg) as TextView
            mNullMsg.typeface = Font.use("Karla_Bold")
            mNullMsg.text = "You don't have anything scheduled here... yet."
            mDaySelector = mView!!.findViewById(R.id.daySelector) as DaySelector
            mDaySelector.onSelect = { it ->
                changeDay(it, true)
            }

            //            // DAY SELECT
            //            TextView name = (TextView) this.view.findViewById(R.id.selectLabel);
            //            name.setTypeface(Font.use("Vitesse_Medium"));
            //            Button btn = (Button) this.view.findViewById(R.id.selectBtn);
            //            btn.setTypeface(Font.use("Vitesse_Medium"));
            //            final LinearLayout selectBtns = (LinearLayout) this.view.findViewById(R.id.select_buttons);
            //            selectBtns.setVisibility(View.GONE);
            //            btn.setTypeface(Font.use("Vitesse_Medium"));
            //            View.OnClickListener selectDayListener = new View.OnClickListener() {
            //                @Override
            //                public void onClick(View v) {
            //                    TextView t = (TextView) v;
            //                    String day = t.getTag().toString();
            //                    String dayStr = t.getText().toString();
            //                    ref.changeDay(day, dayStr);
            //                    selectBtns.setVisibility(View.GONE);
            //                }
            //            };
            //            int count = selectBtns.getChildCount();
            //            for (int i = 0; i <= count; i++) {
            //                View v = selectBtns.getChildAt(i);
            //                if (v instanceof TextView) {
            //                    ((TextView) v).setTypeface(Font.use("Vitesse_Medium"));
            //                    v.setOnClickListener(selectDayListener);
            //                }
            //            }
            //            btn.setOnClickListener(new View.OnClickListener() {
            //                @Override
            //                public void onClick(View arg0) {
            //                    selectBtns.setVisibility(View.VISIBLE);
            //                }
            //            });
            this.update_items()
        }
        return mView
    }

    fun activeDates(events: JSONArray): ArrayList<String> {
        val len = events.length()
        var days: ArrayList<String> = ArrayList<String>();
        for(i in 0..(len-1)) {
            val evDay: String = events.optJSONObject(i).optString("startDay", "");
            if (!days.contains(evDay) && evDay.isNotEmpty()) {
                days.add(evDay)
            }
        }
        return days;
    }

    fun selectAppropriateDay() {
        val days = activeDates(this.items ?: JSONArray());
        var firstDay = "2019-06-25"
        var day = "2019-06-27"
        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val today = formatter.format(date)
        if (today > firstDay) {
            day = today
        }
        if (mTappedDay.isNotEmpty()) {
            day = mTappedDay
        }
        if (!days.contains(day) && !days.isEmpty()) {
            var tryDay = day
            var count = 0;
            var stop = false;
            while (!days.contains(tryDay)) {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                try {
                    val nextDay = Date((parser.parse("${tryDay}T00:00:00.000Z").getTime() + TimeUnit.DAYS.toMillis(1)))
                    tryDay = formatter.format(nextDay)
                } catch (e: ParseException) {
                    Log.e("WDS", "Parse Exception", e)
                }
                if (count > 20) {
                    tryDay = firstDay
                    if (stop) break
                    stop = true
                    count = 0
                } else {
                    count += 1;
                }
            }
            day = tryDay;
        }
        mDaySelector?.setSelectedDay(day)
        changeDay(day)
    }

    fun changeDay(day: String, tapped:Boolean = false) {
        mDay = day
        if (tapped) {
            mTappedDay = day;
        }
        update_items()
    }

    fun update_items(items: JSONArray) {
        this.items = items
        selectAppropriateDay()
//        this.update_items()
    }

    fun update_items() {
        this.adapter = ScheduleAdapter(this.activity)
        this.adapter.setDay(mDay)
        this.adapter.setItems(JSONArray())
        this.adapter.notifyDataSetChanged()
        this.adapter.setItems(this.items)
        this.adapter.notifyDataSetChanged()
        listview?.visibility = View.GONE
        checkIfNull()
        Handler().postDelayed({
            listview?.visibility =View.VISIBLE
        }, 4000)
        val evs = this.items ?: JSONArray();
        mDaySelector?.setDaysFromEvents(evs);
        if (listview != null) {
            listview!!.setAdapter(this.adapter)
//            listview!!.scrollTo(0, 0)
        }
    }

    fun checkIfNull() {
        if (this.adapter.count > 0) {
            listview!!.visibility = View.VISIBLE
            mNullMsg.visibility = View.GONE
        } else {
            listview!!.visibility = View.GONE
            mNullMsg.visibility = View.VISIBLE
        }
    }
}
