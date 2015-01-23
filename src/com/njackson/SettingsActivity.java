package com.njackson;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: njackson
 * Date: 31/07/2013
 * Time: 22:43
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String TAG = "PB-SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        OnPreferenceClickListener pref_install_click_listener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onPreferenceClick:" + preference.getKey());
                if (preference.getKey().equals("pref_install_sdk2")) {
                    install_watchface();
                }
                if (preference.getKey().equals("pref_reset_data")) {
                    MainActivity activity = MainActivity.getInstance();
                    if (activity != null) {
                        activity.ResetSavedGPSStats();
                    }
                }
                return false;
            }
        };
        
        Preference pref;
        Preference pref3 = findPreference("pref_reset_data");
        pref3.setOnPreferenceClickListener(pref_install_click_listener);

    }

}
