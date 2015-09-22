package com.allen.expandablelistview;

import android.view.View;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuLayout;
import com.baoyz.swipemenulistview.SwipeMenuView;

/**
 * @author yuchentang
 */
public class SwipeMenuViewForExpandable extends SwipeMenuView {

    private Swipable mListView;
    private SwipeMenuLayout mLayout;
    private SwipeMenu mMenu;
    private OnSwipeItemClickListenerForExpandable onItemClickListener;
    private int groupPosition, childPostion;

    public SwipeMenuViewForExpandable(SwipeMenu menu, Swipable listView, int groupPosition, int childPostion) {
        super(menu, listView);
        this.groupPosition = groupPosition;
        this.childPostion = childPostion;
    }

    public int getGroupPosition() {
        return this.groupPosition;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    public int getChildPostion() {
        return this.childPostion;
    }

    public void setChildPostion(int childPostion) {
        this.childPostion = childPostion;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null && mLayout.isOpen()) {
            onItemClickListener.onItemClick(this, mMenu, v.getId());
        }
    }

    public OnSwipeItemClickListenerForExpandable getOnSwipeItemClickListenerForExpandable() {
        return onItemClickListener;
    }

    public void setOnSwipeItemClickListenerForExpandable(OnSwipeItemClickListenerForExpandable onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setLayout(SwipeMenuLayout mLayout) {
        this.mLayout = mLayout;
    }

    public static interface OnSwipeItemClickListenerForExpandable {
        void onItemClick(SwipeMenuViewForExpandable view, SwipeMenu menu, int index);
    }
}
