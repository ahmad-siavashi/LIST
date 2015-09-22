package com.baoyz.swipemenulistview;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.WrapperListAdapter;

import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.baoyz.swipemenulistview.SwipeMenuView.OnSwipeItemClickListener;

/**
 * @author baoyz
 * @date 2014-8-24
 */
public class SwipeMenuAdapter implements WrapperListAdapter, OnSwipeItemClickListener {

    SwipeMenuLayout tv;
    View v;
    long last_time = System.currentTimeMillis();
    private BaseSwipeListAdapter mAdapter;
    private Context mContext;
    private OnMenuItemClickListener onMenuItemClickListener;
    private SwipeMenuListView mList;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mList.smoothOpenMenu(msg.arg1);
                    this.sendEmptyMessageDelayed(1, 250);
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
    public SwipeMenuAdapter(Context context, BaseSwipeListAdapter adapter, SwipeMenuListView listView) {
        mAdapter = adapter;
        mContext = context;
        mList = listView;
    }

    private void remove() {
        ((ViewGroup) mList.getParent()).removeView(v);
        v = null;
    }

    public void notifyDataSetChanged(boolean ifKeepMenuOpen) {
        if (System.currentTimeMillis() - last_time < 370) {
            return;
        }
        last_time = System.currentTimeMillis();
        int i = -1;
        if (ifKeepMenuOpen) {
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

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SwipeMenuLayout layout = null;
        View subConvert = null;
        if (convertView instanceof SwipeMenuLayout) {
            subConvert = ((SwipeMenuLayout) convertView).getContentView();
        }
        ContentViewWrapper contentViewWrapper = mAdapter.getViewAndReusable(position, subConvert, parent);
        if (convertView == null || !contentViewWrapper.ifReUsable) {
            SwipeMenu menu = new SwipeMenu(mContext);
            menu.setViewType(mAdapter.getItemViewType(position));
            createMenu(menu);
            SwipeMenuView menuView = new SwipeMenuView(menu, (SwipeMenuListView) parent);
            menuView.setOnSwipeItemClickListener(this);
            SwipeMenuListView listView = (SwipeMenuListView) parent;
            layout = new SwipeMenuLayout(contentViewWrapper.view, menuView, listView.getCloseInterpolator(),
                    listView.getOpenInterpolator(), listView.getmMenuStickTo());
            layout.setPosition(position);
        } else {
            layout = (SwipeMenuLayout) convertView;
            layout.closeMenu();
            layout.setPosition(position);
        }
        return layout;
    }

    public void createMenu(SwipeMenu menu) {
        // Test Code
        SwipeMenuItem item = new SwipeMenuItem(mContext);
        item.setTitle("Item 1");
        item.setBackground(new ColorDrawable(Color.GRAY));
        item.setWidth(300);
        menu.addMenuItem(item);

        item = new SwipeMenuItem(mContext);
        item.setTitle("Item 2");
        item.setBackground(new ColorDrawable(Color.RED));
        item.setWidth(300);
        menu.addMenuItem(item);
    }

    @Override
    public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
        if (onMenuItemClickListener != null) {
            onMenuItemClickListener.onMenuItemClick(view.getPosition(), menu, index);
        }
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
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
    public boolean isEnabled(int position) {
        return mAdapter.isEnabled(position);
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }

}
