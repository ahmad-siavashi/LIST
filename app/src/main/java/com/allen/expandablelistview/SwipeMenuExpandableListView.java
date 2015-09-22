package com.allen.expandablelistview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuLayout;
import com.baoyz.swipemenulistview.SwipeMenuListView;

/**
 * @author yuchentang
 */
public class SwipeMenuExpandableListView extends ExpandableListView implements Swipable {
    private static final String TAG = "SwipeMenuExpandableListView";
    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_X = 1;
    private static final int TOUCH_STATE_Y = 2;
    private final Object lock = new Object();
    private int MAX_Y = 5;
    private int MAX_X = 3;
    private float mDownX;
    private float mDownY;
    private int mTouchState;
    private int mTouchPosition;
    private SwipeMenuLayout mTouchView;
    private OnSwipeListener mOnSwipeListener;
    private SwipeMenuExpandableCreator mMenuCreator;
    private OnMenuItemClickListenerForExpandable mOnMenuItemClickListener;
    private Interpolator mCloseInterpolator;
    private Interpolator mOpenInterpolator;
    private SwipeMenuExpandableListAdapter mAdapter;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:

                    break;
            }
            super.handleMessage(msg);
        }
    };
    /**
     * where does the menu item stick with *
     */
    private int mMenuStickTo = SwipeMenuListView.STICK_TO_ITEM_RIGHT_SIDE;

    public SwipeMenuExpandableListView(Context context) {
        super(context);
        init();
    }

    public SwipeMenuExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SwipeMenuExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeMenuLayout getTouchView() {
        // synchronized (lock) {
        return mTouchView;
        // }
    }

    public void setTouchView(View v) {
        // synchronized (lock) {''
        mTouchView = (SwipeMenuLayout) v;
        // }
    }

    public int getOpenedPosition() {
        // synchronized (lock) {
        if (null == mTouchView || !mTouchView.isOpen())
            return -1;
        try {
            return this.getPositionForView(mTouchView);
        } catch (NullPointerException e) {
            return -1;
        }
        // }
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

    /**
     * 寮哄埗浣跨敤姝ゆ柟娉曪紝BaseExpandableListAdapter锛屽彲浠ュ垏鎹hild鐨刲ayout
     *
     * @param adapter
     */
    public void setAdapter(BaseSwipeMenuExpandableListAdapter adapter) {
        mAdapter = new SwipeMenuExpandableListAdapter(getContext(), adapter, this) {
            @Override
            public void createGroupMenu(SwipeMenu menu) {
                if (mMenuCreator != null) {
                    mMenuCreator.createGroup(menu);
                }
            }

            @Override
            public void createChildMenu(SwipeMenu menu) {
                if (mMenuCreator != null) {
                    mMenuCreator.createChild(menu);
                }
            }

            @Override
            public void onItemClick(SwipeMenuViewForExpandable view, SwipeMenu menu, int index) {
                boolean flag = false;
                if (mOnMenuItemClickListener != null) {
                    int position = getPositionForView(view);
                    long packed = getExpandableListPosition(position);
                    int positionType = getPackedPositionType(packed);
                    int groupPosition, childPosition = GROUP_INDEX;
                    if (positionType != PACKED_POSITION_TYPE_NULL) {
                        // (Child绫诲瀷鏃朵篃鏈塆roup淇℃伅)
                        groupPosition = getPackedPositionGroup(packed);
                        // 濡傛灉鏄痗hild绫诲瀷,鍒欏彇鍑篶hildPosition
                        if (positionType == PACKED_POSITION_TYPE_CHILD) {
                            childPosition = getPackedPositionChild(packed);
                        }
                        flag = mOnMenuItemClickListener.onMenuItemClick(groupPosition, childPosition, menu, index);
                    }
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

    @Override
    @Deprecated
    public void setAdapter(ExpandableListAdapter adapter) {
        throw new IllegalArgumentException("adapter should be type :BaseSwipeMenuExpandableListAdapter");
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
        boolean res = super.onInterceptTouchEvent(ev);
        Log.i("swipe", "list intercept:" + res);
        return res;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean res = super.dispatchTouchEvent(ev);
        Log.i("swipe", "list dispatch:" + res);
        return res;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean res = this.takeTouchEvent(ev);
        Log.i("swipe", "list touch:" + res);
        return res;
    }

    public boolean takeTouchEvent(MotionEvent ev) {
        // synchronized (lock) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN && getTouchView() == null)
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
                } else if (mTouchState == TOUCH_STATE_NONE) {
                }
                break;
        }
        // }
        return super.onTouchEvent(ev);
    }

    public void smoothOpenMenu(int position) {
        if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
            View view = getChildAt(position - getFirstVisiblePosition());
            if (view instanceof SwipeMenuLayout) {
                mTouchPosition = position;
                // synchronized (lock) {
                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                }
                mTouchView = (SwipeMenuLayout) view;
                mTouchView.openMenu();
                // }
            }
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources()
                .getDisplayMetrics());
    }

    public void setMenuCreator(SwipeMenuExpandableCreator menuCreator) {
        this.mMenuCreator = menuCreator;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListenerForExpandable onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;
    }

    public static interface OnMenuItemClickListenerForExpandable {
        boolean onMenuItemClick(int groupPosition, int childPosition, SwipeMenu menu, int index);
    }

    public static interface OnSwipeListener {
        void onSwipeStart(int position);

        void onSwipeEnd(int position);
    }
}
