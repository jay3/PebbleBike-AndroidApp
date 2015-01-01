package com.njackson.gps;

import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.Log;


import javax.inject.Inject;

import fr.jayps.android.AdvancedLocation;

/**
 * Created by njackson on 24/12/14.
 */
public class ServiceNmeaListener implements GpsStatus.NmeaListener {

    private static final String TAG = "PB-ServiceNmeaListener";

    @Inject SharedPreferences _sharedPreferences;
    @Inject GPSService _gpsService;

    private AdvancedLocation _advancedLocation;

    public ServiceNmeaListener(AdvancedLocation _advancedLocation) {
        this._advancedLocation = _advancedLocation;
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        if (nmea.startsWith("$GPGGA")) {
            // http://aprs.gids.nl/nmea/#gga
            //Log.d(TAG, "geoid: " + nmea);

            String[] strValues = nmea.split(",");

            try {
                // Height of geoid above WGS84 ellipsoid
                double geoid_height = Double.parseDouble(strValues[11]);

                Log.d(TAG, "nmea geoid_height: " + geoid_height);

                _advancedLocation.setGeoidHeight(geoid_height);
                SharedPreferences.Editor editor = _sharedPreferences.edit();
                editor.putFloat("GEOID_HEIGHT", (float) geoid_height);
                editor.commit();

                // no longer need Nmea updates
                // TODO(nic) FIXME!
				_gpsService.unregisterNmeaListener(); // doesn't work...
				//_locationManager.removeNmeaListener(this);
            } catch (Exception e) {
            }
        }
    }
}
