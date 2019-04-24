package com.karim.ater.fajralarm;

import android.content.Intent;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import java.util.Locale;


public class SettingsActivity extends AppCompatActivity {
    // Class for handling application settings
    private boolean changeLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.Settings);
        // checks if language has been changed
        changeLang = getIntent().getBooleanExtra("changLang", false);
        MyPreferenceFragment fragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    // start Fagr activity again if language is modified
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (changeLang) {
            startActivity(new Intent(this, Fagr.class).
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);


            ListPreference langPreference = (ListPreference) findPreference("language_preference");
            langPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    setLocale(o.toString());
                    Utils.setLocale(getActivity(), o.toString());
                    // change language and refresh settings activity
                    Intent refresh = new Intent(getActivity(), SettingsActivity.class);
                    refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    refresh.putExtra("changLang", true);
                    startActivity(refresh);
                    return true;
                }
            });

            ListPreference callCountPreference = (ListPreference) findPreference("call_count_preference");
            callCountPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Utils.setCallsCount(getActivity(), Integer.valueOf(o.toString()));
                    return true;
                }
            });

            ListPreference fajrMethodPreference = (ListPreference) findPreference("fajr_method_preference");
            fajrMethodPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Utils.setFajrMethod(getActivity(), Integer.valueOf(o.toString()));
                    return true;
                }
            });

            Preference backgroundPreference = findPreference("stopBatteryOptimization");
            backgroundPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.startPowerSaverIntent(getActivity());
                    return true;
                }
            });
        }

        // setting default locale according to selected language
        public void setLocale(String lang) {
            Locale myLocale = new Locale(lang);
            Locale.setDefault(myLocale);
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.locale = myLocale;
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        }

    }
}
