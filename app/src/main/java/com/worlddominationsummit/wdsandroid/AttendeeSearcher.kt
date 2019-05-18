package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.util.Log
import android.widget.EditText
import android.os.Handler

import com.android.volley.*
import com.android.volley.Response

import org.json.JSONException
import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.Arrays

/**
 * Created by nicky on 5/17/15.
 */
class AttendeeSearcher(private val context: MainActivity, private val inp: EditText) : Runnable {
    private val timer: Handler
    private var query = ""

    init {
        this.timer = Handler()
    }

    fun startSearch(query: String) {
        this.query = query
        val special = arrayOf("friends", "friended me", "match me")
        if (Arrays.asList(*special).contains(query)) {
            specialSearch()
        } else {
            this.timer.removeCallbacks(this)
            this.timer.postDelayed(this, 1)
        }
    }

    fun specialSearch() {
        val params = JSONObject()
        if (this.query.length > 0) {
            try {
                try {
                    params.put("type", URLEncoder.encode(this.query, "utf-8"))
                    params.put("include_user", "1")
                } catch (e: UnsupportedEncodingException) {
                    Log.e("WDS", "Json Exception", e)
                }

            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

            Api.get("user/friends_by_type", params, { rsp ->
                try {
                    context.update_search(rsp.getJSONArray("user"))
                } catch (e: JSONException) {
                    Log.e("WDS", "Json Exception", e)
                }
            }) { MainActivity.offlineAlert() }
        }
    }

    override fun run() {
        val ref = this
        val params = JSONObject()
        if (this.query.length > 0) {
            val results = Assets.searchAttendees(this.query)
            ref.context.update_search(results);
//            try {
//                try {
//                    params.put("search", URLEncoder.encode(this.query, "utf-8"))
//                } catch (e: UnsupportedEncodingException) {
//                    Log.e("WDS", "Json Exception", e)
//                }
//
//            } catch (e: JSONException) {
//                Log.e("WDS", "Json Exception", e)
//            }

//            Api.get("users", params, { rsp ->
//                try {
//                    ref.context.update_search(rsp.getJSONArray("users"))
//                } catch (e: JSONException) {
//                    Log.e("WDS", "Json Exception", e)
//                }
//            }) { MainActivity.offlineAlert() }
        }
    }
}
