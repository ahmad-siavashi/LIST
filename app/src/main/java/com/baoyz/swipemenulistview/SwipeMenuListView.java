package com.baoyz.swipemenulistview;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allen.expandablelistview.Swipable;

/**
 * @author baoyz
 * @date 2014-8-18
 */
public class SwipeMenuListView extends ListView implements Swipable {

    /**
     * stick with item right side, when swipe, it moves from outside of screen *
     */
    public static final int STICK_TO_ITEM_RIGHT_SIDE = 0;
    /**
     * stick with the screen, it was covered and don't move ,item moves then
     * menu show
     */
    public static final int STICK_TO_SCREEN = 1;
    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_X = 1;
    private static final int TOUCH_STATE_Y = 2;
    private int MAX_Y = 5;
    private int MAX_X = 3;
    private float mDownX;
    private float mDownY;
    private int mTouchState;
    private int mTouchPosition;
    private SwipeMenuLayout mTouchView;
    private OnSwipeListener mOnSwipeListener;
    private SwipeMenuCreator mMenuCreator;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private Interpolator mCloseInterpolator;
    private Interpolator mOpenInterpolator;
    private Object lock = new Object();
    /**
     * where does the menu item stick with *
     */
    private int mMenuStickTo = STICK_TO_ITEM_RIGHT_SIDE;

    public SwipeMenuListView(Context context) {
        super(context);
        init();
    }

    public SwipeMenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SwipeMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeMenuLayout getTouchView() {
        synchronized (lock) {
            return mTouchView;
        }
    }

    public void setTouchView(View v) {
        synchronized (lock) {
            mTouchView = (SwipeMenuLayout) v;
        }
    }

    public int getOpenedPosition() {
        synchronized (lock) {
            if (null == mTouchView || !mTouchView.isOpen())
                return -1;
            try {
                return this.getPositionForView(mTouchView);
            } catch (NullPointerException e) {
                return -1;
            }
        }
    }

    public int getmMenuStickTo() {
        return this.mMenuStickTo;
    }

    /**
     * STICK_TO_ITEM_RIGHT_SIDE: Stick with item right side, when swipe, it
     * moves from outside of screen . STICK_TO_SCREEN: Stick with the screen, it
     * was covered and don't move ,item moves then menu show.
     *
     * @param mMenuStickTo
     */
    public void setmMenuStickTo(int mMenuStickTo) {
        this.mMenuStickTo = mMenuStickTo;
    }

    private void init() {
        MAX_X = dp2px(MAX_X);
        MAX_Y = dp2px(MAX_Y);
        mTouchState = TOUCH_STATE_NONE;
    }

    @Override
    @Deprecated
    public void setAdapter(ListAdapter adapter) {
        throw new IllegalArgumentException("adapter should be type :BaseSwipeListAdapter");
    }

    public void setAdapter(BaseSwipeListAdapter adapter) {
        SwipeMenuAdapter mAdapter = new SwipeMenuAdapter(getContext(), adapter, this) {
            @Override
            public void createMenu(SwipeMenu menu) {
                if (mMenuCreator != null) {
                    mMenuCreator.create(menu);
                }
            }

            @Override
            public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
                boolean flag = false;
                if (mOnMenuItemClickListener != null) {
                    flag = mOnMenuItemClickListener.onMenuItemClick(view.getPosition(), menu, index);
                }
                synchronized (lock) {
                    if (mTouchView != null && !flag) {
                        mTouchView.smoothCloseMenu();
                    }
                }
            }
        };
        adapter.wrapperAdapter = mAdapter;
        super.setAdapter(mAdapter);
    }

    public Interpolator getOpenInterpolator() {
        return mOpenInterpolator;
    }

    public void setOpenInterpolator(Interpolator interpolator) {
        mOpenInterpolator = interpolator;
    }

    public Interpolator getCloseInterpolator() {
        return mCloseInterpolator;
    }

    public void setCloseInterpolator(Interpolator interpolator) {
        mCloseInterpolator = interpolator;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        synchronized (lock) {

            if (ev.getAction() != MotionEvent.ACTION_DOWN && mTouchView == null)
                return super.onTouchEvent(ev);
            int action = MotionEventCompat.getActionMasked(ev);
            action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    int oldPos = mTouchPosition;
                    mDownX = ev.getX();
                    mDownY = ev.getY();
                    mTouchState = TOUCH_STATE_NONE;

                    mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());

                    if (mTouchPosition == oldPos && mTouchView != null && mTouchView.isOpen()) {
                        mTouchState = TOUCH_STATE_X;
                        mTouchView.onSwipe(ev);
                        return true;
                    }

                    View view = getChildAt(mTouchPosition - getFirstVisiblePosition());

                    if (mTouchView != null && mTouchView.isOpen()) {
                        mTouchView.smoothCloseMenu();
                        mTouchView = null;
                        // return super.onTouchEvent(ev);
                    }
                    if (view instanceof SwipeMenuLayout) {
                        mTouchView = (SwipeMenuLayout) view;
                    }
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dy = Math.abs((ev.getY() - mDownY));
                    float dx = Math.abs((ev.getX() - mDownX));
                    if (mTouchState == TOUCH_STATE_X) {
                        if (mTouchView != null) {
                            mTouchView.onSwipe(ev);
                        }
                        getSelector().setState(new int[]{0});
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.onTouchEvent(ev);
                        return true;
                    } else if (mTouchState == TOUCH_STATE_NONE) {
                        if (Math.abs(dy) > MAX_Y) {
                            mTouchState = TOUCH_STATE_Y;
                        } else if (dx > MAX_X) {
                            mTouchState = TOUCH_STATE_X;
                            if (mOnSwipeListener != null) {
                                mOnSwipeListener.onSwipeStart(mTouchPosition);
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTouchState == TOUCH_STATE_X) {
                        if (mTouchView != null) {
                            mTouchView.onSwipe(ev);
                            if (!mTouchView.isOpen()) {
                                mTouchPosition = -1;
                                mTouchView = null;
                            }
                        }
                        if (mOnSwipeListener != null) {
                            mOnSwipeListener.onSwipeEnd(mTouchPosition);
                        }
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.onTouchEvent(ev);
                        return true;
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    public void smoothOpenMenu(int position) {
        if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
            View view = getChildAt(position - getFirstVisiblePosition());
            if (view instanceof SwipeMenuLayout) {
                mTouchPosition = position;
                synchronized (lock) {
                    if (mTouchView != null && mTouchView.isOpen()) {
                        mTouchView.smoothCloseMenu();
                    }
                    mTouchView = (SwipeMenuLayout) view;
                    mTouchView.smoothOpenMenu();
                }
            }
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources()
                .getDisplayMetrics());
    }

    public void setMenuCreator(SwipeMenuCreator menuCreator) {
        this.mMenuCreator = menuCreator;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;
    }

    public static interface OnMenuItemClickListener {
        boolean onMenuItemClick(int position, SwipeMenu menu, int index);
    }

    public static interface OnSwipeListener {
        void onSwipeStart(int position);

        void onSwipeEnd(int position);
    }
}
