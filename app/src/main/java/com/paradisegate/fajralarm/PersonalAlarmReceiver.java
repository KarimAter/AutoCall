package com.paradisegate.fajralarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PersonalAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startActivity(new Intent(context, PersonalAlarmActivity.class).
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
