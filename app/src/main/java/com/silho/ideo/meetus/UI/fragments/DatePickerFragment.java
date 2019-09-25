package com.silho.ideo.meetus.UI.fragments;



import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.silho.ideo.meetus.R;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

/**
 * Created by Samuel on 18/08/2017.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener  {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it

        return new DatePickerDialog(getActivity(), R.style.DialogTheme, this,  year, month, day);
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {

        Intent intent = new Intent();
        intent .putExtra("selectedYear", year);
        intent.putExtra("selectedMonth", month);
        intent.putExtra("selectedDay", day );
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
