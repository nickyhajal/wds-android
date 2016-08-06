package com.worlddominationsummit.wdsandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.*;
import org.json.JSONObject;
import java.util.HashMap;

/**
 * Created by nicky on 7/27/16.
 */
public class ContentDialog extends DialogFragment {

    private String mTitle;
    private String mBody;
    private String mBtn;

    public void setContent(String title, String body, String btn) {
        mTitle = title;
        mBody = body;
        mBtn = btn;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.content_dialog, null);
        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        TextView msg = (TextView) view.findViewById(R.id.dialog_message);
        final Button btn_yes = (Button) view.findViewById(R.id.btn_yes);
        title.setTypeface(Font.use("Vitesse_Bold"));
        msg.setTypeface(Font.use("Karla"));
        btn_yes.setTypeface(Font.use("Vitesse_Medium"));

        final ContentDialog self = this;
        btn_yes.setText(mBtn);
        title.setText(mTitle);
        msg.setText(mBody);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                self.dismiss();
            }
        });


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }
}
