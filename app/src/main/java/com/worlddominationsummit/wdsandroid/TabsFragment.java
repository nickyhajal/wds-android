package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
//import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageButton;
import android.widget.LinearLayout;
//import android.support.v4.app.FragmentActivity;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by nicky on 5/20/15.
 */
public class TabsFragment extends Fragment{
    public View view;
    public ImageButton dispatch;
    public ImageButton schedule;
    public ImageButton meetups;
//    public ImageButton explore;
    public ImageButton chat;
    public LinearLayout mTabBar;
    public MainActivity context;
    public Fragment active;
    public Fragment lastActive;
    public HashMap<String, Fragment> frags;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tabDrawables();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            this.view = inflater.inflate(R.layout.tabs, container, false);
            this.dispatch = (ImageButton) this.view.findViewById(R.id.btn_dispatch);
            mTabBar = (LinearLayout) this.view.findViewById(R.id.tabbar);
            this.schedule = (ImageButton) this.view.findViewById(R.id.btn_schedule);
            this.meetups = (ImageButton) this.view.findViewById(R.id.btn_meetups);
            this.chat = (ImageButton) this.view.findViewById(R.id.btn_chat);
//            this.explore = (ImageButton) this.view.findViewById(R.id.btn_explore);
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
                    MainActivity.self.open_event_types();
                }
            });
            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    MainActivity.self.open_chats();
                }
            });
//            explore.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    MainActivity.self.open_explore();
//                }
//            });
        }
        return this.view;
    }

    public void hideTabs() {
        mTabBar.setVisibility(View.GONE);
    }
    public void showTabs() {
        mTabBar.setVisibility(View.VISIBLE);
    }
    public void open(Fragment frag, String tag) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.tab_content, frag, tag);
        transaction.addToBackStack(tag);
        if (frag != MainActivity.self.postFragment) {
        }
        transaction.commit();
        if (!MainActivity.self.frags.containsKey(tag)){
            MainActivity.self.frags.put(tag, frag);
        }

        active = frag;
        MainActivity.self.active = frag;
        MainActivity.self.updateTitle();
        updateTabs();
        //getChildFragmentManager().executePendingTransactions();
    }


    public void updateTabs() {
        //this.dispatch.setTextColor(getResources().getColor((MainActivity.self.active == this.context.homeFragment) ? R.color.orange : R.color.gray));
//        this.schedule.setTextColor(getResources().getColor((MainActivity.self.active == this.context.scheduleFragment) ? R.color.orange : R.color.gray));
//        this.meetups.setTextColor(getResources().getColor((MainActivity.self.active == this.context.eventsFragment) ? R.color.orange : R.color.gray));
        tabDrawables();
    }

    public void tabDrawables() {
        Fragment main = MainActivity.self.active;
        dispatch.setImageResource((main == this.context.homeFragment) ? R.drawable.dispatch_icon_selected : R.drawable.dispatch_icon);
        schedule.setImageResource(
                (
                        main == this.context.scheduleFragment
                                || (lastActive == this.context.scheduleFragment && main == this.context.eventFragment)
                ) ? R.drawable.schedule_icon_selected : R.drawable.schedule_icon
        );
        meetups.setImageResource(
                (
                        main == this.context.eventTypesFragment
                                || main == this.context.eventsFragment
                                || (lastActive == this.context.eventsFragment && main == this.context.eventFragment)
                ) ? R.drawable.meetups_icon_selected : R.drawable.meetups_icon
        );
//        explore.setImageResource((active == this.context.exploreFragment) ? R.drawable.explore_icon_selected : R.drawable.explore_icon);
        chat.setImageResource((main == this.context.chatsFragment) ? R.drawable.chat_icon_selected : R.drawable.chat_icon);
        lastActive = main;
//        this.dispatch.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(((MainActivity.self.active == this.context.homeFragment) ? R.drawable.dispatch_icon_selected : R.drawable.dispatch_icon), 32, 32), null, null);
//        this.schedule.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(((MainActivity.self.active == this.context.scheduleFragment) ? R.drawable.schedule_icon_selected : R.drawable.schedule_icon), 32, 32), null, null);
//        this.meetups.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(((MainActivity.self.active == this.context.eventsFragment) ? R.drawable.meetups_icon_selected : R.drawable.meetups_icon), 32, 32), null, null);
    }

    public Drawable getDrawable(int id, float width, float height) {
        width = MainActivity.density * width;
        height = MainActivity.density * height;
        Bitmap original = BitmapFactory.decodeResource(getResources(), id);
        Bitmap b = Bitmap.createScaledBitmap(original, (int) width, (int) height, false);
        Drawable icon = new BitmapDrawable(context.getResources(), b);
//        Drawable icon = new ScaleDrawable(getResources().getDrawable(id), 0, width, height).getDrawable();
//        icon.setBounds(0, 0, (int) width, (int) height);
//        Puts.i(icon.toString());
        return icon;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.self.active.isResumed()) {
        }
        super.onDestroy();
    }

    public Fragment getCurrentFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag(fragmentTag);
        return currentFragment;
    }

}
