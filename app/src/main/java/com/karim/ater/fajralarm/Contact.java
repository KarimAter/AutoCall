package com.karim.ater.fajralarm;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {

    private int contactId;
    private String contactName;
    private String contactNumber;
    private String contactCallTime;
    private String contactLog;


//    Contact( String contactName, String contactNumber) {
////        this.contactId = contactId;
//        this.contactName = contactName;
//        this.contactNumber = contactNumber;
//        this.contactCallTime = "";
//    }

    Contact(int contactId, String contactName, String contactNumber, String contactCallTime, String contactLog) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactCallTime = contactCallTime;
        this.contactLog = contactLog;
    }

    private Contact(Parcel in) {
        contactId = in.readInt();
        contactName = in.readString();
        contactNumber = in.readString();
        contactCallTime = in.readString();
        contactLog = in.readString();
    }

    public String getContactLog() {
        return contactLog;
    }

    public void setContactLog(String contactLog) {
        this.contactLog = contactLog;
    }

    public int getContactId() {
        return contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactCallTime() {
        return contactCallTime;
    }

    public void setContactCallTime(String contactCallTime) {
        this.contactCallTime = contactCallTime;
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

