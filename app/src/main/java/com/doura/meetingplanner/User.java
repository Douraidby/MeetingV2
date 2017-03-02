package com.doura.meetingplanner;

import com.google.android.gms.maps.model.LatLng;
import java.sql.Blob;

/**
 * Created by doura on 2/21/2017.
 */

public class User {

    private String name;                            //pseudo
    private String group;                           //nom du groupe
    private boolean is_organizer;                   //organisateur ou non
    byte [] photo;                                  //sa photo
    LatLng lastposition;                            //sa deniere position


    public User(String n, String g, byte[] p){

        this.name = n;
        this.group = g;
        this.photo = p;
        this.is_organizer = false;

    }

}
