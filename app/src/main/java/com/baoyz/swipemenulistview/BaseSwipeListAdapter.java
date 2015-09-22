package com.baoyz.swipemenulistview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public abstract class BaseSwipeListAdapter extends BaseAdapter {
    private static final String TAG = "BaseSwipeAdapter";
    public SwipeMenuAdapter wrapperAdapter;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewAndReusable(position, convertView, parent).view;
    }

    public abstract ContentViewWrapper getViewAndReusable(int position, View convertView, ViewGroup parent);

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
