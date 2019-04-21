package com.karim.ater.fajralarm;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static List<Intent> POWERMANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    );

    public static final String[] MAIN_PERMISSIONS = {Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS};
    public static final String[] ALL_PERMISSIONS = {Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7;
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 4;
    public static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 9;
    public static final int REQUEST_CODE_MULTIPLE_PERMISSIONS = 1;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat hourDateFormat = new SimpleDateFormat("HH:mm");
}
