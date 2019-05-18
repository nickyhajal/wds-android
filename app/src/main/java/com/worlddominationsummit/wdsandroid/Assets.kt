package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.worlddominationsummit.wdsandroid.Assets.database
import org.jetbrains.anko.db.*
import org.jetbrains.anko.doAsync

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by nicky on 5/18/15.
 */
object Assets {
    lateinit var context: MainActivity
    lateinit var core: String
    lateinit var expires: JSONObject
    lateinit var days: ArrayList<String>
    lateinit var database: SqlHelper
    fun init(context: MainActivity) {
        // Access property for Context
        database = SqlHelper.getInstance(context)
        days = ArrayList<String>()
        Assets.context = context
        Assets.core = "me,events,interests,places,slim_attendees"
        Assets.expires = JSONObject()
        try {
            Assets.expires.put("me", 0)
            Assets.expires.put("events", 5)
            Assets.expires.put("interests", 300)
            Assets.expires.put("places", 300)
            Assets.expires.put("slim_attendees", 300)
        } catch (e: JSONException) {
            Log.e("WDS", "JSON Exception", e)
        }

    }

    fun sync(successListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) {
        Assets.pull(Assets.core, successListener, errorListener)
    }

    fun pull(includeStr: String, successListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) {
        val finalSuccessListener = successListener
        val include = JSONArray(Arrays.asList(*includeStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        val params = JSONObject()
        try {
            params.put("assets", includeStr)
            params.put("encode", "1")
        } catch (e: JSONException) {
            Log.e("WDS", "JSON Exception", e)
        }

        Api.get("assets", params, Response.Listener<JSONObject> { rsp ->
            val inc_len = include.length()
            for (i in 0..inc_len - 1) {
                var key = ""
                try {
                    key = include.getString(i)
                } catch (e: JSONException) {
                    Log.e("WDS", "Json Exception", e)
                }

                if (rsp.has(key)) {
                    if (key == "me") {
                        try {
                            Assets.process_me(rsp.getJSONObject(key))
                        } catch (e: JSONException) {
                            Log.e("WDS", "Json Exception", e)
                        }

                    } else if (key == "events") {
                        try {
                            Assets.process_events(rsp.getJSONArray(key))
                        } catch (e: JSONException) {
                            Log.e("WDS", "Json Exception", e)
                        }

                    } else if (key == "slim_attendees") {
                        try {
                            Assets.process_slim_attendees(rsp.getJSONArray(key))
                        } catch (e: JSONException) {
                            Log.e("WDS", "Json Exception", e)
                        }
                    } else if (key == "interests") {
                        try {
                            Store.set("interests", rsp.getJSONArray(key))
                        } catch (e: JSONException) {
                            Log.e("WDS", "Json Exception", e)
                        }

                    } else if (key == "places") {
                        try {
                            Store.set("places", rsp.getJSONArray(key))
                        } catch (e: JSONException) {
                            Log.e("WDS", "Json Exception", e)
                        }

                    }
                    track(key)
                }
            }
            finalSuccessListener.onResponse(rsp)
//            Log.i("WDS", rsp.toString())
        }, errorListener)
    }

    fun getEventFromEventId(event_id:String):JSONObject {
        val ints = Store.getJsonArray("events")
        val len = ints.length()
        for (i in 0 until len)
        {
            var event = JSONObject()
            try
            {
                event = ints.getJSONObject(i)
            }
            catch (e:JSONException) {
                Log.e("WDS", "Json Exception", e)
            }
            if (event_id == event.optString("event_id"))
            {
                return event
            }
        }
        return JSONObject()
    }

    fun getSmart(asset: String, successListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) {
        val _tracker = tracker()
        var doPull: Boolean? = false
        var pullAsset = asset
        val existing = Store.getJsonArray(asset)
        val rsp = JSONObject()
        if (asset == "meetups" || asset == "academy" || asset == "spark_session" || asset == "activity" || asset == "registration") {
            pullAsset = "events"
        }
        var tracked: Long = 0
        tracked = _tracker.optLong(pullAsset)

        val now = System.currentTimeMillis() / 1000L
        val diff = (now - tracked).toInt() / 60
        if (existing.length() > 0) {
            try {
                rsp.put("data", existing)
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

            successListener.onResponse(rsp)
            if (diff > Assets.expires.optInt(pullAsset)) {
                doPull = true
            }
        } else {
            doPull = true
        }
        if (doPull!!) {
            Assets.pull(pullAsset, Response.Listener<JSONObject> {
                val pulled = Store.getJsonArray(asset)
                val rsp = JSONObject()
                try {
                    rsp.put("data", pulled)
                } catch (e: JSONException) {
                    Log.e("WDS", "Json Exception", e)
                }

                successListener.onResponse(rsp)
            }, Response.ErrorListener { })
        }
    }

    fun tracker(): JSONObject {
        return Store.getJsonObject("tracker")
    }

    fun track(asset: String) {
        val _tracker = tracker()
        val now = System.currentTimeMillis() / 1000L
        try {
            _tracker.put(asset, now)
        } catch (e: JSONException) {
            Log.e("WDS", "Json Exception", e)
        }

        Store.set("tracker", _tracker)
    }


    fun process_me(rsp: JSONObject) {
        Puts.i(rsp)
        Me.update(rsp)
        Store.set("me", rsp)
    }

    fun process_slim_attendees(atns: JSONArray) {
        doAsync(Throwable::printStackTrace, {
            database.use {
                dropTable("atns", true)
                createTable("atns", true,
                        "user_id" to TEXT + PRIMARY_KEY,
                        "first_name" to TEXT,
                        "last_name" to TEXT)
                var len = atns.length()
                for (i in 0..(atns.length() - 1)) {
                    val item = atns.getJSONObject(i)
                    insert("atns",
                            "user_id" to item.getString("user_id"),
                            "first_name" to item.getString("first_name"),
                            "last_name" to item.getString("last_name")
                    )
                }
            }
        })
    }
    fun process_events(events: JSONArray) {
        var events = events
        val jsonValues = ArrayList<JSONObject>()
        for (i in 0..events.length() - 1) {
            try {
                jsonValues.add(events.getJSONObject(i))
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

        }
        Collections.sort(jsonValues) { o1, o2 -> o1.optString("start").compareTo(o2.optString("start")) }
        events = JSONArray(jsonValues)
        val allowed_events = JSONArray()
        val ev_length = events.length()
        val types = JSONObject()
        for (t in 0..EventTypes.list.length() - 1) {
            try {
                val e = EventTypes.list.getJSONObject(t)
                types.put(e.getString("id"), JSONArray())
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

        }
        for (i in 0..ev_length - 1) {
            try {
                val event = events.getJSONObject(i)
                if (Me.hasPermissionForEvent(event)) {
                    allowed_events.put(event)
                    for (t in 0..EventTypes.list.length() - 1) {
                        val e = EventTypes.list.getJSONObject(t)
                        val ev = events.getJSONObject(i)
                        val type = e.getString("id")
                        if (ev.getString("type") == type) {
                            val evsOfType = types.getJSONArray(type)
                            evsOfType.put(events.getJSONObject(i))
                            types.put(type, evsOfType)
                        }
                        val start = ev.optString("start")
                        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        try {
                            val tz = TimeZone.getDefault()
                            val now = Date()
                            val offsetFromUtc = tz.getOffset(now.time).toLong()
                            val startParsed = parser.parse(start).time + offsetFromUtc - 3000
                            val formatter = SimpleDateFormat("yyyy-MM-dd")
                            val startDate = formatter.format(startParsed)
                            if (startDate != null) {
                                if (!days.contains(startDate)) {
                                    days.add(startDate)
                                    Collections.sort(days) { o1, o2 ->  o2.compareTo(o1) }
                                }
                            }

                        } catch (e: ParseException) {
                            Log.e("WDS", "Parse Exception", e)
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception --", e)
            }

        }
        Store.set("events", allowed_events)
        val keys = types.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var evsOfType = JSONArray()
            try {
                evsOfType = types.getJSONArray(key)
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

            Store.set(key, evsOfType)
        }
    }
    fun searchAttendees(q: String) : ArrayList<HashMap<String, String>> {
        val matches = ArrayList<HashMap<String, SearchScore>>()
        TextUtils.split(q.toLowerCase(), " ").forEach {
            matches.add(searchAtnPart(it))
        }
        return sortSearchScores(mergeSearchScores(matches));
    }
    fun searchAtnPart(q: String) : HashMap<String, SearchScore>  {
        val queries = ArrayList<HashMap<String, SearchScore>>();
        queries.add(calcMatch("first_name", q, 1))
        queries.add(calcMatch("last_name", q, 2))
        return mergeSearchScores(queries)
    }
    fun sortSearchScores(set: HashMap<String, SearchScore>) : ArrayList<HashMap<String, String>>{
        val sorted = ArrayList<SearchScore>()
        val results = ArrayList<HashMap<String, String>>()
        val item = set.iterator()
        while(item.hasNext()) {
            val pair = item.next()
            sorted.add(pair.value)
        }
        Collections.sort(sorted) { o1, o2 -> o1.score.compareTo(o2.score) }
        sorted.forEach {
            results.add(it.atn)
        }
        return results
    }



    fun mergeSearchScores(sets: ArrayList<HashMap<String, SearchScore>>): HashMap<String, SearchScore> {
        val merged = HashMap<String, SearchScore>()
        sets.forEach {
            val item = it.iterator()
            while(item.hasNext()) {
                val pair = item.next()
                val user_id = pair.key
                val atn = pair.value
                if (merged.containsKey(user_id)) {
                    val existing = merged.get(user_id);
                    atn!!.score += existing!!.score
                }
                merged.put(user_id, atn)
            }
        }
        return merged
    }
    fun calcMatch(col: String, q: String, baseScore: Int) : HashMap<String, SearchScore> {
        val users = HashMap<String, SearchScore>()
        val whereString = "($col LIKE ?)"
        database.use {
            select("atns").whereSimple(whereString, "%$q%").exec {
                class AtnRow(val user_id: String, val first_name: String, val last_name: String)
                var rows: List<AtnRow> = emptyList()
                var outRows: List<HashMap<String, String>> = emptyList()
                rows = parseList(classParser<AtnRow>())
                rows.forEach {
                    var row = HashMap<String, String>();
                    row.put("first_name", it.first_name);
                    row.put("last_name", it.last_name);
                    row.put("user_id", it.user_id);
                    var score = baseScore
                    var colVal = row.get(col)
                    if (q == colVal) {
                        score += 2
                    } else if (colVal?.indexOf(q) == 0) { score += 1
                    }
                    val result = SearchScore(row, score);
                    users.put(row.get("user_id")!!, result)
                }

            }
        }
        return users
    }

    class SearchScore(val atn: HashMap<String, String>, var score: Int) {
    }


}
