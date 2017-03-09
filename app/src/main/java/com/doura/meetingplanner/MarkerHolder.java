package com.doura.meetingplanner;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by doura on 3/1/2017.
 */

public class MarkerHolder {

    private String mName;
    private double mLat;
    private double mLong;
    private Uri mUri;

    //Constructeur par defaut
    public MarkerHolder(){
        this.mName = "";
        this.mLat = 0;
        this.mLong = 0;
        this.mUri = null;

    }
    //Constructeur
    public MarkerHolder(String n, double lt,double lg, Uri u){
        this.mName = n;
        this.mLat = lt;
        this.mLong = lg;
        this.mUri = u;

    }

    public void setLatLong(LatLng ll){
        this.mLat = ll.latitude;
        this.mLong = ll.longitude;
    }

    public void setName(String name){
        this.mName = name;
    }

    public void setUri(Uri uri){
        this.mUri = uri;
    }

    public String getmName(){
        return this.mName;
    }

    public double getmLat(){
        return this.mLat;
    }

    public double getmLong(){
        return this.mLong;

    }
    public Uri getmUri(){
        return this.mUri;
    }


}
