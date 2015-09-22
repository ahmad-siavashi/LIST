package com.allen.expandablelistview;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.allen.expandablelistview.SwipeMenuExpandableListView.OnMenuItemClickListenerForExpandable;
import com.allen.expandablelistview.SwipeMenuViewForExpandable.OnSwipeItemClickListenerForExpandable;
import com.baoyz.swipemenulistview.ContentViewWrapper;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuLayout;

/**
 * @author yuchentang
 */
public class SwipeMenuExpandableListAdapter implements ExpandableListAdapter, OnSwipeItemClickListenerForExpandable {

    public static final int GROUP_INDEX = -1991;// when a group's swipe menu was
    View v = null, tv;
    int i;
    BaseSwipeMenuExpandableListAdapter mAdapter;
    // clicked, it fires an onclick
    // event which childPostion is
    // -1991
    private SwipeMenuExpandableListView mList;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mList.smoothOpenMenu(msg.arg1);
                    remove();
                    // this.sendEmptyMessageDelayed(1, 0);
                    break;
                case 1:
                    if (v != null) {
                        remove();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Context mContext;
    private OnMenuItemClickListenerForExpandable onMenuItemClickListener;

    public SwipeMenuExpandableListAdapter(Context context, BaseSwipeMenuExpandableListAdapter adapter,
                                          SwipeMenuExpandableListView lv) {
        mAdapter = adapter;
        mContext = context;
        mList = lv;
    }

    private void remove() {
        ((ViewGroup) mList.getParent()).removeView(v);
        v = null;
    }

    public void notifyDataSetChanged(boolean ifKeepMenuOpen) {
        if (v != null) {
            remove();
        }
        i = mList.getOpenedPosition();
        tv = mList.getTouchView();
        if (i >= 0 && tv != null
                && (mList.getParent() instanceof RelativeLayout || mList.getParent() instanceof FrameLayout)) {
            ViewGroup.LayoutParams lpNew = null;
            int[] fLo = new int[2];
            int[] sLo = new int[2];
            int[] sLoInwindow = new int[2];
            ((ViewGroup) mList.getParent()).getLocationOnScreen(fLo);
            tv.getLocationOnScreen(sLo);
            tv.getLocationInWindow(sLoInwindow);
            if (mList.getParent() instanceof RelativeLayout) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mList.getLayoutParams();
                lpNew = new LayoutParams(tv.getWidth(), tv.getHeight());
                ((RelativeLayout.LayoutParams) lpNew).setMargins(lp.leftMargin, sLo[1] - fLo[1], lp.rightMargin, 0);
            } else if (mList.getParent() instanceof FrameLayout) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mList.getLayoutParams();
                lpNew = new FrameLayout.LayoutParams(tv.getWidth(), tv.getHeight());
                ((FrameLayout.LayoutParams) lpNew).setMargins(lp.leftMargin, sLo[1] - fLo[1], lp.rightMargin, 0);
            }
            v = new View(mContext);
            v.setBackgroundDrawable(getDrawable(tv));
            ((ViewGroup) mList.getParent()).addView(v, lpNew);
        }
        mAdapter.notifyDataSetChanged();
        Log.i("keep", "posi is:" + i);
        if (ifKeepMenuOpen && i >= 0) {
            Message m = new Message();
            m.what = 0;
            m.arg1 = i;
            mHandler.sendMessageDelayed(m, 0);
        } else {
            mList.setTouchView(null);
        }
    }

    private Drawable getDrawable(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return new BitmapDrawable(b);
    }

    public void createGroupMenu(SwipeMenu menu) {

    }

    public void createChildMenu(SwipeMenu menu) {

    }

    @Override
    public void onItemClick(SwipeMenuViewForExpandable view, SwipeMenu menu, int index) {
        if (onMenuItemClickListener != null) {
            onMenuItemClickListener.onMenuItemClick(view.getGroupPosition(), view.getChildPostion(), menu, index);
        }
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListenerForExpandable onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    public BaseSwipeMenuExpandableListAdapter getWrappedAdapter() {
        return mAdapter;
    }

    @Override
    public int getGroupCount() {
        return mAdapter.getGroupCount();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mAdapter.getChildrenCount(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mAdapter.getGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mAdapter.getChild(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mAdapter.getGroupId(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mAdapter.getChildId(groupPosition, childPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (!mAdapter.isGroupSwipable(groupPosition))
            return mAdapter.getGroupViewAndReUsable(groupPosition, isExpanded, convertView, parent).view;
        SwipeMenuLayout layout = null;
        View subConvert = null;
        if (convertView instanceof SwipeMenuLayout) {
            subConvert = ((SwipeMenuLayout) convertView).getContentView();
        }
        ContentViewWrapper contentViewWrapper = mAdapter.getGroupViewAndReUsable(groupPosition, isExpanded, subConvert,
                parent);
        if (convertView == null || !(convertView instanceof SwipeMenuLayout) || !contentViewWrapper.ifReUsable) {
            SwipeMenu menu = new SwipeMenu(mContext);
            menu.setViewType(mAdapter.getGroupType(groupPosition));
            createGroupMenu(menu);
            SwipeMenuViewForExpandable menuView = new SwipeMenuViewForExpandable(menu,
                    (SwipeMenuExpandableListView) parent, groupPosition, GROUP_INDEX);
            menuView.setOnSwipeItemClickListenerForExpandable(this);
            SwipeMenuExpandableListView listView = (SwipeMenuExpandableListView) parent;
            layout = new SwipeMenuLayout(contentViewWrapper.view, menuView, listView.getCloseInterpolator(),
                    listView.getOpenInterpolator(), listView.getmMenuStickTo());
            layout.setPosition(groupPosition);
        } else {
            layout = (SwipeMenuLayout) convertView;
            layout.closeMenu();
            layout.setPosition(groupPosition);
        }
        return layout;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        if (!mAdapter.isChildSwipable(groupPosition, childPosition)) {
            return mAdapter.getChildViewAndReUsable(groupPosition, childPosition, isLastChild, convertView, parent).view;
        }
        SwipeMenuLayout layout = null;
        View subConvert = null;
        if (convertView instanceof SwipeMenuLayout) {
            subConvert = ((SwipeMenuLayout) convertView).getContentView();
        }
        ContentViewWrapper contentViewWrapper = mAdapter.getChildViewAndReUsable(groupPosition, childPosition,
                isLastChild, subConvert, parent);
        if (convertView == null || !(convertView instanceof SwipeMenuLayout) || !contentViewWrapper.ifReUsable) {

            SwipeMenu menu = new SwipeMenu(mContext);
            menu.setViewType(mAdapter.getChildType(groupPosition, childPosition));
            createChildMenu(menu);
            SwipeMenuViewForExpandable menuView = new SwipeMenuViewForExpandable(menu,
                    (SwipeMenuExpandableListView) parent, groupPosition, childPosition);
            menuView.setOnSwipeItemClickListenerForExpandable(this);
            Log.i("ChildViewType", mAdapter.getChildType(groupPosition, childPosition) + "");
            SwipeMenuExpandableListView listView = (SwipeMenuExpandableListView) parent;
            layout = new SwipeMenuLayout(contentViewWrapper.view, menuView, listView.getCloseInterpolator(),
                    listView.getOpenInterpolator(), listView.getmMenuStickTo());
            layout.setPosition(groupPosition);
        } else {
            layout = (SwipeMenuLayout) convertView;
            layout.closeMenu();
            layout.setPosition(groupPosition);
        }
        return layout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return mAdapter.isChildSelectable(groupPosition, childPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        mAdapter.onGroupExpanded(groupPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        mAdapter.onGroupCollapsed(groupPosition);
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return mAdapter.getCombinedChildId(groupId, childId);
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return mAdapter.getCombinedGroupId(groupId);
    }

}
