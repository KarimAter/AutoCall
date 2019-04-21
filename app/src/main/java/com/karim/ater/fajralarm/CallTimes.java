package com.karim.ater.fajralarm;

import android.support.annotation.NonNull;

public class CallTimes {
    private String startTime, endTime, ringingDuration;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRingingDuration() {
        return ringingDuration;
    }

    public void setRingingDuration(String ringingDuration) {
        this.ringingDuration = ringingDuration;
    }


    @NonNull
    @Override
    public String toString() {
        return startTime + "--" + endTime + "--" + ringingDuration;
    }
}
