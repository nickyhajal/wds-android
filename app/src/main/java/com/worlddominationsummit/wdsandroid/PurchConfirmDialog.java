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
public class PurchConfirmDialog extends DialogFragment {

    private int mQuantity;
    private float mTotal;

    public void set(float total, int quantity) {
        mQuantity = quantity;
        mTotal = total;
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

        btn_no.setText("Nevermind");
        title.setText("Confirm Purchase");
        String confmsg = "Just to double-check, you'll be charged $"+mTotal+"0 for "+mQuantity;
        if (mQuantity > 1) {
            confmsg += " tickets (including a $10 processing fee per ticket).";
        } else {
            confmsg += " ticket (including a $10 processing fee).";
        }
        msg.setText(confmsg);
        btn_yes.setText("Purchase");
        final DialogFragment self = this;
        View.OnClickListener yesListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.self.cartFragment != null) {
                    MainActivity.self.cartFragment.startPurchaseProcess();
                }
                self.dismiss();
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