package com.karim.ater.fajralarm;

import java.util.ArrayList;

class ResponseClass {


    public ResponseClass() {
    }

//    ArrayList<PrayerTime> data = new ArrayList<>();
//
//
//
//    public ResponseClass(ArrayList<PrayerTime> data) {
//        this.data = data;
//    }
//
//    public ArrayList<PrayerTime> getData() {
//        return data;
//    }
//
//    public void setData(ArrayList<PrayerTime> data) {
//        this.data = data;
//    }

    PrayerTime data;

    public ResponseClass(PrayerTime data) {
        this.data = data;
    }

    public PrayerTime getData() {
        return data;
    }

    public void setData(PrayerTime data) {
        this.data = data;
    }
}
