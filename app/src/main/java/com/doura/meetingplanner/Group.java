package com.doura.meetingplanner;

/**
 * Created by doura on 2/22/2017.
 */

public class Group {

    private String group_name;                      //nom du groupe
    private User user;

    public void Group(String group){
        this.group_name = group;
    }

    public String getGroup_name(){
        return group_name;

    }
}
