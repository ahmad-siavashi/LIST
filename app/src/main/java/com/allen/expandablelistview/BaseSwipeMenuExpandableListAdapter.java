package com.allen.expandablelistview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.baoyz.swipemenulistview.ContentViewWrapper;

/**
 * @author yuchentang A sub class of BaseExpandableListAdapter , add controll to
 *         swipable
 */
public abstract class BaseSwipeMenuExpandableListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "BaseSwipeMenuExpandableListAdapter";
    public SwipeMenuExpandableListAdapter wrapperAdapter;

    public abstract boolean isGroupSwipable(int groupPosition);

    public abstract boolean isChildSwipable(int groupPosition, int childPosition);

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return getGroupViewAndReUsable(groupPosition, isExpanded, convertView, parent).view;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        return getChildViewAndReUsable(groupPosition, childPosition, isLastChild, convertView, parent).view;
    }

    public abstract ContentViewWrapper getGroupViewAndReUsable(int groupPosition, boolean isExpanded, View convertView,
                                                               ViewGroup parent);

    public abstract ContentViewWrapper getChildViewAndReUsable(int groupPosition, int childPosition,
                                                               boolean isLastChild, View convertView,
                                                               ViewGroup parent);

    /**
     * notify dataSetChanged, when ifKeepMenuOpen is true, ListView will keep
     * menu opened. notice that if use this feature, you'd better put
     * SwipeListView in RelativeLayout/FrameLayout for a better visual
     * effect,otherwise the item which's menu is opened will flash when
     * notifyDataSetChanged(true)
     *
     * @param ifKeepMenuOpen
     */
    public void notifyDataSetChanged(boolean ifKeepMenuOpen) {
        if (ifKeepMenuOpen && wrapperAdapter != null) {
            wrapperAdapter.notifyDataSetChanged(ifKeepMenuOpen);
        } else {
            this.notifyDataSetChanged();
        }
    }
}
