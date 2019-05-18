package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

import com.android.volley.*
import com.android.volley.Response
import com.applidium.headerlistview.HeaderListView

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by nicky on 5/18/15.
 */
class EventsFragment : Fragment() {
    var mView: View? = null
    var listview: StickyListHeadersListView? = null
    lateinit var adapter: EventsAdapter
    var items: JSONArray? = null
    var day = ""
    var mState = "browse"
    var mDayStr = "Tuesday, July 11th"
    var mNullMsg: TextView? = null
    var mMeetupType = "all"
    var mTappedDay = ""
    var mMeetupNav: LinearLayout? = null
    var mType = "meetup"
    lateinit var mTitle: String
    lateinit var mSingular: String
    lateinit var mPlural: String
    lateinit var mViewSpinner: Spinner
    lateinit var mTypeSpinner: Spinner
    var mDaySelector: DaySelector? = null


    fun willDisplay() {
        if (this.items == null) {
            this.items = JSONArray()
        }
//        selectAppropriateDay()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        checkMeetupNav()
        willDisplay()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val ref = this
        Assets.getSmart(mType, Response.Listener<JSONObject> { rsp ->
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
            mView = inflater!!.inflate(R.layout.events, container, false)
            this.listview = mView!!.findViewById(R.id.meetupList) as StickyListHeadersListView
            mMeetupNav = mView!!.findViewById(R.id.meetupNav) as LinearLayout
            mNullMsg = mView!!.findViewById(R.id.nullMsg) as TextView
            mNullMsg!!.typeface = Font.use("Karla_Bold")
            mViewSpinner = mView!!.findViewById(R.id.view_spinner) as Spinner
            mTypeSpinner = mView!!.findViewById(R.id.type_spinner) as Spinner
            val viewSections = ArrayList<String>()
            viewSections.add("Browse Meetups")
            viewSections.add("Attending Meetups")
            viewSections.add("Suggested Meetups")
            val viewSpinnerAdapter = TitleSpinner(context, R.layout.title_spinner, viewSections)
            mViewSpinner.adapter = viewSpinnerAdapter
            mViewSpinner.setSelection(0)
            val typeSections = ArrayList<String>()
            typeSections.add("All Types")
            typeSections.add("Type: Discover")
            typeSections.add("Type: Experience")
            typeSections.add("Type: Network")
            val typeSpinnerAdapter = TitleSpinner(context, R.layout.title_spinner, typeSections)
            mTypeSpinner.adapter = typeSpinnerAdapter
            mTypeSpinner.setSelection(0)

            mViewSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                    changeState(position)
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {}
            }
            mTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                    changeType(position)
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {}
            }
            mDaySelector = mView!!.findViewById(R.id.daySelector) as DaySelector
            mDaySelector?.onSelect = { it ->
                changeDay(it, true)
            }
            checkMeetupNav()
            update_items()
        }
        return mView
    }

    fun setType(type: HashMap<String, String>) {
        mType = type["id"] ?: ""
        mSingular = type["singular"] ?: ""
        mPlural = type["plural"] ?: ""
        mTitle = type["title"] ?: ""
        checkMeetupNav()
//        selectAppropriateDay()
    }

    fun checkMeetupNav() {
        if (mMeetupNav != null) {
            mMeetupNav!!.visibility = View.GONE
            if (mType.compareTo("meetup") == 0) {
                mMeetupNav!!.visibility = View.VISIBLE
            }
        }
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
        var day = firstDay
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
        this.day = day
        if (tapped) {
            mTappedDay = day;
        }
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        try {
            val selectedDay = parser.parse("${day}T00:00:00.000Z").time
            val formatter = SimpleDateFormat("EEEE, MMMM d")
            mDayStr = formatter.format(selectedDay)

        } catch (e: ParseException) {
            Log.e("WDS", "Parse Exception", e)
        }
        update_items()
    }

    fun changeState(id: Int) {
        val states = arrayOf("browse", "attending", "suggested")
        mState = states[id]
        update_items()
    }

    fun changeType(id: Int) {
        val types = arrayOf("all", "discover", "experience", "network")
        mMeetupType = types[id]
        update_items()
    }

    fun update_items(items: JSONArray) {
        this.items = items
        selectAppropriateDay()
//        this.update_items()
    }

    fun update_items() {
        this.adapter = EventsAdapter(this.activity)
        this.adapter.setDay(this.day)
        this.adapter.setState(mState)
        this.adapter.setType(mType)
        this.adapter.setMeetupType(mMeetupType)
        this.adapter.setItems(this.items)
        val evs = this.items ?: JSONArray();
        mDaySelector?.setDaysFromEvents(evs);
        checkIfNull()
        if (this.listview != null) {
            this.adapter.mClearHeader = true
            this.listview!!.setAdapter(this.adapter)
        }
    }

    fun checkIfNull() {
        if (this.adapter.count > 0) {
            this.listview!!.visibility = View.VISIBLE
            mNullMsg!!.visibility = View.GONE
        } else {
            val parts = mDayStr.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val typelow = EventTypes.byId.optJSONObject(mType).optString("plural").toLowerCase()
            var plural = EventTypes.byId.optJSONObject(mType).optString("plural")
            if (mType != "spark_session") {
                plural = plural.toLowerCase()
            }
            if (mType.compareTo("meetup") == 0 && mMeetupType.compareTo("all") != 0) {
                plural = mMeetupType + ' ' + plural
            }
            val end = " for " + parts[1] + " " + parts[2] + "...yet."
            var text = "There are no " + plural + end
            if (mState.compareTo("attending") == 0) {
                text = "You haven't RSVPd to any " + plural + end
            } else if (mState.compareTo("suggested") == 0) {
                text = "Join more communities for more suggestions."
            }
            if (mNullMsg != null) {
                mNullMsg!!.text = text
                this.listview!!.visibility = View.GONE
                mNullMsg!!.visibility = View.VISIBLE
            }
        }
    }

    class TitleSpinner constructor(context: Context, resource: Int, items: ArrayList<String>) : ArrayAdapter<String>(context, resource, items) {

        // Affects default (closed) state of the spinner
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val density = MainActivity.density
            val p = density.toInt() * 2
            val view = super.getView(position, convertView, parent) as TextView
            view.typeface = Font.use("Vitesse_Medium")
            view.setPadding(0, p, 0, 0)
            view.textSize = 17.0f
            view.setTextColor(MainActivity.self.resources.getColor(R.color.dark_gray))
            return view
        }

        // Affects opened state of the spinner
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val density = MainActivity.density
            val p = density.toInt() * 10
            val view = super.getDropDownView(position, convertView, parent) as TextView
            view.typeface = Font.use("Vitesse_Medium")
            view.setPadding(p, p, p, p)
            view.textSize = 17.0f
            view.setBackgroundColor(MainActivity.self.resources.getColor(R.color.coffee))
            return view
        }
    }
}
