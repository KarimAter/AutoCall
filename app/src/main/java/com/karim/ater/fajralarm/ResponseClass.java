package com.karim.ater.fajralarm;

import java.util.ArrayList;

class ResponseClass {
    // WebService POJO class

    private PrayerTime data;

    public ResponseClass(PrayerTime data) {
        this.data = data;
    }

    PrayerTime getData() {
        return data;
    }

}
