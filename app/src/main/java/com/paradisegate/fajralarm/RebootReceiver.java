package com.paradisegate.fajralarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import static com.paradisegate.fajralarm.Constants.dateFormat;

public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String firstAlarmTime = Utils.getFirstAlarmTime(context);
        Log.d("CallingService", "onReceive:instance:" + Calendar.getInstance());
        if (!firstAlarmTime.isEmpty() &&
                !Utils.anotherCallScheduled(context, Calendar.getInstance()) &&
                Utils.isAlarmsScheduled(context)) {
            Log.d("CallingService", "FirstAlarmTime: " + firstAlarmTime);

            //Personal Alarm
            Utils.setPersonalAlarm(context, firstAlarmTime);
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            ArrayList<Contact> selectedContacts = databaseHelper.loadContacts();
            Calendar callCalendar = Utils.convertStringToCalendar(firstAlarmTime, dateFormat);
            // Creating call alarms
            for (int i = 0; i < selectedContacts.size(); i++) {
                Utils.setRecurringAlarm(context, selectedContacts.get(i).getContactNumber(),
                        selectedContacts.get(i).getContactId(), callCalendar);
                if (i != selectedContacts.size() - 1)
                    callCalendar.add(Calendar.MINUTE, 1);
            }
            Utils.setLastCallTime(context, dateFormat.format(callCalendar.getTime()));
        }
    }
}
