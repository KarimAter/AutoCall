package com.karim.ater.fajralarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactsDB.db";
    private static final String CONTACTS_TABLE = "Contacts";
    private static final String CONTACT_NAME = "ContactName";
    private static final String CONTACT_NUMBER = "ContactNumber";
    private static final String CONTACT_CALL_TIME = "ContactCallTime";
    private static final String ID = "ID";
    private static final String CONTACT_CALL_COUNT = "ContactCallCount";
    private static final String CONTACT_LOG = "ContactLog";

    // Constructor
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACT_TABLE = "CREATE Table " + CONTACTS_TABLE + "(" + ID + " INTEGER, " + CONTACT_NAME +
                " TEXT," + CONTACT_NUMBER + " TEXT," + CONTACT_CALL_TIME + " TEXT," + CONTACT_CALL_COUNT + " INTEGER," +
                CONTACT_LOG + " TEXT)";
        db.execSQL(CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    // Adding contact from fab
    boolean addContact(String contactName, String contactNumber) {
        String query = "Select * FROM " + CONTACTS_TABLE + " WHERE " + CONTACT_NUMBER + "= '" + contactNumber + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(ID, getNumberOfContacts() + 1);
            values.put(CONTACT_NAME, contactName);
            values.put(CONTACT_NUMBER, contactNumber);
            values.put(CONTACT_CALL_TIME, "");
            values.put(CONTACT_LOG, "");
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            sqLiteDatabase.insert(CONTACTS_TABLE, null, values);
            cursor.close();
            sqLiteDatabase.close();
            return true;
        }
    }

    // load all added contacts to the app
    ArrayList<Contact> loadContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        String query = "Select * FROM " + CONTACTS_TABLE + " Order by " + ID + " ASC ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            contacts.add(new Contact(cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3), cursor.getString(5)));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return contacts;
    }

    // Get remaining number of calls for specific contact
    int getCallCount(String number) {
        int callCount;
        String query = "Select " + CONTACT_CALL_COUNT + " FROM " + CONTACTS_TABLE + " WHERE " + CONTACT_NUMBER + "= '" + number + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        callCount = cursor.getInt(0);
        cursor.close();
        db.close();
        return callCount;
    }

    // Update the remaining number of calls for specific contact
    void updateCallCount(String number, int newCount) {
        String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_CALL_COUNT + " = " + newCount +
                " WHERE " + CONTACT_NUMBER + " = '" + number + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    // Reset number of calls to default value for all contacts
    void resetCallCount(int countValue) {
        String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_CALL_COUNT + " = " + countValue;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    // Resets the calling time for all contacts
    void resetCallsTime() {
        String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_CALL_TIME + " = ''";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    // Setting calling times for all contacts when alarm is set
    void setCallsTime(ArrayList<Contact> selectedContacts, Calendar calendar) {
        Calendar timerCalendar = Calendar.getInstance();
        timerCalendar.setTime(calendar.getTime());
        SQLiteDatabase db = this.getWritableDatabase();
        for (Contact contact : selectedContacts) {
            String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_CALL_TIME + " = '"
                    + Constants.timeFormat.format(timerCalendar.getTime()) +
                    "' WHERE " + CONTACT_NUMBER + " = '" + contact.getContactNumber() + "'";
            db.execSQL(query);
            timerCalendar.add(Calendar.MINUTE, 1);
        }
        db.close();
    }

    // Setting calling time for specific contact
    void setCallTime(String number, Calendar calendar) {
        Calendar timerCalendar = Calendar.getInstance();
        timerCalendar.setTime(calendar.getTime());
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_CALL_TIME + " = '"
                + Constants.timeFormat.format(timerCalendar.getTime()) +
                "' WHERE " + CONTACT_NUMBER + " = '" + number + "'";
        db.execSQL(query);
        timerCalendar.add(Calendar.MINUTE, 1);
        db.close();
    }

    int getNumberOfContacts() {
        int count = 0;
        String query = "Select count(" + ID + ") FROM " + CONTACTS_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // Getting contact Id
    int getID(String contactNumber) {
        int id = 0;
        String query = "Select " + ID + " FROM " + CONTACTS_TABLE + " WHERE " + CONTACT_NUMBER + " = '" + contactNumber + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
            cursor.close();
            Log.d("Databaser", "getID: " + id);
        }
        return id;
    }

    // Reorder contacts
    void reorder(ArrayList<Contact> contactsList) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Delete From " + CONTACTS_TABLE;
        db.execSQL(query);
        for (Contact contact : contactsList) {
            addContact(contact.getContactName(), contact.getContactNumber());
        }
    }

    // Delete contact
    void deleteContact(String contactName, String contactNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CONTACTS_TABLE, CONTACT_NAME + "=? AND " + CONTACT_NUMBER + "=?", new String[]{contactName, contactNumber});
        db.close();
    }

    // updating contact call log after each call
    void updateContactLog(String number, String log) {
        String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_LOG + " = '" + log +
                "' WHERE " + CONTACT_NUMBER + " = '" + number + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    // get current contact call log details
    CallLogDetails getContactCallTimesLog(String number) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select " + CONTACT_LOG + " FROM " + CONTACTS_TABLE +
                " WHERE " + CONTACT_NUMBER + "= '" + number + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        String databaseLog = cursor.getString(0);
        cursor.close();
        db.close();
        return Utils.extractCallDetail(databaseLog);
    }

    // checks whether is there are logs for all contacts
    boolean isLogExisting() {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean emptyLogs = true;
        String query = "Select " + CONTACT_LOG + " FROM " + CONTACTS_TABLE;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String s = cursor.getString(0);
            emptyLogs = emptyLogs && s.isEmpty();
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return emptyLogs;
    }

    // clear logs from all contacts
    void clearLog() {
        String query = "Update " + CONTACTS_TABLE + " SET " + CONTACT_LOG + " = " + "''";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();

    }
}
