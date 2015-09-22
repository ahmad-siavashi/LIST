package com.ahmadsiavashi.list;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.expandablelistview.BaseSwipeMenuExpandableListAdapter;
import com.allen.expandablelistview.SwipeMenuExpandableCreator;
import com.allen.expandablelistview.SwipeMenuExpandableListView;
import com.baoyz.swipemenulistview.ContentViewWrapper;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.mohamadamin.persianmaterialdatetimepicker.utils.LanguageUtils;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Navarch on 8/22/2015.
 */

public class DayViewFragment extends Fragment {

    public static final String DATE_KEY = "date";
    protected static final int INVALID_POSITION = -1;
    private static final String CURRENT_SELECTED_ITEM_POSITION = "currentSelectedItemPosition";
    private static final String LAST_EXPANDED_GROUP = "lastExpandedGroup";
    private static final String HEADER_DATE_PATTERN = "dd/MMM/yyyy";
    // Context Menu Constants
    private static final int MENU_CONTEXT_GROUP_ITEMS = 0;
    private static final int MENU_CONTEXT_GROUP_HEADER = 1;
    ////////////////////////////////////////////////////
    private static final int MENU_CONTEXT_EDIT_ID = 0;
    private static final int MENU_CONTEXT_POSTPONE_ID = 1;
    private static final int MENU_CONTEXT_DELETE_ID = 2;
    ////////////////////////////////////////////////////
    private static final int MENU_CONTEXT_PREFERENCES_ID = 0;
    private static final int MENU_CONTEXT_ABOUT_ID = 1;
    private View view;
    private Bundle bundle;
    private SwipeMenuExpandableListView swipeMenuExpandableListView;
    private TextView txtHeaderDate;
    private TextView txtHeaderDay;
    private ImageView imgHeaderProgress;
    private ItemListAdapter itemListAdapter;
    private SwipeMenuExpandableCreator swipeMenuExpandableCreator;
    private List<Item> items;
    private int currentSelectedItemPosition = INVALID_POSITION;
    private int lastExpandedGroup = INVALID_POSITION;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("DayViewFragment", "onSaveInstanceState: " + getTxtHeaderDate().getText());
        outState.putInt(CURRENT_SELECTED_ITEM_POSITION, getCurrentSelectedItemPosition());
        outState.putInt(LAST_EXPANDED_GROUP, getLastExpandedGroup());
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Reload State
        if (savedInstanceState != null) {
            setCurrentSelectedItemPosition(savedInstanceState.getInt(CURRENT_SELECTED_ITEM_POSITION));
            setLastExpandedGroup(savedInstanceState.getInt(LAST_EXPANDED_GROUP));
        }
        // Inflate View + get arguments(the date to create the view for)
        setView(inflater.inflate(com.ahmadsiavashi.list.R.layout.day_view, container, false));
        setBundle(getArguments());
        // Find widgets in the view.
        setSwipeMenuExpandableListView((SwipeMenuExpandableListView) getView().findViewById(com.ahmadsiavashi.list.R.id.lvItems));
        getSwipeMenuExpandableListView().setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (getLastExpandedGroup() != groupPosition)
                    getSwipeMenuExpandableListView().collapseGroup(getLastExpandedGroup());
                Log.d("DayViewFragment", "Group " + groupPosition + " Expanded - Last: " + getLastExpandedGroup());
                setLastExpandedGroup(groupPosition);
                MainActivity.getCurrentInstance().getCurrentDayViewFragment().setCurrentSelectedItemPosition(groupPosition);
            }
        });

        setTxtHeaderDate((TextView) getView().findViewById(com.ahmadsiavashi.list.R.id.txtHeaderDate));
        setTxtHeaderDay((TextView) getView().findViewById(com.ahmadsiavashi.list.R.id.txtHeaderDay));
        setImgHeaderProgress((ImageView) getView().findViewById(com.ahmadsiavashi.list.R.id.imgHeaderProgress));

        // Initializing Header Day.
        getTxtHeaderDay().setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(getBundle().get(DayViewFragment.DATE_KEY)));
        // Initializing Header Date
        if (!getDate().equalsIgnoreCase(new SimpleDateFormat(Item.DATE_PATTERN, Locale.US).format(new Date()))) {
            getTxtHeaderDate().setTextColor(getResources().getColor(R.color.headerExpiredDate));
        }
        if (MainActivity.getCurrentInstance().isJalaliCalendar) {
            PersianCalendar persianCalendar = new PersianCalendar();
            persianCalendar.setTime((Date) getBundle().get(DayViewFragment.DATE_KEY));
            getTxtHeaderDate().setText(" " + LanguageUtils.getPersianNumbers(persianCalendar.getPersianDay() + "") + " " + persianCalendar.getPersianMonthName() + " " + LanguageUtils.getPersianNumbers(persianCalendar.getPersianYear()+"") + " ");
        } else {
            getTxtHeaderDate().setText(new SimpleDateFormat(DayViewFragment.HEADER_DATE_PATTERN, Locale.getDefault()).format(getBundle().get(DayViewFragment.DATE_KEY)));
        }
        Log.d("DayViewFragment", "onCreateView() for " + getTxtHeaderDate().getText());
        // Retrieve Items
        this.setItems(Item.find(Item.class, "date = ?", getDate()));
        Collections.sort(getItems());
        // Initializing Header Progressbar
        refreshHeaderProgressbar();
        // Initializing Items List
        setItemListAdapter(new ItemListAdapter(getActivity().getApplicationContext(), getItems()));
        getSwipeMenuExpandableListView().setAdapter(getItemListAdapter());
        // Item Swipe Menu Creation
        setSwipeMenuExpandableCreator(new SwipeMenuExpandableCreator() {
            @Override
            public void createGroup(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set item title
                deleteItem.setTitle(getString(R.string.delete));
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

                // create "Tomorrow" item
                SwipeMenuItem tomorrowItem = new SwipeMenuItem(getActivity().getApplicationContext());
                // set item background
                tomorrowItem.setBackground(new ColorDrawable(Color.rgb(255, 211, 15)));
                // set item width
                tomorrowItem.setWidth(dp2px(90));
                // set item title
                tomorrowItem.setTitle(getString(com.ahmadsiavashi.list.R.string.tomorrow));
                // set item title fontsize
                tomorrowItem.setTitleSize(18);
                // set item title font color
                tomorrowItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(tomorrowItem);

                // create "Done" item
                SwipeMenuItem doneItem = new SwipeMenuItem(getActivity().getApplicationContext());
                // set item background
                doneItem.setBackground(new ColorDrawable(Color.rgb(92, 230, 46)));
                // set item width
                doneItem.setWidth(dp2px(90));
                // set item title
                doneItem.setTitle(getString(com.ahmadsiavashi.list.R.string.item_done));
                // set item title fontsize
                doneItem.setTitleSize(18);
                // set item title font color
                doneItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(doneItem);
            }

            private int dp2px(int dp) {
                return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
            }

            @Override
            public void createChild(SwipeMenu menu) {
            }
        });
        getSwipeMenuExpandableListView().setMenuCreator(getSwipeMenuExpandableCreator());
        getSwipeMenuExpandableListView().setOnMenuItemClickListener(new SwipeMenuExpandableListView.OnMenuItemClickListenerForExpandable() {
            @Override
            public boolean onMenuItemClick(int groupPosition, int childPosition, SwipeMenu menu, int index) {
                final Item item = (Item) getItemListAdapter().getGroup(groupPosition);
                switch (index) {
                    // Delete
                    case 0:
                        DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                        ItemDeleteAlertDialog itemDeleteAlertDialog = new ItemDeleteAlertDialog(MainActivity.getCurrentInstance());
                        itemDeleteAlertDialog.setDayViewFragmentAndItem(dayViewFragment, item);
                        itemDeleteAlertDialog.show();
                        break;
                    // Tomorrow
                    case 1:
                        Date date = (Date) getArguments().getSerializable(DATE_KEY);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.add(Calendar.DATE, 1);
                        item.setDate(new SimpleDateFormat(Item.DATE_PATTERN, Locale.US).format(calendar.getTime()));
                        getItems().remove(item);
                        updateAndNotify(item, false);
                        MainActivity.getCurrentInstance().getDayPageAdapter().notifyDataSetChanged(item.getDate());
                        Toast.makeText(MainActivity.getCurrentInstance(), getString(com.ahmadsiavashi.list.R.string.better_late_than_never), Toast.LENGTH_SHORT).show();
                        break;
                    // Done
                    case 2:
                        if (item.setLevelToComplete()) {
                            updateAndNotify(item, false);
                            Toast.makeText(MainActivity.getCurrentInstance(), getString(com.ahmadsiavashi.list.R.string.congratulations), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            }
        });

        RelativeLayout headerLayout = (RelativeLayout) view.findViewById(com.ahmadsiavashi.list.R.id.headerLayout);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.getCurrentInstance().isJalaliCalendar) {
                    final PersianCalendar persianCalendar = new PersianCalendar();
                    persianCalendar.setTime((Date) getBundle().getSerializable(DATE_KEY));
                    int year = persianCalendar.getPersianYear();
                    int month = persianCalendar.getPersianMonth();
                    int day = persianCalendar.getPersianDay();
                    com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.newInstance(new com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog datePickerDialog, int year, int month, int day) {
                            persianCalendar.setPersianDate(year, month, day);
                            MainActivity.getCurrentInstance().setViewPagerInterval(persianCalendar.getTime());
                        }
                    }, true, new com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            MainActivity.getCurrentInstance().setViewPagerInterval(new Date());
                        }
                    }, year, month, day).show(getFragmentManager(), null);
                } else {
                    DialogFragment dialogFragment = new HeaderDatePicker();
                    dialogFragment.setArguments(getBundle());
                    dialogFragment.show(getFragmentManager(), null);
                }
            }
        });
        registerForContextMenu(getSwipeMenuExpandableListView());
        registerForContextMenu(headerLayout);

        // Persian Layout
        if (MainActivity.getCurrentInstance().isPersianLanguage) {
            getTxtHeaderDay().setTypeface(MainActivity.getCurrentInstance().persianTypeface);
            getTxtHeaderDate().setTypeface(MainActivity.getCurrentInstance().persianTypeface);
        }
        return getView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == com.ahmadsiavashi.list.R.id.lvItems) {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            String itemTitle = ((Item) MainActivity.getCurrentInstance().getCurrentDayViewFragment().getItemListAdapter().getGroup(SwipeMenuExpandableListView.getPackedPositionGroup(info.packedPosition))).getTitle();
            menu.setHeaderTitle(itemTitle);
            menu.add(MENU_CONTEXT_GROUP_ITEMS, MENU_CONTEXT_EDIT_ID, Menu.NONE, getString(com.ahmadsiavashi.list.R.string.edit));
            menu.add(MENU_CONTEXT_GROUP_ITEMS, MENU_CONTEXT_POSTPONE_ID, Menu.NONE, getString(R.string.postpone));
            menu.add(MENU_CONTEXT_GROUP_ITEMS, MENU_CONTEXT_DELETE_ID, Menu.NONE, getString(com.ahmadsiavashi.list.R.string.delete));
        } else if (v.getId() == com.ahmadsiavashi.list.R.id.headerLayout) {
            menu.add(MENU_CONTEXT_GROUP_HEADER, MENU_CONTEXT_PREFERENCES_ID, Menu.NONE, getString(R.string.preferences));
            menu.add(MENU_CONTEXT_GROUP_HEADER, MENU_CONTEXT_ABOUT_ID, Menu.NONE, getString(com.ahmadsiavashi.list.R.string.about));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getGroupId() == MENU_CONTEXT_GROUP_ITEMS) {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();
            final Item item = (Item) MainActivity.getCurrentInstance().getCurrentDayViewFragment().getItemListAdapter().getGroup(SwipeMenuExpandableListView.getPackedPositionGroup(info.packedPosition));
            switch (menuItem.getItemId()) {
                case MENU_CONTEXT_EDIT_ID:
                    try {
                        final DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                        LayoutInflater infalInflater = (LayoutInflater) MainActivity.getCurrentInstance()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View promptsView = infalInflater.inflate(com.ahmadsiavashi.list.R.layout.edit_list_item, null);
                        final Spinner userSpinnerInput = (Spinner) promptsView.findViewById(com.ahmadsiavashi.list.R.id.spinnerEditItemPriority);
                        userSpinnerInput.setAdapter(
                                new SpinnerAdapter(
                                        MainActivity.getCurrentInstance(),
                                        new String[]
                                                {MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_very_low), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_low), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_normal), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_high), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_critical)
                                                }
                                )
                        );
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                MainActivity.getCurrentInstance());
                        alertDialogBuilder.setView(promptsView);
                        final EditText userTitleInput = (EditText) promptsView
                                .findViewById(com.ahmadsiavashi.list.R.id.txtEditItemTitle);
                        final EditText userDescInput = (EditText) promptsView
                                .findViewById(com.ahmadsiavashi.list.R.id.txtEditItemDescription);
                        userTitleInput.setText(item.getTitle());
                        userDescInput.setText(item.getDescription());
                        userSpinnerInput.setSelection(item.getPriority().ordinal());
                        alertDialogBuilder
                                .setCancelable(true)
                                .setPositiveButton(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.done),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                item.setTitle(userTitleInput.getText().toString());
                                                item.setDescription(userDescInput.getText().toString());
                                                if (userSpinnerInput.getSelectedItemPosition() != item.getPriority().ordinal()) {
                                                    MainActivity.getCurrentInstance().getCurrentDayViewFragment().getSwipeMenuExpandableListView().collapseGroup(MainActivity.getCurrentInstance().getCurrentDayViewFragment().getCurrentSelectedItemPosition());
                                                    MainActivity.getCurrentInstance().getCurrentDayViewFragment().setCurrentSelectedItemPosition(DayViewFragment.INVALID_POSITION);
                                                }
                                                item.setPriority(userSpinnerInput.getSelectedItemPosition());
                                                dayViewFragment.updateAndNotify(item, true);
                                            }
                                        })
                                .setNegativeButton(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.cancel), null);

                        // Persian Layout
                        if (MainActivity.getCurrentInstance().isPersianLanguage) {
                            userTitleInput.setTypeface(MainActivity.getCurrentInstance().persianTypeface);
                            userDescInput.setTypeface(MainActivity.getCurrentInstance().persianTypeface);
                        }
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } catch (Exception e) {
                        Log.e("MenuContextEdit", e.getMessage());
                    }
                    return true;
                case MENU_CONTEXT_POSTPONE_ID:
                    if (MainActivity.getCurrentInstance().isJalaliCalendar) {
                        try {
                            final PersianCalendar persianCalendar = new PersianCalendar();
                            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Item.DATE_PATTERN, Locale.US);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(simpleDateFormat.parse(item.getDate()));
                            persianCalendar.setTime(calendar.getTime());
                            com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.newInstance(new com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog datePickerDialog, int year, int month, int day) {
                                    persianCalendar.setPersianDate(year, month, day);
                                    String newDate = simpleDateFormat.format(persianCalendar.getTime());
                                    if (!newDate.equalsIgnoreCase(item.getDate())) {
                                        MainActivity.getCurrentInstance().getCurrentDayViewFragment().changeItemDateAndNotify(item, newDate);
                                        MainActivity.getCurrentInstance().getDayPageAdapter().notifyDataSetChanged(newDate);
                                    }
                                }
                            }, true, new com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                    Date today = new Date();
                                    String newDate = simpleDateFormat.format(today);
                                    if (!newDate.equalsIgnoreCase(item.getDate())) {
                                        MainActivity.getCurrentInstance().getCurrentDayViewFragment().changeItemDateAndNotify(item, newDate);
                                        MainActivity.getCurrentInstance().getDayPageAdapter().notifyDataSetChanged(newDate);
                                    }
                                }
                            }, persianCalendar.getPersianYear(), persianCalendar.getPersianMonth(), persianCalendar.getPersianDay()).show(getFragmentManager(), null);
                        } catch (Exception e) {
                            Log.e("ContextMenuLater", e.getMessage());
                        }
                    } else {
                        DialogFragment dialogFragment = new ItemDatePicker();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Item", item);
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(MainActivity.getCurrentInstance().getFragmentManager(), null);
                    }
                    return true;
                case MENU_CONTEXT_DELETE_ID:
                    DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                    ItemDeleteAlertDialog itemDeleteAlertDialog = new ItemDeleteAlertDialog(MainActivity.getCurrentInstance());
                    itemDeleteAlertDialog.setDayViewFragmentAndItem(dayViewFragment, item);
                    itemDeleteAlertDialog.show();
                    return true;
                default:
                    return super.onContextItemSelected(menuItem);
            }
        } else if (menuItem.getGroupId() == MENU_CONTEXT_GROUP_HEADER) {
            switch (menuItem.getItemId()) {
                case MENU_CONTEXT_PREFERENCES_ID:
                    Intent intent = new Intent(MainActivity.getCurrentInstance(), PreferencesActivity.class);
                    startActivity(intent);
                    return true;
                case MENU_CONTEXT_ABOUT_ID:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(com.ahmadsiavashi.list.R.drawable.ic_launcher);
                    builder.setTitle(getString(com.ahmadsiavashi.list.R.string.about));
                    builder.setPositiveButton(getString(com.ahmadsiavashi.list.R.string.done), null);
                    builder.setCancelable(true);
                    View aboutDialogView = getActivity().getLayoutInflater().inflate(com.ahmadsiavashi.list.R.layout.about_dialog, null);
                    builder.setView(aboutDialogView);
                    builder.show();
                    return true;
                default:
                    return super.onContextItemSelected(menuItem);
            }
        }
        return super.onContextItemSelected(menuItem);
    }

    public String getDate() {
        return new SimpleDateFormat(Item.DATE_PATTERN, Locale.US).format((Date) getBundle().get(DATE_KEY));
    }

    public void updateAndNotify(Item item, boolean keepSwipeMenuOpen) {
        Log.d("DayViewFragment", "updateAndNotify: Item(" + item.getTitle() + ")");
        item.save();
        this.notifyItemsChanged(keepSwipeMenuOpen);
    }

    public void addAndNotify(Item newItem) {
        Log.d("DayViewFragment", "addAndNotify: Item(" + newItem.getTitle() + ", " + newItem.getDate() + ")");
        newItem.save();
        getItems().add(newItem);
        MainActivity.getCurrentInstance().getCurrentDayViewFragment().getSwipeMenuExpandableListView().collapseGroup(getCurrentSelectedItemPosition());
        setCurrentSelectedItemPosition(INVALID_POSITION);
        this.notifyItemsChanged(false);
    }

    public void removeAndNotify(Item item) {
        Log.d("DayViewFragment", "removeAndNotify: Item(" + item.getTitle() + ")");
        MainActivity.getCurrentInstance().getCurrentDayViewFragment().getSwipeMenuExpandableListView().collapseGroup(getCurrentSelectedItemPosition());
        setCurrentSelectedItemPosition(INVALID_POSITION);
        getItems().remove(item);
        item.delete();
        this.notifyItemsChanged(false);
    }

    public void changeItemDateAndNotify(Item item, String newDate) {
        Log.d("DayViewFragment", "changeItemDateAndNotify: Item(" + item.getTitle() + ")");
        MainActivity.getCurrentInstance().getCurrentDayViewFragment().getSwipeMenuExpandableListView().collapseGroup(getCurrentSelectedItemPosition());
        setCurrentSelectedItemPosition(INVALID_POSITION);
        getItems().remove(item);
        item.setDate(newDate);
        item.save();
        this.notifyItemsChanged(false);
    }

    public void notifyItemsChanged(boolean keepSwipeMenuOpen) {
        Log.d("DayViewFragment", "notifyItemsChanged");
        Collections.sort(getItems());
        getItemListAdapter().notifyDataSetChanged(keepSwipeMenuOpen);
        this.refreshHeaderProgressbar();
    }


    public void refreshHeaderProgressbar() {
        Log.d("DayViewFragment", "refreshHeaderProgressbar");
        int sum = 0;
        for (Item item : this.getItems()) {
            sum += item.getLevel().ordinal() * 100;
        }
        int avg = 0;
        if (getItems().size() != 0) {
            avg = (sum / getItems().size()) / (Level.getHighest().ordinal() - Level.getLowest().ordinal());
        }
        getImgHeaderProgress().setImageLevel(avg);
    }

    @Nullable
    @Override
    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public SwipeMenuExpandableListView getSwipeMenuExpandableListView() {
        return swipeMenuExpandableListView;
    }

    public void setSwipeMenuExpandableListView(SwipeMenuExpandableListView swipeMenuExpandableListView) {
        Log.d("DayViewFragment", "setSwipeMenuExpandableListView");
        this.swipeMenuExpandableListView = swipeMenuExpandableListView;
    }

    public TextView getTxtHeaderDate() {
        return txtHeaderDate;
    }

    public void setTxtHeaderDate(TextView txtHeaderDate) {
        this.txtHeaderDate = txtHeaderDate;
    }

    public TextView getTxtHeaderDay() {
        return txtHeaderDay;
    }

    public void setTxtHeaderDay(TextView txtHeaderDay) {
        this.txtHeaderDay = txtHeaderDay;
    }

    public ImageView getImgHeaderProgress() {
        return imgHeaderProgress;
    }

    public void setImgHeaderProgress(ImageView imgHeaderProgress) {
        this.imgHeaderProgress = imgHeaderProgress;
    }

    public ItemListAdapter getItemListAdapter() {
        return itemListAdapter;
    }

    public void setItemListAdapter(ItemListAdapter itemListAdapter) {
        this.itemListAdapter = itemListAdapter;
    }

    public SwipeMenuExpandableCreator getSwipeMenuExpandableCreator() {
        return swipeMenuExpandableCreator;
    }

    public void setSwipeMenuExpandableCreator(SwipeMenuExpandableCreator swipeMenuExpandableCreator) {
        this.swipeMenuExpandableCreator = swipeMenuExpandableCreator;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public int getCurrentSelectedItemPosition() {
        return currentSelectedItemPosition;
    }

    public void setCurrentSelectedItemPosition(int currentSelectedItemPosition) {
        Log.d("DayViewFragment", "CurrentItemPosition: " + currentSelectedItemPosition);
        this.currentSelectedItemPosition = currentSelectedItemPosition;
    }

    public int getLastExpandedGroup() {
        return lastExpandedGroup;
    }

    public void setLastExpandedGroup(int lastExpandedGroup) {
        this.lastExpandedGroup = lastExpandedGroup;
    }
}

