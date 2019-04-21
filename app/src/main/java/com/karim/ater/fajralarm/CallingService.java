package com.karim.ater.fajralarm;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CallingService extends Service implements StoppingServiceInterface {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat logFormat = new SimpleDateFormat("HH:mm:ss");
    long offhookCal, idleCal, callDuration;
    PhoneListener phoneListener = null;
    TelephonyManager tm;
    boolean initialIdleState = true;
    Calendar newCalendar;
    CallDetails contactCallDetails;
    CallTimes callTimes;
    String currentCallingNumber;
    String TAG = getClass().getName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: " + "Service is created" + dateFormat.format(Calendar.getInstance().getTime()));
        contactCallDetails = new CallDetails();
        callTimes = new CallTimes();


        tm = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        phoneListener = new PhoneListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + "Service" + startId + " is started");
        currentCallingNumber = intent.getStringExtra("currentCallingNumber");
        tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        return START_NOT_STICKY;
    }

    @Override
    public void stopCallingService(CallingService callingService) {
//        CallingService.this.stopSelf();
//        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
//        phoneListener = null;
//        tm = null;
//        Log.d(TAG, "Interface intervented.. ");

    }

    class PhoneListener extends PhoneStateListener {

        StoppingServiceInterface stoppingServiceInterface = CallingService.this;
        String offhookMsg, idleMsg, durationMsg, actionMsg, resultMsg;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
//            currentCallingNumber = Utils.getCurrentCallingNumber(getBaseContext());
            contactCallDetails.setContactName(currentCallingNumber);
            DatabaseHelper dHelperr = new DatabaseHelper(getBaseContext());

            ArrayList<CallTimes> previousCallTimes = dHelperr.getContactCallTimesLog(currentCallingNumber).getCallTimes();
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "Phone inital idle State " + initialIdleState);
                    DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
                    if (!initialIdleState) {
                        idleCal = Calendar.getInstance().getTimeInMillis();
                        idleMsg = "Contact Number " + currentCallingNumber + " idleCal Fajr at " +
                                dateFormat.format(Calendar.getInstance().getTime());
                        Log.d(TAG, idleMsg);
                        callTimes.setEndTime(logFormat.format(Calendar.getInstance().getTime()));
                        int count = databaseHelper.getCallCount(currentCallingNumber);
                        callDuration = (idleCal - offhookCal);
                        callTimes.setRingingDuration(String.valueOf(callDuration / 1000));
                        // call duration exceeded the normal time
//                        if (60000 > callDuration && callDuration > 59000) {
//                            durationMsg = "Do nothing" + "Call duration was " + String.valueOf(callDuration / 1000.00);
//                            Log.d(TAG, durationMsg);
//                        }
//
//                        // if no or late answer from the receptor
//                        else
                        if ((callDuration > 37000) & count > 0) {
                            durationMsg = "Call duration was " + String.valueOf(callDuration / 1000.00);
                            resultMsg = " Repeating Call for receptor " + currentCallingNumber + " ,Call# "
                                    + String.valueOf(Utils.getCallsCount(getBaseContext()) - count);
                            Log.d(TAG, durationMsg + "\n" + actionMsg);

                            int id = databaseHelper.getID(currentCallingNumber);

                            newCalendar = Calendar.getInstance();
                            String newCalendarString = Utils.getLastCallTime(getBaseContext());
                            // assiging the time of the scheduled call
                            try {
                                newCalendar.setTime(dateFormat.parse(newCalendarString));
                                newCalendar.add(Calendar.MINUTE, 1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Utils.setLastCallTime(getBaseContext(), dateFormat.format(newCalendar.getTime()));
                            resultMsg = resultMsg +
                                    "\n next Call is " + currentCallingNumber + " scheduled at " + logFormat.format(newCalendar.getTime());
                            Log.d(TAG, resultMsg);
                            Utils.setRecurringAlarm(getBaseContext(), currentCallingNumber, id, newCalendar);
                            databaseHelper.setCallTime(currentCallingNumber, newCalendar);
                            databaseHelper.updateCallCount(currentCallingNumber, count - 1);
                        } else {
                            if ((callDuration > 37000) & count == 0) {
                                // Call attempts are finished
                                resultMsg = "No answer from receptor: " + currentCallingNumber;
                                Log.d(TAG, resultMsg);
                            } else {
                                // User has cancelled
                                resultMsg = "Call is cancelled by receptor: " + currentCallingNumber;
                                Log.d(TAG, resultMsg);
                            }
                        }
                        //
                        previousCallTimes.add(callTimes);
                        contactCallDetails.setCallTimes(previousCallTimes);
                        contactCallDetails.setFinalStatus(resultMsg);
                        databaseHelper.updateContactLog(currentCallingNumber,
                                contactCallDetails.toString());
                        Log.d(TAG, "Ending the service.. ");
                        stopSelf();
//                        stoppingServiceInterface.stopCallingService(CallingService.this);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    initialIdleState = false;
                    offhookCal = Calendar.getInstance().getTimeInMillis();
                    offhookMsg = "Contact Number " + currentCallingNumber + " offhookCal Fajr at "
                            + dateFormat.format(Calendar.getInstance().getTime());
                    Log.d(TAG, offhookMsg);
                    callTimes.setStartTime(logFormat.format(Calendar.getInstance().getTime()));

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
        Log.d(TAG, "Interface intervented.. ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
