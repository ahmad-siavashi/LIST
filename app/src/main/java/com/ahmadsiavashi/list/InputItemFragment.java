package com.ahmadsiavashi.list;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Navarch on 8/22/2015.
 */
public class InputItemFragment extends Fragment {
    private View view;
    private Spinner spinner;
    private ImageView imgInputInsertItem;
    private EditText txtInputItemTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        Log.d("InputItemFragment", "onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(com.ahmadsiavashi.list.R.layout.fragment_input_item, container, false);
        setSpinner((Spinner) view.findViewById(com.ahmadsiavashi.list.R.id.spinnerPriority));
        setImgInputInsertItem((ImageView) view.findViewById(com.ahmadsiavashi.list.R.id.imgInputInsertItem));
        setTxtInputItemTitle((EditText) view.findViewById(com.ahmadsiavashi.list.R.id.txtInputItemTitle));
        // Initializing the spinner items.
        getSpinner().setAdapter(new SpinnerAdapter(getActivity().getApplicationContext(), new String[]{getString(com.ahmadsiavashi.list.R.string.priority_very_low), getString(com.ahmadsiavashi.list.R.string.priority_low), getString(com.ahmadsiavashi.list.R.string.priority_normal), getString(com.ahmadsiavashi.list.R.string.priority_high), getString(com.ahmadsiavashi.list.R.string.priority_critical)}));
        getSpinner().setSelection(Priority.getNormal().ordinal());
        // Events
        getImgInputInsertItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!StringUtils.isNullOrEmpty(getTxtInputItemTitle().getText().toString())) {
                    Item newItem = new Item(getTxtInputItemTitle().getText().toString().trim(), "", getSpinner().getSelectedItemPosition(), MainActivity.getCurrentInstance().getCurrentDayViewFragment().getDate());
                    MainActivity.getCurrentInstance().getDayPageAdapter().notifyDataSetChanged();
                    MainActivity.getCurrentInstance().getCurrentDayViewFragment().addAndNotify(newItem);
                    getTxtInputItemTitle().setText("");
                }
            }
        });

        // Persian Layout
        if (MainActivity.getCurrentInstance().isPersianLanguage) {
            getTxtInputItemTitle().setTypeface(MainActivity.getCurrentInstance().persianTypeface);
        }
        return view;
    }

    @Nullable
    @Override
    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    public ImageView getImgInputInsertItem() {
        return imgInputInsertItem;
    }

    public void setImgInputInsertItem(ImageView imgInputInsertItem) {
        this.imgInputInsertItem = imgInputInsertItem;
    }

    public EditText getTxtInputItemTitle() {
        return txtInputItemTitle;
    }

    public void setTxtInputItemTitle(EditText txtInputItemTitle) {
        this.txtInputItemTitle = txtInputItemTitle;
    }
}

class SpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public SpinnerAdapter(Context context, String[] values) {
        super(context, com.ahmadsiavashi.list.R.layout.spinner_priority, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spinner_priority,
                    parent, false);
        }
        TextView label = (TextView) convertView.findViewById(R.id.txtSpinnerPriority);
        ImageView icon = (ImageView) convertView.findViewById(R.id.imgSpinnerPriority);
        switch (position) {
            case 0:
                label.setText(values[position]);
                icon.setImageResource(R.drawable.very_low_priority_icon);
                break;
            case 1:
                label.setText(values[position]);
                icon.setImageResource(R.drawable.low_priority_icon);
                break;
            case 2:
                label.setText(values[position]);
                icon.setImageResource(R.drawable.normal_priority_icon);
                break;
            case 3:
                label.setText(values[position]);
                icon.setImageResource(R.drawable.high_priority_icon);
                break;
            case 4:
                label.setText(values[position]);
                icon.setImageResource(R.drawable.critical_priority_icon);
                break;
        }

        // Persian Layout
        if (MainActivity.getCurrentInstance().isPersianLanguage) {
            label.setTypeface(MainActivity.getCurrentInstance().persianTypeface);
        }
        return convertView;

    }
}