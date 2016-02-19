package com.sandklef.coachapp.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.sandklef.coachapp.misc.Log;

/**
 * Created by hesa on 2016-02-15.
 */
public class AdaptedSwipeViewPager extends ViewPager {
    private final static String LOG_TAG = AdaptedSwipeViewPager.class.getSimpleName();
    private double lastX;

    //private boolean swipeLocked;
    private int maxIndex;

    public AdaptedSwipeViewPager(Context context) {
        super(context);
        lastX=0.0;
        maxIndex = Integer.MAX_VALUE;
        Log.d(LOG_TAG, " constructor()");
    }

    public AdaptedSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        maxIndex = Integer.MAX_VALUE;
        lastX=0.0;
        Log.d(LOG_TAG, " constructor()");
    }

    public void setPagingMax(int limit) {
        maxIndex = limit;
    }

    public boolean swipeLocked() {
        boolean result = getCurrentItem()<=maxIndex;
        Log.d(LOG_TAG, " " + result + "  <==== " + getCurrentItem() + " < " + maxIndex);
        return result;
    }

    private boolean goingLeft(MotionEvent event) {
        boolean left  = false;
        double  X      = event.getX(0);
        if (lastX!=0.0) {
            left = X > lastX;
        }
        lastX = X;
        return left;
    }

    public boolean allowSwipe(MotionEvent event) {
        return (!swipeLocked()) || goingLeft(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return allowSwipe(event) && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return allowSwipe(event)  && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return !swipeLocked() && super.canScrollHorizontally(direction);
    }

*/
}
