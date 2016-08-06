package com.worlddominationsummit.wdsandroid;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import java.util.Calendar;

/**
 * Created by nicky on 7/27/16.
 */
public class ExpirationPicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public int mDefYear = 0;
    public int mDefMonth = 100;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (mDefYear > 0) {
            year = mDefYear;
        }
        if (mDefMonth < 100) {
            month = mDefMonth;
        }
        DatePickerDialog d = new DatePickerDialog(getActivity(), this, year, month, day){
            @Override
            protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                int year = getContext().getResources()
                        .getIdentifier("android:id/day", null, null);
                if(year != 0){
                    View yearPicker = findViewById(year);
                    if(yearPicker != null){
                        yearPicker.setVisibility(View.GONE);
                    }
                }
            }
        };
        d.setTitle("");
        d.setButton(DatePickerDialog.BUTTON_POSITIVE, "Select", d);

        // Create a new instance of DatePickerDialog and return it
        return d;
    }

    public void setDefs(int m, int y) {
        mDefYear = y;
        mDefMonth = m;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        MainActivity.self.cartFragment.setExpiration(month, year);
    }
}
