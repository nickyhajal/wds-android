package com.worlddominationsummit.wds;

import android.app.Activity;
//import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
//import android.support.v4.app.FragmentActivity;

import java.lang.reflect.Field;

/**
 * Created by nicky on 5/20/15.
 */
public class TabsFragment extends Fragment{
    public View view;
    public Activity context;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.tabs, container, false);
            Button dispatch = (Button) this.view.findViewById(R.id.btn_dispatch);
            Button schedule = (Button) this.view.findViewById(R.id.btn_schedule);
            Button meetups = (Button) this.view.findViewById(R.id.btn_meetups);
            dispatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    MainActivity.self.open_dispatch();
                }
            });
            schedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    MainActivity.self.open_schedule();
                }
            });
            meetups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    MainActivity.self.open_meetups();
                }
            });
        }
        return this.view;
    }

    public void open(Fragment frag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        //transaction.addToBackStack(null);
        transaction.replace(R.id.tab_content, frag);
        transaction.commit();
        //getChildFragmentManager().executePendingTransactions();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}