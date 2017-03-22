package com.doura.meetingplanner;

import android.net.Uri;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by doura on 3/1/2017.
 * Classe qui va contenir les infos d'un marqueur de place de meeting ou le profile d'un utilisateur
 */

public class MarkerHolder {

    private String mId;
    private String mName;
    private String mGroup;
    private double mLat;
    private double mLong;
    private String mUri;
    private String mImgUrl;
    private String mVote;
    private Map<String,String> mVotes;

    //Constructeur blanc pour firebase
    public MarkerHolder(){
    }

    //Constructeur
    public MarkerHolder(String n, String g, double lt,double lg, String u, String v){
        this.mName = n;
        this.mGroup = g;
        this.mLat = lt;
        this.mLong = lg;
        this.mUri = u;
        this.mVote =v;
        this.mImgUrl = null;
    }

    public void setLatLong(LatLng ll){
        this.mLat = ll.latitude;
        this.mLong = ll.longitude;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmGroup() {
        return mGroup;
    }

    public void setmGroup(String mGroup) {
        this.mGroup = mGroup;
    }

    public double getmLat() {
        return mLat;
    }

    public void setmLat(double mLat) {
        this.mLat = mLat;
    }

    public double getmLong() {
        return mLong;
    }

    public String getmId() {
        return mId;
    }

    public Map<String, String> getmVotes() {
        return mVotes;
    }

    public void setmVotes(Map<String, String> mVotes) {
        this.mVotes = mVotes;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public void setmLong(double mLong) {
        this.mLong = mLong;
    }

    public String getmUri() {
        return mUri;
    }

    public void setmUri(String mUri) {
        this.mUri = mUri;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmVote() {
        return mVote;
    }

    public void setmVote(String mVote) {
        this.mVote = mVote;
    }
}