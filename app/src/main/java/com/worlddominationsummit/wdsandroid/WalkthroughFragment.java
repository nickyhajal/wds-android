package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class WalkthroughFragment extends Fragment{
    public View mView;
    public LinearLayout mDots;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.walkthrough, container, false);
            mDots = (LinearLayout) mView.findViewById(R.id.dots);
            mViewPager = (ViewPager) mView.findViewById(R.id.view_pager);
            mViewPager.setAdapter(new WalkthroughPagerAdapter());
            mViewPager.setOnPageChangeListener(new WalkthroughPageChangeListener());
            mDots.getChildAt(0).setBackgroundResource(R.drawable.dot_filled);
        }
        return mView;
    }

    private static final int MAX_VIEWS = 8;

    ViewPager mViewPager;

    class WalkthroughPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return MAX_VIEWS;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View wtContainer = inflater.inflate(R.layout.walkthrough_section, null);
            LinearLayout card = (LinearLayout) wtContainer.findViewById(R.id.card);
            ImageView imageView = (ImageView) wtContainer.findViewById(R.id.icon);
            TextView title = (TextView) wtContainer.findViewById(R.id.title);
            TextView text = (TextView) wtContainer.findViewById(R.id.text);
            title.setTypeface(Font.use("Vitesse_Medium"));
            text.setTypeface(Font.use("Karla"));

            switch(position) {
                case 0:
                    title.setText("Hey there!");
                    text.setText("Welcome the WDS App!\n\nTo get you off on the right foot, let's go through a quick walkthrough of what you can do here.");
                    imageView.setImageResource(R.drawable.wt_welcome_icon);
                    break;
                case 1:
                    title.setText("The Dispatch");
                    text.setText("Get to know your fellow WDSers before arriving by posting and chatting on the Dispatch.");
                    imageView.setImageResource(R.drawable.wt_dispatch_icon);
                    break;
                case 2:
                    title.setText("Communities");
                    text.setText("Join communities to have discussions with other WDSers about your shared interests.");
                    imageView.setImageResource(R.drawable.wt_communities_icon);
                    break;
                case 3:
                    title.setText("Meetups");
                    text.setText("Browse and RSVP to the wide-range of attendee-hosted meetups during WDS.\n\nWe'll even make some suggestions based on your communities.");
                    imageView.setImageResource(R.drawable.wt_meetups_icon);
                    break;
                case 4:
                    title.setText("Your Schedule");
                    text.setText("Stay on top of your schedule!\n\nEverything you care about is clearly outlined — even your meetups!");
                    imageView.setImageResource(R.drawable.wt_schedule_icon);
                    break;
                case 5:
                    title.setText("Browse Attendees");
                    text.setText("Search WDSers, browse their profiles and friend them to easily stay connected in the future.");
                    imageView.setImageResource(R.drawable.wt_attendees_icon);
                    break;
                case 6:
                    title.setText("Ready to go?");
                    text.setText("You'll need to login with your WDS account to continue.\n\nIf you haven't created one yet, just click the link in your WDS Welcome E-Mail to get started.");
                    imageView.setImageResource(R.drawable.wt_finish_icon);
                    break;
                case 7:
                    title.setVisibility(View.GONE);
                    text.setVisibility(View.GONE);
                    imageView.setImageResource(R.drawable.big_logo);
                    card.setBackgroundColor(getActivity().getResources().getColor(R.color.green));
            }

            ((ViewPager) container).addView(wtContainer, 0);
            return wtContainer;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            ((ViewPager)container).removeView(view);
            view = null;
        }
    }


    class WalkthroughPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < MAX_VIEWS; i++) {
                View v = mDots.getChildAt(i);
                if (i == position) {
                    v.setBackgroundResource(R.drawable.dot_filled);
                }
                else {
                    v.setBackgroundResource(R.drawable.dot);
                }
                if (position == 7) {
                    Me.checkLoggedIn();
                }
                Store.set("walkthrough", String.valueOf(position+1));
            }
            // Here is where you should show change the view of page indicator
            switch(position) {

                case MAX_VIEWS - 1:


                    break;

                default:

            }

        }

    }
}

