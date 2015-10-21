package com.example.readLogSDroid;

import android.graphics.Bitmap;
public class item {
    Bitmap image;
   // String title;
    String titleTime;
    String titleDistr;
    String titleMsg;

    public item(Bitmap image, String titleTime,String titleDistr,String titleMsg) {
        super();
        this.image = image;
        this.titleTime= titleTime;
        this.titleDistr=titleDistr;
        this.titleMsg=titleMsg;

    }
    public Bitmap getImage() {
        return image;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public String getTitleTime() {
        return titleTime;
    }
    public String getTitleDistr() {
        return titleDistr;
    }
    public String getTitleMsg() {
        return titleMsg;
    }
    public void setTitle(String titleTime,String titleDistr,String titleMsg) {
        this.titleTime = titleTime;
        this.titleDistr = titleDistr;
        this.titleMsg = titleMsg;
    }


}