package com.karim.ater.fajralarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.karim.ater.fajralarm.Constants.dateFormat;
import static com.karim.ater.fajralarm.Constants.timeFormat;

public class CallingService extends Service {

    private long offhookCal;
    private PhoneListener phoneListener = null;
    private TelephonyManager tm;
    private boolean initialIdleState = true;
    private CallLogDetails contactCallLogDetails;
    private CallTimes callTimes;
    private String currentCallingNumber;
    private String TAG = getClass().getName();
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: " + "Service is created" + dateFormat.format(Calendar.getInstance().getTime()));
        contactCallLogDetails = new CallLogDetails();
        callTimes = new CallTimes();
        tm = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        phoneListener = new PhoneListener();
        context = getBaseContext();
        Utils.updateLocale(context);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + "Service" + startId + " is started");
        currentCallingNumber = intent.getStringExtra("currentCallingNumber");
        tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        return START_NOT_STICKY;
    }


    class PhoneListener extends PhoneStateListener {
        private String offhookMsg, idleMsg, durationMsg, actionMsg, resultMsg;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            DatabaseHelper dHelperr = new DatabaseHelper(context);
            String contactName = dHelperr.getContactName(currentCallingNumber);
            ArrayList<CallTimes> previousCallTimes = dHelperr.getContactCallTimesLog(currentCallingNumber).getCallTimes();

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "Phone inital idle State " + initialIdleState);
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    if (!initialIdleState) {
                        long idleCal = Calendar.getInstance().getTimeInMillis();
                        idleMsg = "Contact Number " + currentCallingNumber + " idleCal Fajr at " +
                                dateFormat.format(Calendar.getInstance().getTime());
                        Log.d(TAG, idleMsg);
                        String endTime = timeFormat.format(Calendar.getInstance().getTime());

                        callTimes.setEndTime(Utils.arTranslate(context, endTime));
                        int count = databaseHelper.getCallCount(currentCallingNumber);
                        long callDuration = (idleCal - offhookCal);
                        callTimes.setRingingDuration(Utils.arTranslate(context, String.valueOf(callDuration / 1000)));

                        // if no or late answer from the receptor

                        if ((callDuration > 37000) & count > 0) {
                            durationMsg = "Call duration was " + String.valueOf(callDuration / 1000.00);
                            resultMsg = " Repeating Call for receptor " + currentCallingNumber + " ,Call# "
                                    + String.valueOf(Utils.getCallsCount(context) - count);
                            Log.d(TAG, durationMsg + "\n" + actionMsg);

                            int id = databaseHelper.getID(currentCallingNumber);

                            Calendar newCalendar = Calendar.getInstance();
                            String newCalendarString = Utils.getLastCallTime(context);
                            // assigning the time of the scheduled call
                            try {
                                newCalendar.setTime(dateFormat.parse(newCalendarString));
                                newCalendar.add(Calendar.MINUTE, 1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Utils.setLastCallTime(context, dateFormat.format(newCalendar.getTime()));
                            resultMsg = getString(R.string.schedulingMessage) + " " +
                                    Utils.arTranslate(context, timeFormat.format(newCalendar.getTime()));
                            Log.d(TAG, resultMsg);
                            // scheduling the next call and updating time & count in database
                            Utils.setRecurringAlarm(context, currentCallingNumber, id, newCalendar);
                            databaseHelper.setCallTime(currentCallingNumber, newCalendar);
                            databaseHelper.updateCallCount(currentCallingNumber, count - 1);
                        } else {
                            if ((callDuration > 37000) & count == 0) {
                                // Call attempts are finished
                                resultMsg = getString(R.string.noAnswerMessage) + " " + contactName;
                                Log.d(TAG, resultMsg);
                            } else {
                                // User has cancelled
                                resultMsg = getApplicationContext().getString(R.string.cancelMessage) + " " + contactName;
                                Log.d(TAG, resultMsg);
                            }
                        }
                        // updating logs
                        previousCallTimes.add(callTimes);
                        contactCallLogDetails.setCallTimes(previousCallTimes);
                        contactCallLogDetails.setFinalStatus(resultMsg);
                        databaseHelper.updateContactLog(currentCallingNumber,
                                contactCallLogDetails.toString());
                        Log.d(TAG, "Ending the service.. ");
                        stopSelf();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    initialIdleState = false;
                    offhookCal = Calendar.getInstance().getTimeInMillis();
                    offhookMsg = "Contact Number " + currentCallingNumber + " offhookCal Fajr at "
                            + dateFormat.format(Calendar.getInstance().getTime());
                    Log.d(TAG, offhookMsg);
                    String startTime = timeFormat.format(Calendar.getInstance().getTime());
                    callTimes.setStartTime(Utils.arTranslate(context, startTime));
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        phoneListener = null;
        tm = null;
        Log.d(TAG, "OnDestroy: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
