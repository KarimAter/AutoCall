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

    private String currentNumber;
    private String TAG = getClass().getName();
    private TelephonyManager tm;
    private PhoneListener phoneListener;

    @Override
    public void onReceive(final Context context, Intent intent) {
        currentNumber = intent.getStringExtra("Number");

        // listener to check if there is an existing call to end it
        tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        phoneListener = new PhoneListener(context);
        tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        // End existing call if phone is offhook
        if (tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK)
            endExistingCall();
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

    private void endExistingCall() {
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
                // Will be invoked automatically if initial state idle or after ending existing call
                case TelephonyManager.CALL_STATE_IDLE: {
                    prepareCalling(context);
                    Log.d("Managing call state", "onCallStateChanged: idle");
                }
                break;
                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    Log.d("Managing call state", "onCallStateChanged: offhook");
                }
                break;
            }
        }
    }

    private void prepareCalling(Context context) {
        // stop the calling service if existing
        context.stopService(new Intent(context, CallingService.class));
        Intent serviceIntent = new Intent(context, CallingService.class);
        serviceIntent.putExtra("currentCallingNumber", currentNumber);
        // start a new service with the current calling number
        context.startService(serviceIntent);
        Utils.setCurrentCallingNumber(context, currentNumber);
        makeCall(context, currentNumber);
        stopListening();
    }

    // ending the phoneListener
    private void stopListening() {
        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        phoneListener = null;
        tm = null;
    }
}
