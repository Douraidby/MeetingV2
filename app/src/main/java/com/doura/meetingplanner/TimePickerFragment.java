package com.doura.meetingplanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by doura on 3/23/2017.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MONTH);
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    /**
     * Interface to communicate to the parent activity (MainActivity.java)
     */
    private FragmentCallbacks mCallbacks;

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCallbacks.setTextTime(hourOfDay, minute);
    }

    public interface FragmentCallbacks {
        void setTextTime(int hourOfDay, int minute);
    }

    @Override
    public void onAttach(Context activity) { // Attach it to the activity
        super.onAttach(activity);
        try {
            mCallbacks = (FragmentCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement Fragment One.");
        }
    }

    @Override
    public void onDetach() { // Remove the listener
        super.onDetach();
        mCallbacks = null;
    }

}