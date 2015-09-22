package com.ahmadsiavashi.list;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Navarch on 9/2/2015.
 */
public class SwipeableViewPager extends ViewPager {
    private boolean swipeable = true;

    public SwipeableViewPager(Context context) {
        super(context);
    }

    public SwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        //point is inside view bounds
        return (x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()));
    }

    // Call this method in your motion events when you want to disable or enable
    // It should work as desired.
    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        setSwipeable(!isPointInsideView(arg0.getRawX(), arg0.getRawY(), MainActivity.getCurrentInstance().getCurrentDayViewFragment().getSwipeMenuExpandableListView()));
        return (this.swipeable) && super.onInterceptTouchEvent(arg0);
    }

}