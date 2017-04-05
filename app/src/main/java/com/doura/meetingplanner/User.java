package com.doura.meetingplanner;

import com.google.android.gms.maps.model.LatLng;
import java.sql.Blob;

/**
 * Created by doura on 2/21/2017.
 * Classe qui va contenir le profile d'un utilisateur
 */

public class User {

    private String name;                            //pseudo
    private String group;                           //nom du groupe
    private boolean organizer;                      //organisateur ou non
    private double uLat ;
    private double uLong;
    private String mImgUrl;                         //photo encodé
//    byte [] photo;


    public User(){
    }

    public User(String name, String group, boolean organizer, double uLat, double uLong, String mImgUrl) {
        this.name = name;
        this.group = group;
        this.organizer = organizer;
        this.uLat = uLat;
        this.uLong = uLong;
        this.mImgUrl = mImgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isOrganizer() {
        return organizer;
    }

    public void setOrganizer(boolean organizer) {
        this.organizer = organizer;
    }

    public double getuLat() {
        return uLat;
    }

    public void setuLat(double uLat) {
        this.uLat = uLat;
    }

    public double getuLong() {
        return uLong;
    }

    public void setuLong(double uLong) {
        this.uLong = uLong;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }
}
