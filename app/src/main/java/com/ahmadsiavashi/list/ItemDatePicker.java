package com.ahmadsiavashi.list;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Navarch on 9/2/2015.
 */
public class ItemDatePicker extends DialogFragment {
    Item item;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Item.DATE_PATTERN, Locale.US);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.item = (Item) getArguments().getSerializable("Item");
        try {
            calendar.setTime(simpleDateFormat.parse(this.item.getDate()));
        } catch (Exception e) {
            Log.e("ItemDatePicker", e.getMessage());
        }
        final DatePickerDialog dialog = new DatePickerDialog(getActivity(), null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(com.ahmadsiavashi.list.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE)
                    dialog.cancel();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(com.ahmadsiavashi.list.R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                calendar.set(Calendar.YEAR, dialog.getDatePicker().getYear());
                calendar.set(Calendar.MONTH, dialog.getDatePicker().getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, dialog.getDatePicker().getDayOfMonth());
                String newDate = simpleDateFormat.format(calendar.getTime());
                if (!newDate.equalsIgnoreCase(item.getDate())) {
                    MainActivity.getCurrentInstance().getCurrentDayViewFragment().changeItemDateAndNotify(item, newDate);
                    MainActivity.getCurrentInstance().getDayPageAdapter().notifyDataSetChanged(newDate);
                }
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(com.ahmadsiavashi.list.R.string.today), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                if (which == DialogInterface.BUTTON_NEUTRAL) {
                    Date today = new Date();
                    String newDate = simpleDateFormat.format(today);
                    if (!newDate.equalsIgnoreCase(item.getDate())) {
                        MainActivity.getCurrentInstance().getCurrentDayViewFragment().changeItemDateAndNotify(item, newDate);
                        MainActivity.getCurrentInstance().getDayPageAdapter().notifyDataSetChanged(newDate);
                    }
                }
            }
        });
        return dialog;
    }
}