package com.ahmadsiavashi.list;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Navarch on 9/2/2015.
 */
public class HeaderDatePicker extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(DayViewFragment.DATE_KEY);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
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
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, dialog.getDatePicker().getYear());
                calendar.set(Calendar.MONTH, dialog.getDatePicker().getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, dialog.getDatePicker().getDayOfMonth());
                MainActivity.getCurrentInstance().setViewPagerInterval(calendar.getTime());
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(com.ahmadsiavashi.list.R.string.today), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEUTRAL) {
                    MainActivity.getCurrentInstance().setViewPagerInterval(new Date());
                }
            }
        });
        return dialog;
    }
}
