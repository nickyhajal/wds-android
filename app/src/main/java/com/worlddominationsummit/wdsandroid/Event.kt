package com.worlddominationsummit.wdsandroid

/**
 * Created by nicky on 5/18/15.
 */

import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.util.ArrayList
import java.util.HashMap
import net.nightwhistler.htmlspanner.HtmlSpanner
import java.nio.charset.Charset

class Event {
    var event_id: String = ""
    var slug: String = ""
    var what: String = "░░░░░░░░░"
    var who: String = "░░░░░░░"
    var place = "░░░░░"
    var descr: String = "░░░░░ ░░░░░░░░░░ ░░░░░"
    var descrOneline: String = ""
    var start: String = ""
    var type: String  = ""
    var for_type = "all"
    var format: String? = null
    var lat: String = ""
    var lon: String = ""
    var address: String = ""
    var venueNote = ""
    var startTime: String = ""
    var startStr: String = ""
    var endStr: String = ""
    var dayStr: String = ""
    var timeStr: String = ""
    var whoStr: String = ""
    var becauseStr: String = ""
    var price: String = ""
    var pay_link: String = ""
    var max: String = ""
    var num_rsvps: String = ""
    var free_max = "0"
    var num_free = "0"
    var ints: JSONArray = JSONArray()
    var hostsJSON: JSONArray? = null
    var hosts: ArrayList<Attendee> = ArrayList()
    var mBecause: JSONArray = JSONArray()
    var host: Attendee = Attendee()
    var host2: Attendee = Attendee()

    constructor() {
    }
    constructor(event_id: String, slug: String, what: String, who: String, place: String, descr: String, start: String, type: String, for_type: String, lat: String, lon: String, address: String, startTime: String, endStr: String, startStr: String, dayStr: String, hostsJSON: JSONArray) {
        this.event_id = event_id
        this.slug = slug
        this.what = what
        this.who = who
        this.place = place
        this.descr = descr
        this.type = type
        this.for_type = for_type
        this.start = start
        this.lat = lat
        this.lon = lon
        this.address = address
        this.startTime = startTime
        this.startStr = startStr
        this.endStr = endStr
        this.dayStr = dayStr
        this.hostsJSON = hostsJSON
        this.num_free = "0"
        this.free_max = "0"
    }

