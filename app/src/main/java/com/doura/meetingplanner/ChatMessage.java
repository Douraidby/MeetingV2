package com.doura.meetingplanner;

import java.util.Date;

/**
 * Created by doura on 4/12/2017.
 */

public class ChatMessage {

    private String cUser;
    private String cMessage;
    private long cTime;

    public ChatMessage(){
    }

    public ChatMessage(String cUser, String cMessage) {
        this.cUser = cUser;
        this.cMessage = cMessage;
        this.cTime = new Date().getTime();
    }

    public String getcUser() { return cUser; }

    public void setcUser(String cUser) {
        this.cUser = cUser;
    }

    public String getcMessage() {
        return cMessage;
    }

    public void setcMessage(String cMessage) {
        this.cMessage = cMessage;
    }

    public long getcTime() {
        return cTime;
    }

    public void setcTime(long cTime) {
        this.cTime = cTime;
    }
}
