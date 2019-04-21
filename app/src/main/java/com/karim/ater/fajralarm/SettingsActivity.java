package com.karim.ater.fajralarm;

import android.content.Intent;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import android.os.Bundle;


import java.util.Locale;


public class SettingsActivity extends PreferenceActivity {
    boolean changeLang;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (changeLang) {
            startActivity(new Intent(this, Fagr.class).
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeLang = getIntent().getBooleanExtra("changLang", false);
        MyPreferenceFragment fragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);


            ListPreference langPreference = (ListPreference) findPreference("list_preference_1");
            langPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    setLocale(o.toString());
                    Intent refresh = new Intent(getActivity(), SettingsActivity.class);
                    refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    refresh.putExtra("changLang", true);
                    startActivity(refresh);
                    return true;
                }
            });

            ListPreference callCountPreference = (ListPreference) findPreference("list_preference_2");
            callCountPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Utils.setCallsCount(getActivity(), Integer.valueOf(o.toString()));
                    return true;
                }
            });

            ListPreference fajrMethodPreference = (ListPreference) findPreference("list_preference_3");
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
                    if (!Utils.ignoringBatteryOptimization(getActivity()))
                        Utils.startPowerSaverIntent(getActivity());
                    return true;
                }
            });
        }

        public void setLocale(String lang) {
            Locale myLocale = new Locale(lang);
            Locale.setDefault(myLocale);
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.locale = myLocale;
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        }

    }
}