    fun init() {
        hosts = ArrayList<Attendee>()
        if (this.hostsJSON != null) {
            val len = this.hostsJSON!!.length()
            for (i in 0..len - 1) {
                try {
                    hosts.add(Attendee.fromJson(this.hostsJSON!!.getJSONObject(i)))
                } catch (e: JSONException) {
                    Log.e("WDS", "JSON Exception", e)
                }

            }
        }
        try {
            if (this.what != null) {
                this.what = String(this.what!!.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
                //            this.what = Html.fromHtml(this.what).toString();
            }
            if (this.who != null) {
                this.who = String(this.who!!.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
            }
            if (this.descr != null) {
//                Puts.i(descr)
                this.descrOneline = Html.fromHtml(String(TextUtils.htmlEncode(this.descr!!).toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))).toString()
//                Puts.i(descr)
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        this.timeStr = this.dayStr + "\nfrom " + this.startStr + " to " + this.endStr
        if (this.type == "academy") {
        } else {
            this.whoStr = ""
            if (this.who != null && this.who!!.length > 0) {
                if (EventTypes.byId.has(this.type)) {
                    val typelow = EventTypes.byId.optJSONObject(this.type).optString("singular", "event").toLowerCase()
                    var start = "A " + typelow
                    if (typelow == "activity") {
                        start = "An " + typelow
                    }
//                    Puts.i(this.what)
//                    Puts.i(start)
//                    Puts.i(this.who!!)
                    this.whoStr = start + " for " + this.who!!.replaceFirst(this.who!!.substring(0, 1).toRegex(), this.who!!.substring(0, 1).toLowerCase())
                    if (this.type == "meetup" && this.format != null && this.format!!.length > 2) {
                        val format = ucfirst(this.format)
                        this.whoStr = this.whoStr
                    }
                }
            }
        }

    }

    fun hasClaimableTickets(): Boolean {
        val num_free = Integer.valueOf(this.num_free)!!
        val free_max = Integer.valueOf(this.free_max)!!
        return free_max > 0 && num_free < free_max
    }

    fun setBecause(because: JSONArray) {
        var str = ""
        mBecause = because
        val len = because.length()
        for (i in 0..len - 1) {
            str += because.optString(i)
            if (i < len - 2) {
                str += ", "
            } else if (i == len - 2) {
                str += " & "
            }

        }
        this.becauseStr = str
    }

    fun descrWithHtmlParsed(): Spanned {
        val htmlspanner = HtmlSpanner()
        this.descr = String(TextUtils.htmlEncode(this.descr!!).toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
        val descr = Html.fromHtml(this.descr!!.replace("\n", "<br>")).toString()
        val text = htmlspanner.fromHtml(descr)
        return text
//        return this.descr!!;
    }

    fun descrSpecial(): Spanned {
        val htmlspanner = HtmlSpanner()
        val replaceBr = this.descr.replace("\n", "<br>")
        val utf8 = String(replaceBr.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
        val html = Html.fromHtml(utf8).toString()
        return htmlspanner.fromHtml(html.replace("\n", "<br>"));
    }

    val isPurchase: Boolean
        get() {
            return (type.compareTo("academy") == 0 || price.toInt() > 0)
        }

    val isExternal: Boolean
        get() {
            return (pay_link.isNotEmpty() && !pay_link.equals("null"))
        }

    val isCancelable: Boolean get() { return !isPurchase }

    val isAttending: Boolean
        get() = Me.isAttendingEvent(this)

    //(int)(m+(float)m*0.1f);
    val isFull: Boolean
        get() {
            val m = Integer.parseInt(max)
            return Integer.parseInt(num_rsvps) > m
        }

    fun shouldAppearFull(): Boolean? {
        return isFull && (!isAttending)
    }

    fun getTitledFormat(): String {
        return ucfirst(this.format);
    }


    companion object {


        fun fromHashMap(params: HashMap<String, String>): Event {
            return Event.fromJson(JSONObject(params))
        }

        fun fromJson(params: JSONObject): Event {
            val ev = Event()
            ev.event_id = params.optString("event_id")
            ev.slug = params.optString("slug")
            ev.what = params.optString("what")
            ev.who = params.optString("who")
            ev.place = params.optString("place")
            ev.price = params.optString("price")
            ev.price = if (ev.price.compareTo("null") == 0) "0" else ev.price
            ev.pay_link = params.optString("pay_link")
            ev.descr = params.optString("descr")
            ev.start = params.optString("start")
            ev.type = params.optString("type")
            ev.format = params.optString("format")
            ev.for_type = params.optString("for_type")
            ev.lat = params.optString("lat")
            ev.lon = params.optString("lon")
            ev.address = params.optString("address")
            ev.venueNote = params.optString("venue_note")
            ev.startTime = params.optString("startTime")
            ev.startStr = params.optString("startStr")
            ev.endStr = params.optString("endStr")
            ev.dayStr = params.optString("dayStr")
            ev.max = params.optString("max")
            ev.num_rsvps = params.optString("num_rsvps")
            ev.free_max = params.optString("free_max")
            ev.num_free = params.optString("num_free")
            try {
                val hosts = params.getJSONArray("hosts")
                ev.hostsJSON = hosts
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

            try {
                val ints = params.optString("ints")
                ev.ints = JSONArray(ints)
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

            try {
                if (ev.hostsJSON!!.length() > 0) {
                    ev.host = Attendee.fromJson(ev.hostsJSON!!.getJSONObject(0))
                }
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }
            try {
                if (ev.hostsJSON!!.length() > 1) {
                    ev.host2 = Attendee.fromJson(ev.hostsJSON!!.getJSONObject(1))
                }
            } catch (e: JSONException) {
                Log.e("WDS", "Json Exception", e)
            }

            ev.init()
            return ev
        }

        fun ucfirst(subject: String?): String {
            if (subject != null) {
                return Character.toUpperCase(subject[0]) + subject.substring(1)
            }
            return ""
        }
    }


}
