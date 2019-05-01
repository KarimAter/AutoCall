package com.paradisegate.fajralarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

public class CallMonitoringReceiver extends BroadcastReceiver {
    private String currentNumber;

    private TelephonyManager tm;
    private PhoneStateListener phoneListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        phoneListener = new PhoneStateListener();
        tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        // End existing call if phone is offhook
        if (tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            endExistingCall();
        }
        stopListening();
    }

    private void stopListening() {
        tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        phoneListener = null;
        tm = null;
        Log.d("CallingService", "CallMonitoringReceiver:stop listening");
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
        Log.d("CallingService", "CallMonitoringReceiver:ending existing call");
    }
}