class ItemListAdapter extends BaseSwipeMenuExpandableListAdapter {
    private final Context context;
    private final List<Item> items;

    public ItemListAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public boolean isGroupSwipable(int groupPosition) {
        return true;
    }

    @Override
    public boolean isChildSwipable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public ContentViewWrapper getGroupViewAndReUsable(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        boolean reUsable = true;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(com.ahmadsiavashi.list.R.layout.list_item, parent, false);
            reUsable = false;
        }
        TextView txtTitle = (TextView) convertView.findViewById(com.ahmadsiavashi.list.R.id.txtListItemTitle);
        ImageView priority = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.imgListItemPriority);
        ImageView progress = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.imgListItemLevel);
        TextView txtDescription = (TextView) convertView.findViewById(com.ahmadsiavashi.list.R.id.txtListItemDescription);
        Item item = items.get(groupPosition);
        txtTitle.setText(item.getTitle());
        txtDescription.setText(item.getDescription());
        switch (item.getPriority()) {
            case VERY_LOW:
                priority.setImageResource(com.ahmadsiavashi.list.R.drawable.very_low_priority_icon);
                break;
            case LOW:
                priority.setImageResource(com.ahmadsiavashi.list.R.drawable.low_priority_icon);
                break;
            case NORMAL:
                priority.setImageResource(com.ahmadsiavashi.list.R.drawable.normal_priority_icon);
                break;
            case HIGH:
                priority.setImageResource(com.ahmadsiavashi.list.R.drawable.high_priority_icon);
                break;
            case CRITICAL:
                priority.setImageResource(com.ahmadsiavashi.list.R.drawable.critical_priority_icon);
                break;
        }
        txtTitle.setPaintFlags(txtTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        switch (item.getLevel()) {
            case Q1:
                progress.setImageLevel(0);
                break;
            case Q2:
                progress.setImageLevel(1);
                break;
            case Q3:
                progress.setImageLevel(2);
                break;
            case Q4:
                progress.setImageLevel(3);
                txtTitle.setPaintFlags(txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                break;
        }
        if (MainActivity.getCurrentInstance().isPersianLanguage) {
            txtTitle.setTypeface(MainActivity.getCurrentInstance().persianTypeface);
            txtDescription.setTypeface(MainActivity.getCurrentInstance().persianTypeface);
        }
        return new ContentViewWrapper(convertView, reUsable);
    }

    @Override
    public ContentViewWrapper getChildViewAndReUsable(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        boolean reUsable = true;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(com.ahmadsiavashi.list.R.layout.list_item_options, null);
            reUsable = false;
            ImageView btnItemListDone = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.btnListItemDone);
            btnItemListDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                    int pos = dayViewFragment.getCurrentSelectedItemPosition();
                    Item item = (Item) dayViewFragment.getItemListAdapter().getGroup(pos);
                    if (item.setLevelToComplete())
                        dayViewFragment.updateAndNotify(item, true);
                }
            });

            ImageView btnItemListLevelIncrease = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.btnListItemLevelIncrease);
            btnItemListLevelIncrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                    int pos = dayViewFragment.getCurrentSelectedItemPosition();
                    Item item = (Item) dayViewFragment.getItemListAdapter().getGroup(pos);
                    if (item.toHigherLevel())
                        dayViewFragment.updateAndNotify(item, true);
                }
            });

            ImageView btnItemListLevelDecrease = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.btnListItemLevelDecrease);
            btnItemListLevelDecrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                    int pos = dayViewFragment.getCurrentSelectedItemPosition();
                    Item item = (Item) dayViewFragment.getItemListAdapter().getGroup(pos);
                    if (item.toLowerLevel())
                        dayViewFragment.updateAndNotify(item, true);
                }
            });

            ImageView btnItemListRemove = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.btnListItemRemove);
            btnItemListRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                    int pos = dayViewFragment.getCurrentSelectedItemPosition();
                    Item item = (Item) dayViewFragment.getItemListAdapter().getGroup(pos);
                    ItemDeleteAlertDialog itemDeleteAlertDialog = new ItemDeleteAlertDialog(MainActivity.getCurrentInstance());
                    itemDeleteAlertDialog.setDayViewFragmentAndItem(dayViewFragment, item);
                    itemDeleteAlertDialog.show();
                }
            });

            ImageView btnItemListEdit = (ImageView) convertView.findViewById(com.ahmadsiavashi.list.R.id.btnListItemEdit);
            btnItemListEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final DayViewFragment dayViewFragment = MainActivity.getCurrentInstance().getCurrentDayViewFragment();
                        int pos = dayViewFragment.getCurrentSelectedItemPosition();
                        final Item item = (Item) dayViewFragment.getItemListAdapter().getGroup(pos);
                        LayoutInflater infalInflater = (LayoutInflater) MainActivity.getCurrentInstance()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View promptsView = infalInflater.inflate(com.ahmadsiavashi.list.R.layout.edit_list_item, null);
                        final Spinner userSpinnerInput = (Spinner) promptsView.findViewById(com.ahmadsiavashi.list.R.id.spinnerEditItemPriority);
                        userSpinnerInput.setAdapter(
                                new SpinnerAdapter(
                                        MainActivity.getCurrentInstance(),
                                        new String[]
                                                {MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_very_low), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_low), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_normal), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_high), MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.priority_critical)
                                                }
                                )
                        );
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                MainActivity.getCurrentInstance());
                        alertDialogBuilder.setView(promptsView);
                        final EditText userTitleInput = (EditText) promptsView
                                .findViewById(com.ahmadsiavashi.list.R.id.txtEditItemTitle);
                        final EditText userDescInput = (EditText) promptsView
                                .findViewById(com.ahmadsiavashi.list.R.id.txtEditItemDescription);
                        userTitleInput.setText(item.getTitle());
                        userDescInput.setText(item.getDescription());
                        userSpinnerInput.setSelection(item.getPriority().ordinal());
                        alertDialogBuilder
                                .setCancelable(true)
                                .setPositiveButton(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.done),
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                item.setTitle(userTitleInput.getText().toString());
                                                item.setDescription(userDescInput.getText().toString());
                                                if (userSpinnerInput.getSelectedItemPosition() != item.getPriority().ordinal()) {
                                                    MainActivity.getCurrentInstance().getCurrentDayViewFragment().getSwipeMenuExpandableListView().collapseGroup(MainActivity.getCurrentInstance().getCurrentDayViewFragment().getCurrentSelectedItemPosition());
                                                    MainActivity.getCurrentInstance().getCurrentDayViewFragment().setCurrentSelectedItemPosition(DayViewFragment.INVALID_POSITION);
                                                }
                                                item.setPriority(userSpinnerInput.getSelectedItemPosition());
                                                dayViewFragment.updateAndNotify(item, true);
                                            }
                                        })
                                .setNegativeButton(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.cancel), null);

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } catch (Exception e) {
                        Log.e("EditItemDialog", e.getMessage());
                    }
                }
            });
        }
        return new ContentViewWrapper(convertView, reUsable);
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

class ItemDeleteAlertDialog extends AlertDialog.Builder implements AlertDialog.OnClickListener {
    DayViewFragment dayViewFragment;
    Item item;

    public ItemDeleteAlertDialog(Context context) {
        super(context);
    }

    public void setDayViewFragmentAndItem(DayViewFragment dayViewFragment, Item item) {
        this.dayViewFragment = dayViewFragment;
        this.item = item;
    }

    @NonNull
    @Override
    public AlertDialog create() {
        this.setMessage(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.remove_message));
        this.setCancelable(true);
        this.setTitle(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.app_name));
        this.setPositiveButton(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.yes), this);
        this.setNegativeButton(MainActivity.getCurrentInstance().getString(com.ahmadsiavashi.list.R.string.no), null);
        return super.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dayViewFragment.removeAndNotify(item);
    }
}

