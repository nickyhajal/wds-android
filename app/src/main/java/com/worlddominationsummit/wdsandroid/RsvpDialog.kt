package com.worlddominationsummit.wdsandroid

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.android.volley.VolleyError
import com.android.volley.Response
import org.json.JSONObject

import java.util.HashMap

/**
 * Created by nicky on 7/8/16.
 */
class RsvpDialog : DialogFragment() {

    private var mEvent: Event? = null
    private var mAttending: Boolean? = null

    fun setEvent(e: Event, isAttending: Boolean?) {
        mEvent = e
        mAttending = isAttending
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.rsvp_layout, null)
        val title = view.findViewById(R.id.dialog_title) as TextView
        val msg = view.findViewById(R.id.dialog_message) as TextView
        val btn_yes = view.findViewById(R.id.btn_yes) as Button
        val btn_no = view.findViewById(R.id.btn_no) as Button
        title.typeface = Font.use("Vitesse_Bold")
        msg.typeface = Font.use("Karla")
        btn_yes.typeface = Font.use("Vitesse_Medium")
        btn_no.typeface = Font.use("Vitesse_Medium")
        var priceStr = mEvent?.price ?: "0"
        var price: Int = priceStr.toInt() / 100
        val purchaseRequired: Boolean = price > 0

        val eventType = EventTypes.byId.optJSONObject(mEvent!!.type)

        btn_no.text = "Nevermind"
        var type = eventType.optString("singular", "meetup")
        val typelow = type.toLowerCase()
        var mode = "purchase"
        val isExternal = mEvent!!.isExternal
        if (mEvent!!.type == "registration") {
            if (mAttending!!) {
                title.text = "Can't make it?"
                btn_yes.text = "Cancel RSVP"
                msg.text = "Not able to make it to this registration time? No problem.\n\nJust cancel your RSVP below.\n\nPlease signup for another registration time so we can have things ready for you!"
            } else {
                title.text = "Register at this Time!"
                btn_yes.text = "Yep, let's do it!"
                msg.text = "By RSVPing for a registration time, we can make sure the process goes quickly and smoothly for everyone.\n\nWill you make it this registration time?"
            }
        }
        else if (mEvent!!.type == "academy") {
            title.text = "Attend this Academy"
            if (!Me.claimedAcademy() && mEvent!!.hasClaimableTickets()) {
                mode = "claim"
                btn_yes.text = "Claim"
                msg.text = "WDS Attendees who preordered their ticket can claim one complimentary academy and purchase additional academies for $29." + "\n\nWould you like to claim this ticket? (You can't change this later)"
            } else if (!Me.claimedAcademy() && !mEvent!!.hasClaimableTickets()) {
                msg.text = "You still have 1 free academy to claim but unfortunately thre are no more Insider Access tickets available for this academy.\n\n" + "You can still purchase a ticket for $29."
                btn_yes.text = "Purchase"
            } else {
                msg.text = "WDS Academies cost $59 but attendees can get access for just $29.\n\n" + "Would you like to purchase this academy?"
                btn_yes.text = "Purchase"
            }
        }
        else if (mEvent!!.type == "activity") {
            if (mAttending!!) {
                title.text = "Can't make it?"
                btn_yes.text = "Cancel RSVP"
                msg.text = "Not able to make it to this event? No problem.\n\nJust remove it from your schedule below"
            } else {
                title.text = "See you there?"
                btn_yes.text = "Add to Schedule"
                msg.text = "This event will be on " + mEvent!!.dayStr + " at " + mEvent!!.startStr + ".\n\nWe hope to see you there!"
            }
        } else {
            if (mAttending!!) {
                title.text = "Can't make it?"
                msg.text = "Not able to make it to this $typelow? No problem.\n\nJust cancel your RSVP below to make space for other attendees."
                btn_yes.text = "Cancel RSVP"
            } else {
                title.text = "See you there?"
                if (purchaseRequired) {
                    if (isExternal) {
                        msg.text = "Attending this event costs $$price\n\nIn this case, payment is processed externally, so you'll temporarily leave the WDS App to complete your purchase.\n\nWould you like to purchase access to this $typelow?"
                        btn_yes.text = "Let's do it!"
                    } else {
                        msg.text = "Access to this event costs $$price\n\nWould you like to purchase access to this $typelow?"
                        btn_yes.text = "Let's do it!"
                    }
                } else {
                    msg.text = "This " + typelow + " will be on " + mEvent!!.dayStr + " at " + mEvent!!.startStr + ".\n\nPlease only RSVP if you're sure you will attend."
                    btn_yes.text = "RSVP"
                }
            }
        }
        val self = this
        val ac_mode = mode
        val yesListener = View.OnClickListener {
            if (mEvent!!.type == "academy") {
                if (ac_mode == "claim") {
                    btn_yes.text = "Claiming..."
                    Me.claimAcademy(mEvent!!.event_id, {
                        if (MainActivity.self.eventsFragment != null && MainActivity.self.eventsFragment === MainActivity.self.active) {
                            MainActivity.self.eventsFragment.update_items()
                        }
                        if (MainActivity.self.eventFragment != null && MainActivity.self.eventFragment === MainActivity.self.active) {
                            MainActivity.self.eventFragment.updateRsvpButton()
                        }
                        btn_yes.text = "Claimed!"
                        Handler().postDelayed({ self.dismiss() }, 2000)
                    }) { }
                } else if (ac_mode == "purchase") {
                    val prod = HashMap<String, String>()
                    prod.put("name", "WDS Academy")
                    prod.put("descr", mEvent!!.what)
                    prod.put("event_id", mEvent!!.event_id)
                    prod.put("price", "29")
                    self.dismiss()
                    MainActivity.self.open_cart("academy", prod)
                }
            } else {
                btn_yes.text = "RSVPing..."
                if (Me.isAttendingEvent((mEvent))) {
                    btn_yes.text = "unRSVPing..."
                }
                if (purchaseRequired) {
                    if (isExternal) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mEvent!!.pay_link)))
                        self.dismiss()
                    }
                    else {
                        val prod = HashMap<String, String>()
                        prod.put("name", "WDS $type")
                        prod.put("descr", mEvent!!.what)
                        prod.put("event_id", mEvent!!.event_id)
                        prod.put("price", "$${mEvent!!.price.toInt()/100}")
                        self.dismiss()
                        MainActivity.self.open_cart("event", prod)
                    }
                } else {
                    Me.toggleRsvp(mEvent, {
                        if (MainActivity.self.eventsFragment != null) {
                            MainActivity.self.eventsFragment.update_items()
                        }
                        if (MainActivity.self.eventFragment != null) {
                            MainActivity.self.eventFragment.updateRsvpButton()
                        }
                        btn_yes.text = "RSVP"
                        self.dismiss()
                    }) { MainActivity.offlineAlert() }
                }
            }
        }
        val noListener = View.OnClickListener { self.dismiss() }
        btn_yes.setOnClickListener(yesListener)
        btn_no.setOnClickListener(noListener)


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        return builder.create()
    }
}