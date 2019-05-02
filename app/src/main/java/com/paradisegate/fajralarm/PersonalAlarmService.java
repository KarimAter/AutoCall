package com.paradisegate.fajralarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class PersonalAlarmService extends Service {
    Ringtone r;
    Vibrator v;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Uri notification;
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (notification == null)
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (notification == null)
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (notification != null) {
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            if (r != null)
                r.play();
        }
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            long[] vibrationPattern = new long[]{0, 500, 1500};

            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int[] amp = new int[]{VibrationEffect.DEFAULT_AMPLITUDE, VibrationEffect.DEFAULT_AMPLITUDE
                        , VibrationEffect.DEFAULT_AMPLITUDE};
                v.vibrate(VibrationEffect.createWaveform(vibrationPattern, amp, 0));
            } else {
                //deprecated in API 26
                v.vibrate(vibrationPattern, 0);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (r != null) {
            r.stop();
        }
        if (v != null) {
            v.cancel();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
