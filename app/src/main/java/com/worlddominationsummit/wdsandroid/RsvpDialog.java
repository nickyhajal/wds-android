package com.worlddominationsummit.wdsandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nicky on 7/8/16.
 */
public class RsvpDialog extends DialogFragment {

    private Event mEvent;
    private Boolean mAttending;

    public void setEvent(Event e, Boolean isAttending) {
        mEvent = e;
        mAttending = isAttending;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.rsvp_layout, null);
        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        TextView msg = (TextView) view.findViewById(R.id.dialog_message);
        final Button btn_yes = (Button) view.findViewById(R.id.btn_yes);
        Button btn_no = (Button) view.findViewById(R.id.btn_no);
        title.setTypeface(Font.use("Vitesse_Bold"));
        msg.setTypeface(Font.use("Karla"));
        btn_yes.setTypeface(Font.use("Vitesse_Medium"));
        btn_no.setTypeface(Font.use("Vitesse_Medium"));

        JSONObject eventType = EventTypes.byId.optJSONObject(mEvent.type);

        btn_no.setText("Nevermind");
        String typelow = eventType.optString("singular", "meetup").toLowerCase();
        String mode = "purchase";
        if (mEvent.type.equals("academy")) {
            title.setText("Attend this Academy");
            if (!Me.claimedAcademy() && mEvent.hasClaimableTickets()) {
                mode = "claim";
                btn_yes.setText("Claim");
                msg.setText("360 and Connect attendees may claim one complimentary academy and purchase additional academies for $29."+
                "\n\nWould you like to claim this ticket? (You can't change this later)");
            } else if (!Me.claimedAcademy() && !mEvent.hasClaimableTickets()) {
                msg.setText("You still have 1 free academy to claim but unfortunately thre are no more Insider Access tickets available for this academy.\n\n"+
                        "You can still purchase a ticket for $29.");
                btn_yes.setText("Purchase");
            } else {
                msg.setText("WDS Academies cost $59 but 360 and Connect attendees can get access for just $29.\n\n"
                        +"Would you like to purchase this academy?");
                btn_yes.setText("Purchase");
            }
        } else {
            if (mAttending) {
                title.setText("Can't make it?");
                msg.setText("Not able to make it to this "+typelow+"? No problem.\n\nJust cancel your RSVP below to make space for other attendees.");
                btn_yes.setText("Cancel RSVP");
            } else {
                title.setText("See you there?");
                msg.setText("This "+typelow+" will be on "+mEvent.dayStr+" at "+mEvent.startStr+".\n\nPlease only RSVP if you're sure you will attend.");
                btn_yes.setText("RSVP");
            }
        }
        final DialogFragment self = this;
        final String ac_mode = mode;
        View.OnClickListener yesListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mEvent.type.equals("academy")) {
                    if (ac_mode.equals("claim")) {
                        btn_yes.setText("Claiming...");
                        Me.claimAcademy(mEvent.event_id, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (MainActivity.self.eventsFragment != null && MainActivity.self.eventsFragment == MainActivity.self.active) {
                                    MainActivity.self.eventsFragment.update_items();
                                }
                                if (MainActivity.self.eventFragment != null && MainActivity.self.eventFragment == MainActivity.self.active) {
                                    MainActivity.self.eventFragment.updateRsvpButton();
                                }
                                btn_yes.setText("Claimed!");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        self.dismiss();
                                    }
                                }, 2000);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                    } else if (ac_mode.equals("purchase")) {
                        HashMap<String, String> prod = new HashMap<>();
                        prod.put("name", "WDS Academy");
                        prod.put("descr", mEvent.what);
                        prod.put("event_id", mEvent.event_id);
                        self.dismiss();
                        MainActivity.self.open_cart("academy", prod);
                    }
                } else {
                    btn_yes.setText("RSVPing...");
                    Me.toggleRsvp(mEvent, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            if (MainActivity.self.eventsFragment != null) {
                                MainActivity.self.eventsFragment.update_items();
                            }
                            if (MainActivity.self.eventFragment != null) {
                                MainActivity.self.eventFragment.updateRsvpButton();
                            }
                            btn_yes.setText("RSVP");
                            self.dismiss();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            MainActivity.offlineAlert();
                        }
                    });
                }
            }
        };
        View.OnClickListener noListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.dismiss();
            }
        };
        btn_yes.setOnClickListener(yesListener);
        btn_no.setOnClickListener(noListener);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }
}