package com.karim.ater.fajralarm;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CallLogDetails {
    // Log details for each contact
    private String contactName, finalStatus;
    private ArrayList<CallTimes> callTimes;

    CallLogDetails() {
        callTimes = new ArrayList<>();
    }

    public String getContactName() {
        return contactName;
    }

    void setContactName(String contactName) {
        this.contactName = contactName;
    }

    String getFinalStatus() {
        return finalStatus;
    }

    void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    ArrayList<CallTimes> getCallTimes() {
        return callTimes;
    }

    void setCallTimes(ArrayList<CallTimes> callTimes) {
        this.callTimes = callTimes;
    }

    @NonNull
    @Override
    public String toString() {
        return contactName + callTimes.toString() + finalStatus;
    }


}
