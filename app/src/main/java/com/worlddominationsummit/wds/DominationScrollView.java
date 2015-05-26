package com.worlddominationsummit.wds;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by nicky on 5/22/15.
 */
public class DominationScrollView extends ScrollView {
    public boolean isScrollEnabled = true;

    public DominationScrollView(Context context) {
        super(context);
    }

    public DominationScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DominationScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScroll(boolean scrollEnabled){
        Puts.i("ALLOW SCROLL?");
        Puts.i(scrollEnabled);
        isScrollEnabled = scrollEnabled;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Puts.i("FROM DOMINSCROLL");
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Puts.i("DOWN IN DOMIN");
                // if we can scroll pass the event to the superclass
                if (isScrollEnabled) return super.onTouchEvent(ev);
                Puts.i("PREVENT SCROLL");
                // only continue to handle the touch event if scrolling enabled
                return isScrollEnabled; // mScrollable is always false at this point
            default:
                if (isScrollEnabled) return super.onTouchEvent(ev);
                Puts.i("PREVENT SCROLL");
                // only continue to handle the touch event if scrolling enabled
                return isScrollEnabled; // mScrollable is always false at this point
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        Puts.i("INTERCEPT");
        if (!isScrollEnabled) return false;
        else {
            Puts.i("ALLOW");
            return super.onInterceptTouchEvent(ev);
        }
    }
}
