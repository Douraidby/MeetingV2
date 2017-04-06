package com.doura.meetingplanner;


/**
 * Created by doura on 3/28/2017.
 */

class Blog {
    private String bName;
    private String bDesc;
    private String bImage;
    private String bUser;

    public Blog(){
    }

    Blog(String bName, String bDesc, String bImageUrl, String buser) {
        this.bName = bName;
        this.bDesc = bDesc;
        this.bImage = bImageUrl;
        this.bUser = buser;
    }

    public String getbName() {
        return bName;
    }

    public void setbName(String bName) {
        this.bName = bName;
    }

    public String getbDesc() {
        return bDesc;
    }

    public void setbDesc(String bDesc) {
        this.bDesc = bDesc;
    }

    public String getbImage() {
        return bImage;
    }

    public void setbImage(String bImage) {
        this.bImage = bImage;
    }

    public String getbUser() {
        return bUser;
    }

    public void setbUser(String bUser) {
        this.bUser = bUser;
    }
}
