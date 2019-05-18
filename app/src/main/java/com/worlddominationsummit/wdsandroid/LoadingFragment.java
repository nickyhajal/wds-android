package com.worlddominationsummit.wdsandroid;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by nicky on 5/18/15.
 */
public class LoadingFragment extends Fragment{
    public View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.loading, container, false);
            ImageView top = this.view.findViewById(R.id.topLoadingView);
            ImageView bot = this.view.findViewById(R.id.botLoadingView);
            Glide.with(this).load(R.drawable.loading_top).into(top);
            Glide.with(this).load(R.drawable.loading_bottom).into(bot);

        }
        return this.view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (MainActivity.self.tabsStarted) {
            MainActivity.self.open_dispatch();
        }
    }
}
