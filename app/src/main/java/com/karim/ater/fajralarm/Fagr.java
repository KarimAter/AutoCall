package com.karim.ater.fajralarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Fagr extends AppCompatActivity {
    Fragment fragment = null;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;
    int contactsCount;
    private static final int PICK_CONTACT = 44;

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String value = prefs.getString("list_preference_1", "en");
        Locale myLocale = new Locale(value);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        setTitle(R.string.app_name);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (!Utils.ignoringBatteryOptimization(this))
//            Utils.startPowerSaverIntent(this);
        setContentView(R.layout.activity_fagr);
//        mCoordinatorLo = findViewById(R.id.mCoordinatorLo);
        bottomNavigationView = findViewById(R.id.bottomNav);
        fab = findViewById(R.id.fab);
        fragment = new HomeFragment();
        loadFragment(fragment);


        String lastCallingTime = Utils.getLastCallTime(this);
//        int callCount = Utils.getCallsCount(this);

        resettingCallsDate(lastCallingTime);

        Permissions.checkPermissions(Fagr.this, Constants.ALL_PERMISSIONS);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean permissionResults = Permissions.getPermissionsResult(Fagr.this, 1);
                if (permissionResults) {
                    final DatabaseHelper databaseHelper = new DatabaseHelper(getBaseContext());
                    contactsCount = databaseHelper.getNumberOfContacts();
                    if (contactsCount < 10) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT);
                    } else
                        Toast.makeText(Fagr.this, getResources().getString(R.string.MaxContactsMessage),
                                Toast.LENGTH_SHORT).show();

                } else {
                    Permissions.checkPermissions(Fagr.this, Constants.MAIN_PERMISSIONS);
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);

                switch (item.getItemId()) {
                    case R.id.action_Home:
                        if (!(fragment instanceof HomeFragment))
                            fragment = new HomeFragment();
                        break;
                    case R.id.action_contacts:
                        if (!(fragment instanceof ContactsFragment))
                            fragment = new ContactsFragment();
                        break;
                    case R.id.action_logs:
                        if (!(fragment instanceof LogsFragment))
                            fragment = new LogsFragment();
                        break;
                }

                loadFragment(fragment);
                return false;
            }
        });
    }

    private void resettingCallsDate(String lastCallingTime) {
        try {
            Date lastDate = Constants.dateFormat.parse(lastCallingTime);
            if (System.currentTimeMillis() > lastDate.getTime()) {
                DatabaseHelper databaseHelper1 = new DatabaseHelper(this);
                databaseHelper1.resetCallsTime();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            if (fragment instanceof ContactsFragment)
                fab.show();
            else
                fab.hide();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
        }
    }

    private void pickingContact(Intent data) {
        Uri contactData = data.getData();
        Cursor cursor = getContentResolver().query(contactData,
                null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = '" + name + "'";
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                Cursor numberCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection, selection, null, null);
                if (numberCursor != null) {
                    numberCursor.moveToFirst();
                    final ArrayList<String> contactNumbers = new ArrayList<>();
                    while (!numberCursor.isAfterLast()) {
                        contactNumbers.add(numberCursor.getString(0));
                        numberCursor.moveToNext();
                    }
                    if (contactNumbers.size() == 1) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(Fagr.this);
                        boolean check = databaseHelper.addContact(name, contactNumbers.get(0));
                        if (!check)
                            Toast.makeText(getBaseContext(), "Number already exists", Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder dialog = Utils.showDialog(this);
                        dialog.setSingleChoiceItems(contactNumbers.toArray(new CharSequence[contactNumbers.size()]),
                                0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int n) {
                                        Log.d("Dialog", "onClick: " + contactNumbers.get(n));
                                        DatabaseHelper databaseHelper = new DatabaseHelper(Fagr.this);
                                        boolean check = databaseHelper.addContact(name, contactNumbers.get(n));
                                        if (!check)
                                            Toast.makeText(getBaseContext(), getResources().getString(R.string.ExistingNumberMessage),
                                                    Toast.LENGTH_LONG).show();
                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                        ft.replace(R.id.frame, new ContactsFragment());
                                        ft.commit();
                                        d.dismiss();

                                    }
                                });
                        dialog.show();

                    }
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frame, new ContactsFragment());
                    ft.commit();
                    numberCursor.close();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            pickingContact(data);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (permissions.length == 0) {
            return;
        }
        boolean allPermissionsGranted = true;
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
        }
        if (!allPermissionsGranted) {
            boolean somePermissionsForeverDenied = false;
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //denied
                    Log.e("denied", permission);
                    for (int i = 0, len = Constants.ALL_PERMISSIONS.length; i < len; i++) {
                        Permissions.setPermissionsResult(Fagr.this, i, false);
                    }

                } else {
                    if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        //allowed
                        Log.e("allowed", permission);
                    } else {
                        //set to never ask again
                        Log.e("set to never ask again", permission);
                        somePermissionsForeverDenied = true;
                    }
                }
            }
            if (somePermissionsForeverDenied) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Permissions Required")
                        .setMessage("You have forcefully denied some of the required permissions " +
                                "for this action. Please open settings, go to permissions and allow them.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0, len = Constants.ALL_PERMISSIONS.length; i < len; i++) {
                                    Permissions.setPermissionsResult(Fagr.this, i, false);
                                }
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        } else {
            for (int i = 0, len = permissions.length; i < len; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                    Permissions.setPermissionsResult(Fagr.this, i, false);
                } else
                    Permissions.setPermissionsResult(Fagr.this, i, true);

            }

            if (Permissions.getPermissionsResult(this, 0) &&
                    Permissions.getPermissionsResult(this, 1) &&
                    !Utils.ignoringBatteryOptimization(this)) {
                Utils.startPowerSaverIntent(this);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}