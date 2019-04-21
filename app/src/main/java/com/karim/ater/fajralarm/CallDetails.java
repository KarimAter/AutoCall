package com.karim.ater.fajralarm;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CallDetails {
    private String contactName, finalStatus;
    ArrayList<CallTimes> callTimes;

    public CallDetails() {
        callTimes = new ArrayList<>();
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public ArrayList<CallTimes> getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(ArrayList<CallTimes> callTimes) {
        this.callTimes = callTimes;
    }

    @NonNull
    @Override
    public String toString() {
        return contactName + callTimes.toString() + finalStatus;
    }


}
