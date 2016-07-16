package com.worlddominationsummit.wdsandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import org.json.JSONObject;

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
        if (mEvent.type.equals("academy")) {
            btn_yes.setText("Get a Ticket");
            title.setText("Attend this Academy");
            msg.setText("360 and Connect attendees may claim one complimentary academy and purchase additional academies for $29.\n\nYou'll need to complete this process through our site. Tap below to continue.");
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
        View.OnClickListener yesListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.dismiss();
                btn_yes.setText("RSVPing...");

                if (mEvent.type.equals("academy")) {
                    String url = "https://worlddominationsummit.com/academy/"+mEvent.slug;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } else {
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