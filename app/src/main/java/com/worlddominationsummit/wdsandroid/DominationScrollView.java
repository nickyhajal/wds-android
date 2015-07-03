package com.worlddominationsummit.wdsandroid;

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
        isScrollEnabled = scrollEnabled;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (isScrollEnabled) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return isScrollEnabled; // mScrollable is always false at this point
            default:
                if (isScrollEnabled) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return isScrollEnabled; // mScrollable is always false at this point
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!isScrollEnabled) return false;
        else {
            return super.onInterceptTouchEvent(ev);
        }
    }
}
