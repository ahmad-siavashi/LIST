package com.ahmadsiavashi.list;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {
    private static final String PERSIAN_FONT_ADDRESS = "fonts/Far_Yekan.ttf";
    private static MainActivity currentInstance;
    private SwipeableViewPager swipeableViewPager;
    private DayPageAdapter dayPageAdapter;
    public boolean isPersianLanguage = false;
    public boolean isJalaliCalendar = false;
    public Typeface persianTypeface;

    public static MainActivity getCurrentInstance() {
        return currentInstance;
    }

    public static void setCurrentInstance(MainActivity currentInstance) {
        MainActivity.currentInstance = currentInstance;
    }

    public DayViewFragment getCurrentDayViewFragment() {
        DayViewFragment currentDayViewFragment = (DayViewFragment) getDayPageAdapter().getRegisteredFragment(getSwipeableViewPager().getCurrentItem());
        Log.d("MainActivity", "getCurrentDayViewFragment() is " + ((currentDayViewFragment == null) ? "NULL" : ("Instance " + currentDayViewFragment.getTxtHeaderDate().getText())));
        return currentDayViewFragment;
    }

    public DayPageAdapter getDayPageAdapter() {
        return this.dayPageAdapter;
    }

    public void setDayPageAdapter(DayPageAdapter dayPageAdapter) {
        this.dayPageAdapter = dayPageAdapter;
    }

    public SwipeableViewPager getSwipeableViewPager() {
        return this.swipeableViewPager;
    }

    public void setSwipeableViewPager(SwipeableViewPager swipeableViewPager) {
        this.swipeableViewPager = swipeableViewPager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreated()");
        super.onCreate(savedInstanceState);

        setContentView(com.ahmadsiavashi.list.R.layout.activity_main);

        if (savedInstanceState == null) {
            Log.d("MainActivity", "InputItemFragment Added.");
            getFragmentManager()
                    .beginTransaction()
                    .add(com.ahmadsiavashi.list.R.id.fragment_input_item, new InputItemFragment())
                    .commit();
        }

        setCurrentInstance(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String language = sharedPreferences.getString("language", null);
        String calendar = sharedPreferences.getString("calendar", null);
        if (language == null) {
            sharedPreferences.edit().putString("language", "device_default").apply();
            language = "device_default";
        }
        if (Resources.getSystem().getConfiguration().locale.getLanguage().equals(new Locale("fa").getLanguage())) {
            // Persian System Context
            isPersianLanguage = true;
            persianTypeface = Typeface.createFromAsset(MainActivity.getCurrentInstance().getAssets(), PERSIAN_FONT_ADDRESS);
        } else {
            // Non-Persian System Context
            if (language.equals("device_default")) {
                if (!getResources().getConfiguration().locale.equals(Resources.getSystem().getConfiguration().locale)) {
                    setLanguage(Resources.getSystem().getConfiguration().locale.getLanguage(), Resources.getSystem().getConfiguration().locale.getCountry(), Resources.getSystem().getConfiguration().locale.getVariant());
                }
            } else {
                // Language == Persian
                if (!getResources().getConfiguration().locale.getLanguage().equals(new Locale("fa").getLanguage())) {
                    setLanguage("fa");
                } else {
                    isPersianLanguage = true;
                    persianTypeface = Typeface.createFromAsset(MainActivity.getCurrentInstance().getAssets(), PERSIAN_FONT_ADDRESS);
                }
            }
        }

        if (calendar == null) {
            isJalaliCalendar = isPersianLanguage;
            if (isJalaliCalendar) {
                sharedPreferences.edit().putString("calendar", "jalali_calendar").apply();
            } else {
                sharedPreferences.edit().putString("calendar", "gregorian_calendar").apply();
            }
        } else if (calendar.equalsIgnoreCase("jalali_calendar")) {
            isJalaliCalendar = true;
        }

        setSwipeableViewPager((SwipeableViewPager) findViewById(com.ahmadsiavashi.list.R.id.view_pager_day_view));
        setViewPagerInterval(new Date());
    }

    public void setViewPagerInterval(Date today) {
        this.setDayPageAdapter(new DayPageAdapter(getFragmentManager(), today));
        this.getSwipeableViewPager().setAdapter(this.getDayPageAdapter());
        this.getSwipeableViewPager().setCurrentItem(DayPageAdapter.TODAY);
    }

    public void setLanguage(String language) {
        Locale locale;
        try {
            locale = new Locale(language);
            Locale.setDefault(locale);
        } catch (Exception e) {
            Log.e("SetLanguage", e.getMessage());
            locale = Locale.getDefault();
        }
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        getApplicationContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        this.getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        recreate();
    }

    public void setLanguage(String language, String country, String variant) {
        Locale locale;
        try {
            locale = new Locale(language, country, variant);
            Locale.setDefault(locale);
        } catch (Exception e) {
            Log.e("SetLanguage", e.getMessage());
            locale = Locale.getDefault();
        }
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        getApplicationContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        this.getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        recreate();
    }

}

