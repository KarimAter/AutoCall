package com.karim.ater.fajralarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


class Utils {
    // Class of utilities methods

    // setting calling alarm for broadcast receiver
    static void setRecurringAlarm(Context context, String number, int contactId, Calendar callTime) {
        int requestCode = contactId * 100;
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("Number", number);

        Log.d("StopAlarm", "SetRecAlrm " + requestCode + " " + number);
        PendingIntent callAlarmPIntent = PendingIntent.getBroadcast(context,
                requestCode, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, callTime.getTimeInMillis(),
                        callAlarmPIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, callTime.getTimeInMillis(),
                        callAlarmPIntent);
            }
            Log.d("Fajr", "setRecurringAlarm: setExactAndAllowWhileIdle is fired on " + number
                    + callTime.getTime().toString());
        }
    }

    // stopping calling alarm
    static void stopAlarms(Context context, String number, int contactId) {
        int requestCode = contactId * 100;
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("Number", number);
        Log.d("StopAlarm", "StopRecAlrm " + requestCode + " " + number);
        PendingIntent stopAlarmPIntent = PendingIntent.getBroadcast(context,
                requestCode, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(stopAlarmPIntent);
        }
    }


    // Battery optimization methods

    // check if app is ignoring battery optimization or not
    static boolean ignoringBatteryOptimization(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean status = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (pm != null) {
                status = pm.isIgnoringBatteryOptimizations("com.karim.ater.fajralarm");
            }
            Toast.makeText(context, "Ignoring battery optimization: " + String.valueOf(status), Toast.LENGTH_LONG).show();
        }
        return status;
    }

    // go to setting depending on phone type
    static void startPowerSaverIntent(final Context context) {


        for (final Intent intent : Constants.POWERMANAGER_INTENTS) {
            if (isCallable(context, intent)) {
                Log.d("Power", "startPowerSaverIntent: selection list");
                new AlertDialog.Builder(context)
                        .setTitle(Build.MANUFACTURER + " Protected Apps")
                        .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", context.getString(R.string.app_name)))
                        .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            }
        }

    }

    // check whether there is setting intent found from various phone intents
    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    //    static void startPowerSaverIntent(final Context context) {
//        SharedPreferences settings = context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
//        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
//        if (!skipMessage) {
//            final SharedPreferences.Editor editor = settings.edit();
//            boolean foundCorrectIntent = false;
//            for (final Intent intent : Constants.POWERMANAGER_INTENTS) {
//                if (isCallable(context, intent)) {
//                    foundCorrectIntent = true;
//                    final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
//                    dontShowAgain.setText("Do not show again");
//                    dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                            editor.putBoolean("skipProtectedAppCheck", isChecked);
//                            editor.apply();
//                        }
//                    });
//                    Log.d("Power", "startPowerSaverIntent: selection list");
//                    new AlertDialog.Builder(context)
//                            .setTitle(Build.MANUFACTURER + " Protected Apps")
//                            .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", context.getString(R.string.app_name)))
//                            .setView(dontShowAgain)
//                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    context.startActivity(intent);
//                                }
//                            })
//                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    editor.putBoolean("skipProtectedAppCheck", false);
//                                    editor.apply();
//                                }
//                            })
//                            .setCancelable(false)
//                            .show();
//                    break;
//                }
//            }
//            if (!foundCorrectIntent) {
//                editor.putBoolean("skipProtectedAppCheck", true);
//                editor.apply();
//            }
//        }
//    }

    // convert contactLog string to CallLogDetails class
    static CallLogDetails extractCallDetail(String contactLog) {
        CallLogDetails callLogDetails = new CallLogDetails();
        if (contactLog.isEmpty()) {
            callLogDetails.setContactName("");
            callLogDetails.setFinalStatus("");
            return callLogDetails;
        } else {
            String contactName = contactLog.substring(0, contactLog.indexOf("["));
            String finalStatus = contactLog.substring(contactLog.indexOf("]") + 1);
            ArrayList<CallTimes> callTimesArrayList = new ArrayList<>();
            String[] allCallsTimes;
            contactLog = contactLog.substring(contactLog.indexOf("[") + 1);
            contactLog = contactLog.substring(0, contactLog.indexOf("]"));
            allCallsTimes = contactLog.split(",");

            for (String singleCallTimes : allCallsTimes) {
                CallTimes callTimes = new CallTimes();
                String parts[] = singleCallTimes.split("--");
                callTimes.setStartTime(parts[0].trim());
                callTimes.setEndTime(parts[1].trim());
                callTimes.setRingingDuration(parts[2].trim());
                callTimesArrayList.add(callTimes);
            }

            callLogDetails.setCallTimes(callTimesArrayList);
            callLogDetails.setContactName(contactName);
            callLogDetails.setFinalStatus(finalStatus);

            return callLogDetails;
        }
    }

    // multiple number selector for contact dialog
    static AlertDialog.Builder showDialog(Context context, String title) {
        final AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setNegativeButton("Cancel", null);
        adb.setTitle(title);
        return adb;
    }

    // Calendar conversion methods

    public static String convertCalendarToString(Calendar calendar, SimpleDateFormat dateFormat) {
        return dateFormat.format(calendar.getTime());
    }

    static Calendar convertStringToCalendar(String calendarString, SimpleDateFormat dateFormat) {
        Calendar calendar = null;
        try {
            calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(calendarString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    // SharedPreferences getters and setters

    static void setCallsCount(Context context, int callsCount) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("CALL_COUNT", callsCount);
        editor.apply();
    }

    static int getCallsCount(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("CALL_COUNT", 4);
    }

    static void setFajrPrayerTime(Context context, String fajrPrayerTime) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("FajrPrayerTime", fajrPrayerTime);
        editor.apply();
    }

    static String getFajrPrayerTime(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("FajrPrayerTime", "");
    }

    static void setFajrMethod(Context context, int fajrMethod) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("FAJR_METHOD", fajrMethod);
        editor.apply();
    }

    static int getFajrMethod(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("FAJR_METHOD", 5);
    }

    static void setTimePickerHour(Context context, int hour) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("HOUR", hour);
        editor.apply();
    }

    static void setTimePickerMinute(Context context, int minute) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("MINUTE", minute);
        editor.apply();
    }

    static int getTimePickerHour(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("HOUR", 0);
    }

    static int getTimePickerMinute(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("MINUTE", 0);
    }

    static void setLastCallTime(Context context, String lastCallTime) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Last Call Time", lastCallTime);
        editor.apply();
    }

    static String getLastCallTime(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("Last Call Time", "");
    }

    static void setCurrentCallingNumber(Context context, String currentNumber) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("CurrentCallingNumber", currentNumber);
        editor.apply();
    }

    static String getCurrentCallingNumber(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("CurrentCallingNumber", "");
    }

    static void setLatitude(Context context, double latitude) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("Latitude", (float) latitude);
        editor.apply();
    }

    static void setLongitude(Context context, double longitude) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("Longitude", (float) longitude);
        editor.apply();
    }

    static Float getLatitude(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat("Latitude", 1000f);
    }

    static Float getLongitude(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat("Longitude", 1000f);
    }
}

