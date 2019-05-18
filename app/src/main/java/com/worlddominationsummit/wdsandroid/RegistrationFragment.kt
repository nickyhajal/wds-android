package com.worlddominationsummit.wdsandroid

import android.support.v4.app.Fragment
import android.os.Bundle
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

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

/**
 * Created by nicky on 5/18/15.
 */
class RegistrationFragment : Fragment() {
    var mView: View? = null
    var listview: HeaderListView? = null
    lateinit var adapter: RegistrationAdapter
    var items: JSONArray? = null
    var day = ""
    lateinit var mNullMsg: TextView
    lateinit var mDaySelector: DaySelector

    fun willDisplay() {
        if (day == "") {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = Date()
            val today = dateFormat.format(date)
            if (today.compareTo("2019-06-25") < 0) {
                day = "2019-06-27"
            } else {
                day = today
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
            mView = inflater!!.inflate(R.layout.registration, container, false)
            this.listview = mView!!.findViewById(R.id.scheduleList) as HeaderListView
            this.listview!!.listView.id = R.id.scheduleListview
            mNullMsg = mView!!.findViewById(R.id.nullMsg) as TextView
            mNullMsg.typeface = Font.use("Karla_Bold")
            mDaySelector = mView!!.findViewById(R.id.daySelector) as DaySelector
            mDaySelector.onSelect = { it ->
                changeDay(it)
            }
        }
        return mView
    }

    fun changeDay(day: String) {
        this.day = day
        update_items()
    }

    fun update_items(items: JSONArray) {
        this.items = items
        this.update_items()
    }

    fun update_items() {
        this.adapter = RegistrationAdapter(this.activity)
        this.adapter.setDay(this.day)
        this.adapter.setItems(this.items)
        checkIfNull()
        if (this.listview != null) {
            this.listview!!.setAdapter(this.adapter)
        }
    }

    fun checkIfNull() {
        if (this.adapter.count > 0) {
            this.listview!!.visibility = View.VISIBLE
            mNullMsg.visibility = View.GONE
        } else {
            this.listview!!.visibility = View.GONE
            mNullMsg.visibility = View.VISIBLE
        }
    }
}
