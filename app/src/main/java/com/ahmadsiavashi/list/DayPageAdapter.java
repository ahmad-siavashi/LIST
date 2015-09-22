package com.ahmadsiavashi.list;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Navarch on 8/26/2015.
 */

public class DayPageAdapter extends FragmentStatePagerAdapter {
    public static int DAYS_TO_SHOW_MARGIN = 365;
    public static int TODAY = 2 * DAYS_TO_SHOW_MARGIN / 2;

    private Date today;
    private FragmentManager fragmentManager;
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private Fragment fragmentToRemove;

    public DayPageAdapter(FragmentManager fragmentManager, Date date) {
        super(fragmentManager);
        this.setFragmentManager(fragmentManager);
        this.today = date;
        Log.d("DayPageAdapter", "A new DayPageAdapter Instantiated.");
    }

    public Fragment getRegisteredFragment(int position) {
        return this.getRegisteredFragments().get(position);
    }

    @Override
    public Fragment getItem(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        if (MainActivity.getCurrentInstance().isPersianLanguage) {
            calendar.add(Calendar.DATE, TODAY - position);
        } else {
            calendar.add(Calendar.DATE, position - TODAY);
        }
        DayViewFragment dayViewFragment = new DayViewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DayViewFragment.DATE_KEY, calendar.getTime());
        dayViewFragment.setArguments(bundle);
        Log.d("DayPageAdapter", "Item Pos:" + position + " Created.");
        return dayViewFragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        getRegisteredFragments().put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        getRegisteredFragments().remove(position);
        super.destroyItem(container, position, object);
        Log.d("DayPageAdapter", "Item Pos: " + position + " Destroyed.");
    }

    @Override
    public int getCount() {
        return 2 * DAYS_TO_SHOW_MARGIN;
    }

    public void notifyDataSetChanged(String date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Item.DATE_PATTERN, Locale.US);
            Date inputDate = simpleDateFormat.parse(date);
            Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(today));
            long duration;
            if (MainActivity.getCurrentInstance().isPersianLanguage) {
                duration = todayDate.getTime() - inputDate.getTime();
            } else {
                duration = inputDate.getTime() - todayDate.getTime();
            }
            long diffDay = TimeUnit.MILLISECONDS.toDays(duration);
            if (Math.abs(diffDay) > DAYS_TO_SHOW_MARGIN)
                return;
            Integer index = (int) diffDay + TODAY;
            DayViewFragment dayViewFragment = (DayViewFragment) getRegisteredFragment(index);
            Log.d("DayPageAdapter", "notifyDataSetChanged: " + "Fragment #" + index + " to be Deleted.");
            fragmentToRemove = dayViewFragment;
            if (fragmentToRemove != null)
                notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("DayPageAdapter", "notifyDataSetChanged: " + e.getMessage());
        }
    }

    @Override
    public int getItemPosition(Object object) {
        if (object.equals(fragmentToRemove)) {
            Log.d("DayPageAdapter", "getItemPosition: " + "Fragment got Deleted.");
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    private SparseArray<Fragment> getRegisteredFragments() {
        return registeredFragments;
    }

    public void setRegisteredFragments(SparseArray<Fragment> registeredFragments) {
        this.registeredFragments = registeredFragments;
    }
}
