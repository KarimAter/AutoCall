package com.karim.ater.fajralarm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

class Permissions {


    // request system permission dialog and saves result
    static void checkPermissions(Activity activity, String[] perms) {
        if (!hasPermissions(activity, perms)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(activity, perms, Constants.REQUEST_CODE_MULTIPLE_PERMISSIONS);
            }
        } else {
            for (int i = 0; i < perms.length; i++) {
                setPermissionsResult(activity, i, true);
            }
        }
    }

    // check whether are permissions granted or not
    private static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // saving and getting permission results in sharedPrefs

    static void setPermissionsResult(Context context, int i, boolean result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("PermissionsResult" + i, result);
        editor.apply();
    }

    static boolean getPermissionsResult(Context context, int i) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("PermissionsResult" + i, false);
    }
}
