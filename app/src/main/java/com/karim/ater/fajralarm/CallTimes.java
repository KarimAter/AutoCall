package com.karim.ater.fajralarm;

import android.support.annotation.NonNull;

public class CallTimes {
    // Start and end times and duration for each call

    private String startTime, endTime, ringingDuration;

    String getStartTime() {
        return startTime;
    }

    void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    String getEndTime() {
        return endTime;
    }

    void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    String getRingingDuration() {
        return ringingDuration;
    }

    void setRingingDuration(String ringingDuration) {
        this.ringingDuration = ringingDuration;
    }


    @NonNull
    @Override
    public String toString() {
        return startTime + "--" + endTime + "--" + ringingDuration;
    }
}
