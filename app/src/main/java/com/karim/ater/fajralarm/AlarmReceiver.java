package com.karim.ater.fajralarm;


import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AlarmReceiver extends BroadcastReceiver {

    String currentNumber;
    String TAG = getClass().getName();
    TelephonyManager tm;
    PhoneListener phoneListener;
    boolean offHook;

    @Override
    public void onReceive(final Context context, Intent intent) {
        currentNumber = intent.getStringExtra("Number");
        // listener to check if there is an existing call to end it

        tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        phoneListener = new PhoneListener(context);
        tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


        int x = tm.getCallState();
        if (x == TelephonyManager.CALL_STATE_OFFHOOK)
            endExistingCall(context);
//        else if (x == TelephonyManager.CALL_STATE_IDLE)
//            callingStuff(context);
    }

    // ending the phoneListener
    private void stopListening() {
        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        phoneListener = null;
        tm = null;
    }

    // calling the calling app
    private void makeCall(Context context, final String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        callIntent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        context.startActivity(callIntent);
    }

    private void endExistingCall(Context context) {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
            c = Class.forName(telephonyService.getClass().getName()); // Get its class
            m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
            m.setAccessible(true); // Make it accessible
            m.invoke(telephonyService); // invoke endCall()
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Managing call state", "ending existing call");
    }

    class PhoneListener extends PhoneStateListener {
        Context context;

        PhoneListener(Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: {
                    callingStuff(context);
                    Log.d("Managing call state", "onCallStateChanged: idle");
                }
                break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    offHook = true;
                    break;
            }
        }
    }

    private void callingStuff(Context context) {
        Log.d(TAG, "Phone inital idle State ");
        offHook = false;
        // stop the calling service if existing
        context.stopService(new Intent(context, CallingService.class));

        Log.d("Fajr", "onReceive: " + "alarm is received for number " + currentNumber);
        Intent serviceIntent = new Intent(context, CallingService.class);
        serviceIntent.putExtra("currentCallingNumber", currentNumber);
        // start a new service with the current calling number
        context.startService(serviceIntent);

        Utils.setCurrentCallingNumber(context, currentNumber);
        makeCall(context, currentNumber);
        stopListening();
    }
}
