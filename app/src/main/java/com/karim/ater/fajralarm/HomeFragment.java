package com.karim.ater.fajralarm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {
    private String TAG = Fagr.class.getSimpleName();
    View view = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int PICK_CONTACT = 44;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 4;
    public static final int REQUEST_CODE_MULTIPLE_PERMISSIONS = 1;
    private static final String[] MY_PERMISSIONS = {Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS};
    Button setAlarmBu, cancelAlarmBu;
    TimePicker timePicker;
    Calendar calendar, callCalendar;
    TextView fajrTimeTv;
    ArrayList<Contact> selectedContacts = new ArrayList<>();
    private AdView mAdView;
    private static final String ADMOB_APP_ID = "ca-app-pub-6836093923955433~8387322372";
    int fajrMethod;
    double longitude, latitude;
    Location location;
    //    public static final String FAJR_WEBSERVICE_LINK=
//            "http://api.aladhan.com/v1/calendarByCity?city=Cairo&country=Egypt&method=5&month=01&year=2019";
    public String FAJR_WEBSERVICE_LINK =
            "http://api.aladhan.com/v1/timingsByCity?city=Cairo&country=Egypt&method=";
    public String FAJR_WEBSERVICE_LINKK =
            "http://api.aladhan.com/v1/timingsByCity?city=Cairo&country=Egypt&method=5";

    private String tag_json_obj = "jobj_req", tag_json_arry = "jarray_req";
    private FusedLocationProviderClient mFusedLocationClient;
    Context context;


    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Utils.setTimePickerHour(getContext(), timePicker.getHour());
            Utils.setTimePickerMinute(getContext(), timePicker.getMinute());
        } else {
            Utils.setTimePickerHour(getContext(), timePicker.getCurrentHour());
            Utils.setTimePickerMinute(getContext(), timePicker.getCurrentMinute());
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            context = getContext();
            initializeViews(inflater);
            loadAd();
            String fajrPrayerTime = Utils.getFajrPrayerTime(context);
            if (!fajrPrayerTime.equals(""))
                fajrTimeTv.setText(fajrPrayerTime);
            else fajrTimeTv.setVisibility(View.INVISIBLE);


            location = getDeviceLastLocation();

            if (location == null) {
                GPSTracker gpsTracker = new GPSTracker(context);
                location = gpsTracker.getLocation(context);
            }
            if (location != null) {
                Utils.setLatitude(context, location.getLatitude());
                Utils.setLongitude(context, location.getLongitude());
            }
            makeJsonObjReq();


            setAlarmBu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean permissionsResult = Permissions.getPermissionsResult(context, 0);
                    if (permissionsResult) {
                        getTime();
                        callCalendar = calendar;
                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                        databaseHelper.resetCallCount(Utils.getCallsCount(context) - 1);
                        databaseHelper.clearLog();
                        selectedContacts = databaseHelper.loadContacts();
                        databaseHelper.setCallsTime(selectedContacts, calendar);
                        for (int i = 0; i < selectedContacts.size(); i++) {
                            Utils.setRecurringAlarm(context, selectedContacts.get(i).getContactNumber(),
                                    selectedContacts.get(i).getContactId(), callCalendar);
                            if (i != selectedContacts.size() - 1)
                                callCalendar.add(Calendar.MINUTE, 1);
                        }
                        Utils.setLastCallTime(context, dateFormat.format(callCalendar.getTime()));
                        Toast.makeText(context, context.getResources().getString(R.string.SetAlarmAction)
                                + dateFormat.format(callCalendar.getTime()), Toast.LENGTH_LONG).show();

                    } else {
                        Permissions.checkPermissions(getActivity(), Constants.MAIN_PERMISSIONS);
                    }
                }

            });

            cancelAlarmBu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    ArrayList<Contact> currentContacts = databaseHelper.loadContacts();

                    for (int i = 0; i < currentContacts.size(); i++) {

                        String callTime = currentContacts.get(i).getContactCallTime();
                        Calendar nextCallCalendar = Utils.convertStringToCalendar(callTime, dateFormat);
                        Calendar currentCalendar = Calendar.getInstance();

//                        if (nextCallCalendar.getTimeInMillis() > currentCalendar.getTimeInMillis()) {
                        Utils.stopAlarms(context, currentContacts.get(i).getContactNumber(), currentContacts.get(i).getContactId());
//                        }
                    }
                    databaseHelper.resetCallsTime();
                }
            });

        }
        return view;
    }

    private void initializeViews(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.fragment_home, null);
        mAdView = view.findViewById(R.id.adView);
        timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        int savedHour = Utils.getTimePickerHour(context);
        int savedMinute = Utils.getTimePickerMinute(context);
        if (savedHour != 0 && savedMinute != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(savedHour);
                timePicker.setMinute(savedMinute);
            } else {
                timePicker.setCurrentHour(savedHour);
                timePicker.setCurrentMinute(savedMinute);
            }
        }
        fajrTimeTv = view.findViewById(R.id.fajrTimeTv);
        setAlarmBu = view.findViewById(R.id.setAlarmBu);
        cancelAlarmBu = view.findViewById(R.id.cancelAlarmBu);
    }

    // Todo: Change add Id
    private void loadAd() {
        MobileAds.initialize(context, ADMOB_APP_ID);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void getTime() {
        int hour, minute;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }
        boolean timeDiff = compareTimeToFajr(hour, minute);
        Toast.makeText(context, String.valueOf(timeDiff), Toast.LENGTH_LONG).show();

        calendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCalendar.get(Calendar.MINUTE);
        if ((hour < currentHour) || ((hour == currentHour) && (minute < currentMinute)))
            calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Log.d("Fajr", "Waking call will start on : " + calendar.getTime().toString());

    }

    //Todo: Dont set alarms if not within 30 minutes in full version
    private boolean compareTimeToFajr(int hour, int minute) {
        Calendar calendar111 = Calendar.getInstance();
        calendar111.set(Calendar.HOUR_OF_DAY, hour);
        calendar111.set(Calendar.MINUTE, minute);
        Calendar calendar112 = Calendar.getInstance();
        String[] parts = Utils.getFajrPrayerTime(context).split(":");
        calendar112.set(Calendar.HOUR_OF_DAY, Integer.valueOf(parts[0]));
        calendar112.set(Calendar.MINUTE, Integer.valueOf(parts[1]));
        long diff = Math.abs(calendar112.getTime().getTime() - calendar111.getTime().getTime());
        return diff < 30000 * 60 || diff == 30000 * 60;

    }

    private void makeJsonObjReq() {
        /**
         * Making json object request
         */
        String fajrWebServiceLink = generateWebServiceLink();
//        String fajrWebServiceLink = " http://api.aladhan.com/v1/timings/1398332113?latitude=30.07207207207207&longitude=31.02262310111727&method=5";
//        showProgressDialog();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                fajrWebServiceLink, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Gson gson = new Gson();
                        ResponseClass currentResponse = gson.fromJson(response.toString(), ResponseClass.class);
                        String fajrPrayerTime = currentResponse.getData().getTimings().getFajr();
                        Toast.makeText(context, fajrPrayerTime, Toast.LENGTH_LONG).show();
                        Utils.setFajrPrayerTime(context, fajrPrayerTime);
                        fajrTimeTv.setText(fajrPrayerTime);
                        Log.d("TimingCalibration", "onResponse: " + response.toString());

//                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
//                hideProgressDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

        // Cancelling request
        // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
    }

    private String generateWebServiceLink() {
        StringBuilder builder = new StringBuilder();
        Float latitude = Utils.getLatitude(context);
        Float longitude = Utils.getLongitude(context);
        fajrMethod = Utils.getFajrMethod(context);

        if (longitude != 1000 & latitude != 1000) {

            builder.append(" http://api.aladhan.com/v1/timings/");
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            builder.append(ts);
            builder.append("?latitude=");
            builder.append(latitude);
            builder.append("&longitude=");
            builder.append(longitude);
            builder.append("&method=");
            builder.append(fajrMethod);
            Log.d("TimingCalibration", "generateWebServiceLink: Lat:" + latitude + " Long:" + longitude + " method:" + fajrMethod + " Stamp:" + tsLong);
            return builder.toString();
        } else return null;
    }

    private Location getDeviceLastLocation() {
        final Location[] location = {null};
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location loc) {
                // Got last known location. In some rare situations this can be null.
                if (loc != null) {
                    // Logic to handle location object
                    location[0] = loc;

                }
            }
        });
        return location[0];
    }
}