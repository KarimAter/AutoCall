package com.karim.ater.fajralarm;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    // Contact class
    private int contactId;
    private String contactName;
    private String contactNumber;
    private String contactCallTime;
    private String contactLog;

    // Constructor
    Contact(int contactId, String contactName, String contactNumber, String contactCallTime, String contactLog) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactCallTime = contactCallTime;
        this.contactLog = contactLog;
    }

    // Getters and Setters
    String getContactLog() {
        return contactLog;
    }

    int getContactId() {
        return contactId;
    }

    String getContactName() {
        return contactName;
    }

    String getContactNumber() {
        return contactNumber;
    }

    String getContactCallTime() {
        return contactCallTime;
    }

    // Making class parcelable

    private Contact(Parcel in) {
        contactId = in.readInt();
        contactName = in.readString();
        contactNumber = in.readString();
        contactCallTime = in.readString();
        contactLog = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(contactId);
        dest.writeString(contactName);
        dest.writeString(contactNumber);
        dest.writeString(contactCallTime);
        dest.writeString(contactLog);
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

}

