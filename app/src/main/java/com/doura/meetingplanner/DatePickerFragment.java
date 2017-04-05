package com.doura.meetingplanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by doura on 3/23/2017.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        mCallbacks.setTextDate(year, month, day);
    }

    /**
     * Interface to communicate to the parent activity (MainActivity.java)
     */
    private FragmentCallbacks mCallbacks;

    public interface FragmentCallbacks {
        void setTextDate(int year, int month, int day);
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