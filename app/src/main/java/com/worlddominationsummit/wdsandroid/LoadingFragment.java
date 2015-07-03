package com.worlddominationsummit.wdsandroid;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nicky on 5/18/15.
 */
public class LoadingFragment extends Fragment{
    public View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.loading, container, false);
        }
        return this.view;
    }
}
