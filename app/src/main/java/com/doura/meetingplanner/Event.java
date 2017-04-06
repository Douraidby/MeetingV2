package com.doura.meetingplanner;

import android.text.Editable;
import android.widget.EditText;

/**
 * Created by doura on 3/23/2017.
 */

public class Event {

    private String eName;
    private String eDescription;
    private String eStartDate;
    private String eStartTime;
    private String eEndDate;
    private String eEndTime;


    public Event() {
    }

    public Event(String eName, String eDescription, String eStartDate, String eStartTime, String eEndDate, String eEndTime) {
        this.eName = eName;
        this.eDescription = eDescription;
        this.eStartDate = eStartDate;
        this.eStartTime = eStartTime;
        this.eEndDate = eEndDate;
        this.eEndTime = eEndTime;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public String geteDescription() {
        return eDescription;
    }

    public void seteDescription(String eDescription) {
        this.eDescription = eDescription;
    }

    public String geteEndDate() {
        return eEndDate;
    }

    public void seteEndDate(String eEndDate) {
        this.eEndDate = eEndDate;
    }

    public String geteStartDate() {
        return eStartDate;
    }

    public void seteStartDate(String eStartDate) {
        this.eStartDate = eStartDate;
    }

    public String geteEndTime() {
        return eEndTime;
    }

    public void seteEndTime(String eEndTime) {
        this.eEndTime = eEndTime;
    }

    public String geteStartTime() {
        return eStartTime;
    }

    public void seteStartTime(String eStartTime) {
        this.eStartTime = eStartTime;
    }
}
