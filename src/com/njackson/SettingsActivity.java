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
                if (preference.getKey().equals("PREF_HRM")) {
                    final Intent intent = new Intent(getApplicationContext(), HRMScanActivity.class);
                    startActivity(intent);                    
                }
                return false;
            }
        };
        
        Preference pref;
        Preference pref3 = findPreference("pref_reset_data");
        pref3.setOnPreferenceClickListener(pref_install_click_listener);


        pref = findPreference("PREF_PRESSURE_INFO");
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
            pref.setSummary("Pressure sensor available");
        } else {
            pref.setSummary("No pressure sensor");
        }

        _setHrmSummary();

        // check to determine whether BLE is supported on the device.
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Preference pref_hrm = findPreference("PREF_HRM");
            pref_hrm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference.getKey().equals("PREF_HRM")) {
                        final Intent intent = new Intent(getApplicationContext(), HRMScanActivity.class);
                        startActivityForResult(intent, 1);
                    }
                    return false;
                }
            });
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
           String hrm_name = "";
           String hrm_address = "";
           if(resultCode == RESULT_OK) {
               hrm_name = data.getStringExtra("hrm_name");
               hrm_address = data.getStringExtra("hrm_address");
           }

           SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);
           SharedPreferences.Editor editor = settings.edit();
           editor.putString("hrm_name", hrm_name);
           editor.putString("hrm_address", hrm_address);
           editor.commit();

           // reload prefs
           MainActivity.getInstance().loadPreferences();

           _setHrmSummary();

           if (!hrm_address.equals("")) {
               if (MainActivity.getInstance().checkServiceRunning()) {
                   Toast.makeText(getApplicationContext(), "Please restart GPS to display heart rate", Toast.LENGTH_LONG).show();
               }
           }
        }
    }

    private void _setOruxMapsSummary(SharedPreferences prefs) {
        ListPreference oruxPref = (ListPreference) findPreference("ORUXMAPS_AUTO");
        CharSequence listDesc = oruxPref.getEntry();
        oruxPref.setSummary(listDesc);
    }
    private void _setCanvasSummary(SharedPreferences prefs) {
        ListPreference canvasPref = (ListPreference) findPreference("CANVAS_MODE");
        CharSequence listDesc = canvasPref.getEntry();
        canvasPref.setSummary(listDesc);
    }
    private void _setHrmSummary() {
        String summary = MainActivity.hrm_name;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            summary = getResources().getString(R.string.ble_not_supported);
        }
        if (summary.equals("")) {
            summary = "Click to choose a sensor";
        }
        Preference loginPref = findPreference("PREF_HRM");
        loginPref.setSummary(summary);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        if (key.equals("UNITS_OF_MEASURE")) {
            _setUnitsSummary(sharedPreferences);
        }        
        if (key.equals("REFRESH_INTERVAL")) {
            _setRefreshSummary(sharedPreferences);
        }
        if (key.equals("ORUXMAPS_AUTO")) {
            _setOruxMapsSummary(sharedPreferences);
        }
        if (key.equals("CANVAS_MODE")) {
            _setCanvasSummary(sharedPreferences);
        }

        MainActivity activity = MainActivity.getInstance();
        if(activity != null)
            activity.loadPreferences(sharedPreferences);
    }

}
