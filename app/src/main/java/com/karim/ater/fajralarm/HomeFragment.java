package com.karim.ater.fajralarm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

import static com.karim.ater.fajralarm.Constants.dateFormat;
import static com.karim.ater.fajralarm.Constants.timeFormat;

public class HomeFragment extends Fragment {
    private String TAG = Fagr.class.getSimpleName();
    private View view = null;
    private Button setAlarmBu, cancelAlarmBu;
    private TimePicker timePicker;
    private Calendar calendar, callCalendar;
    private TextView fajrTimeTv;
    private ArrayList<Contact> selectedContacts = new ArrayList<>();
    private AdView mAdView;
    private Context context;
    private Location lastLocation;
    private FragmentActivity activity;

    @Override
    public void onPause() {
        super.onPause();
        // Saving the selected time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Utils.setTimePickerHour(getContext(), timePicker.getHour());
            Utils.setTimePickerMinute(getContext(), timePicker.getMinute());
        } else {
            Utils.setTimePickerHour(getContext(), timePicker.getCurrentHour());
            Utils.setTimePickerMinute(getContext(), timePicker.getCurrentMinute());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Permissions.getPermissionsResult(context, 2)) {
            gettingLocation();
            makeJsonObjReq();
            setFajrTimeTv();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            initializeViews(view);
            loadAd();
            setAlarmBu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check the calling permission
                    boolean permissionsResult = Permissions.getPermissionsResult(context, 0);
                    if (permissionsResult) {
                        // Todo: unhash
//                        if (timeDifference) {
                        getTime();
                        String firstAlarmTime = Utils.convertCalendarToString(calendar, dateFormat);
                        callCalendar = calendar;
                        // resetting call counts to default value and clearing old logs
                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                        databaseHelper.resetCallCount(Utils.getCallsCount(context) - 1);
                        databaseHelper.clearLog();

                        // adjusting contact calling times in database
                        selectedContacts = databaseHelper.loadContacts();
                        databaseHelper.setCallsTime(selectedContacts, calendar);

                        // Creating call alarms
                        for (int i = 0; i < selectedContacts.size(); i++) {
                            Utils.setRecurringAlarm(context, selectedContacts.get(i).getContactNumber(),
                                    selectedContacts.get(i).getContactId(), callCalendar);
                            if (i != selectedContacts.size() - 1)
                                callCalendar.add(Calendar.MINUTE, 1);
                        }
                        Utils.setLastCallTime(context, dateFormat.format(callCalendar.getTime()));

                        // showing snack bar with Undo option
                        Snackbar snackbar = Snackbar
                                .make(activity.findViewById(R.id.mCoordinatorLo),
                                        getString(R.string.SetAlarmAction) + firstAlarmTime, Snackbar.LENGTH_LONG);
                        snackbar.setAction(getString(R.string.UndoAction), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // undo is selected, restore the deleted item
                                CancellingAlarms();
                            }
                        });
                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();
//                        Toast.makeText(context, context.getResources().getString(R.string.SetAlarmAction)
//                                + dateFormat.format(calendar.getTime()), Toast.LENGTH_LONG).show();
//                        }
//                        else Toast.makeText(context, R.string.AlarmTimingMessage,
//                                Toast.LENGTH_LONG).show();
                    } else {
                        // Request permissions
                        Permissions.checkPermissions(getActivity(), Constants.MAIN_PERMISSIONS);
                    }
                }

            });

            cancelAlarmBu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Todo:Snack here and up
                    // Stopping the alarms
                    CancellingAlarms();
                }
            });

        }
        return view;
    }

    private void CancellingAlarms() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        ArrayList<Contact> currentContacts = databaseHelper.loadContacts();
        for (int i = 0; i < currentContacts.size(); i++) {
            Utils.stopAlarms(context, currentContacts.get(i).getContactNumber(), currentContacts.get(i).getContactId());
        }
        databaseHelper.resetCallsTime();
    }

    private void gettingLocation() {
        // get location by last saved location method
//        Location location = getDeviceLastLocation();
        // get location by gps location
//        if (location == null) {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        Location location = gpsTracker.getLocation(getActivity());
//        }
        // Saving location
        if (location != null) {
            Utils.setLatitude(context, location.getLatitude());
            Utils.setLongitude(context, location.getLongitude());
        }
    }

    // Request fajr time from API
    private void makeJsonObjReq() {
        String fajrWebServiceLink = generateWebServiceLink();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                fajrWebServiceLink, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        ResponseClass currentResponse = gson.fromJson(response.toString(), ResponseClass.class);
                        String fajrPrayerTime = currentResponse.getData().getTimings().getFajr();
//                        Toast.makeText(context, fajrPrayerTime, Toast.LENGTH_LONG).show();
                        Utils.setFajrPrayerTime(context, fajrPrayerTime);
                        fajrTimeTv.setText(fajrPrayerTime);
                        fajrTimeTv.setVisibility(View.VISIBLE);
                        Log.d("TimingCalibration", "onResponse: " + response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        // Adding request to request queue
        String tag_json_obj = "jobj_req";
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    // Generating the link based on location
    private String generateWebServiceLink() {
        StringBuilder builder = new StringBuilder();
        Float latitude = Utils.getLatitude(context);
        Float longitude = Utils.getLongitude(context);
        int fajrMethod = Utils.getFajrMethod(context);

        // if location not null
        if (longitude != 1000 & latitude != 1000) {

            builder.append(" http://api.aladhan.com/v1/timings/");
            long tsLong = System.currentTimeMillis() / 1000;
            String ts = Long.toString(tsLong);
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

    // Setting the Fajr time in the textView
    private void setFajrTimeTv() {
        String fajrPrayerTime = Utils.getFajrPrayerTime(context);
        if (!fajrPrayerTime.isEmpty())
            fajrTimeTv.setText(fajrPrayerTime);
    }

    private void initializeViews(View view) {
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
        MobileAds.initialize(context, Constants.ADMOB_APP_ID);
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
        boolean timeDifference = compareTimeToFajr(hour, minute);
        Toast.makeText(context, String.valueOf(timeDifference), Toast.LENGTH_SHORT).show();

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
        Calendar selectedAlarmTime = Calendar.getInstance();
        selectedAlarmTime.set(Calendar.HOUR_OF_DAY, hour);
        selectedAlarmTime.set(Calendar.MINUTE, minute);
        Calendar savedFajrTime = Calendar.getInstance();
        String fajrPrayerTime = Utils.getFajrPrayerTime(context);
        if (fajrPrayerTime.isEmpty()) {
            Calendar midnightCal = Utils.convertStringToCalendar("00:00:00", timeFormat);
            Calendar sunriseCal = Utils.convertStringToCalendar("06:00:00", timeFormat);
            return (selectedAlarmTime.after(midnightCal) && selectedAlarmTime.before(sunriseCal));
        }
        String[] parts = fajrPrayerTime.split(":");
        savedFajrTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(parts[0]));
        savedFajrTime.set(Calendar.MINUTE, Integer.valueOf(parts[1]));
        long diff = Math.abs(savedFajrTime.getTime().getTime() - selectedAlarmTime.getTime().getTime());
        return diff < 30000 * 60 || diff == 30000 * 60;

    }

    // Getting last known location
    private Location getDeviceLastLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
                    lastLocation = loc;
                }
            }
        });
        return lastLocation;
    }
}