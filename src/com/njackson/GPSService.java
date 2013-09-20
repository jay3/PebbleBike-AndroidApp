package com.njackson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import fr.jayps.android.AdvancedLocation;

/**
 * Created with IntelliJ IDEA.
 * User: njackson
 * Date: 23/05/2013
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class GPSService extends Service {
	
	private static final String TAG = "PB-GPSService";

    private int _updates;
    private float _speed;
    private float _averageSpeed;
    private float _distance;

    private float _prevspeed = -1;
    private float _prevaverageSpeed = -1;
    private float _prevdistance = -1;
    private double _prevaltitude = -1;
    private long _prevtime = -1;
    private double _currentLat;
    private double _currentLon;
    double xpos = 0;
    double ypos = 0;
    double prevXPos = 0;
    double prevYPos = 0;
    Location firstLocation = null;
    private AdvancedLocation _myLocation;
    private LiveTracking _liveTracking;

    private static GPSService _this;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        _this = this;
        
        makeServiceForeground("Pebble Bike", "GPS started");
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    @Override
    public void onCreate() {
        _locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        super.onCreate();
    }

    private boolean checkGPSEnabled(LocationManager locationMgr) {

        if(!locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
           return false;
        } else {
            return true;
        }

    }

    @Override
    public void onDestroy (){
        Log.d(TAG, "Stopped GPS Service");
        
        saveGPSStats();

        _this = null;
        
        removeServiceForeground();
        
        //PebbleKit.closeAppOnPebble(getApplicationContext(), Constants.WATCH_UUID);

        _locationMgr.removeUpdates(onLocationChange);
        

        if ((getApplication().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
	        // remove MockLocationProvider.mocLocationProvider from the location manager
	        try {
	            _locationMgr.removeTestProvider(MockLocationProvider.mocLocationProvider);
	            Log.d(TAG, "removeTestProvider " + MockLocationProvider.mocLocationProvider + " OK");
	        }
	        catch (Exception e) {
	        	Log.d(TAG, "Exception:"+e.getMessage());
	        }
        }
       
    }

    // load the saved state
    public void loadGPSStats() {
    	Log.d(TAG, "loadGPSStats()");
    	
    	SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);
        _speed = settings.getFloat("GPS_SPEED",0.0f);
        _distance = settings.getFloat("GPS_DISTANCE",0.0f);
        _myLocation.setDistance(_distance);
        _myLocation.setElapsedTime(settings.getLong("GPS_ELAPSEDTIME", 0));
        
        try {
            _myLocation.setAscent(settings.getFloat("GPS_ASCENT", 0.0f));
        } catch (ClassCastException e) {
            _myLocation.setAscent(0.0);
        }
        try {
            _updates = settings.getInt("GPS_UPDATES",0);
        } catch (ClassCastException e) {
            _updates = 0;
        }        
    }

    // save the state
    public void saveGPSStats() {
    	Log.d(TAG, "saveGPSStats()");
    	
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("GPS_SPEED", _speed);
        editor.putFloat("GPS_DISTANCE",_distance);
        editor.putLong("GPS_ELAPSEDTIME", _myLocation.getElapsedTime());
        editor.putFloat("GPS_ASCENT", (float) _myLocation.getAscent());
        editor.putInt("GPS_UPDATES", _updates);
        editor.commit();
    }

    // reset the saved state
    public static void resetGPSStats(SharedPreferences settings) {
    	Log.d(TAG, "resetGPSStats()");
    	
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putFloat("GPS_SPEED", 0.0f);
	    editor.putFloat("GPS_DISTANCE",0.0f);
	    editor.putLong("GPS_ELAPSEDTIME", 0);
	    editor.putFloat("GPS_ASCENT", 0.0f);
	    editor.putInt("GPS_UPDATES", 0);
	    editor.commit();
	    
	    if (_this != null) {
	    	// GPS is running
		    // reninit all properties
	    	_this._myLocation = new AdvancedLocation(_this.getApplicationContext());
	    	_this._myLocation.debugLevel = 2;
	    	_this._myLocation.debugTagPrefix = "PB-";

	    	_this.loadGPSStats();  	    	
	    }
    }

    private void handleCommand(Intent intent) {
        Log.d(TAG, "Started GPS Service");
        
        _liveTracking = new LiveTracking(getApplicationContext());

        _myLocation = new AdvancedLocation(getApplicationContext());
        _myLocation.debugLevel = 2;
        _myLocation.debugTagPrefix = "PB-";

        loadGPSStats();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        	 
        _liveTracking.setLogin(prefs.getString("LIVE_TRACKING_LOGIN", ""));
        _liveTracking.setPassword(prefs.getString("LIVE_TRACKING_PASSWORD", ""));
        _liveTracking.setUrl(prefs.getString("LIVE_TRACKING_URL", ""));

        // check to see if GPS is enabled
        if(checkGPSEnabled(_locationMgr)) {
            

        	if ((getApplication().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
	        	// try to remove MockLocationProvider.mocLocationProvider from the location manager if already here
	            try {
	                _locationMgr.removeTestProvider(MockLocationProvider.mocLocationProvider);
	                Log.e(TAG, "removeTestProvider " + MockLocationProvider.mocLocationProvider + " OK");
	            }
	            catch (Exception e) {}            
	            
	            _locationMgr.addTestProvider(MockLocationProvider.mocLocationProvider, false, false,
	                    false, false, true, true, true, 0, 5);
	            _locationMgr.setTestProviderEnabled(MockLocationProvider.mocLocationProvider, true);
	            _locationMgr.requestLocationUpdates(MockLocationProvider.mocLocationProvider, 0, 0, onLocationChange);
	
	            try {
	
	                List<String> data = new ArrayList<String>();
	                InputStream is = getAssets().open("data.txt");
	                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	                String line = null;
	                while ((line = reader.readLine()) != null) {
	                	//Log.v(TAG, line);
	                    data.add(line);
	                }
	                Log.e(TAG, data.size() + " lines");
	                new MockLocationProvider(_locationMgr, data).start();
	
	            } catch (IOException e) {
	            	Log.e(TAG, e.getMessage());
	            }
        	} else {
        		_locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, onLocationChange);
        	}
            
            // send the saved values directly to update pebble
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.GPSServiceReceiver.ACTION_RESP);
            broadcastIntent.putExtra("DISTANCE", _myLocation.getDistance());
            broadcastIntent.putExtra("AVGSPEED", _myLocation.getAverageSpeed());
            broadcastIntent.putExtra("ASCENT",   _myLocation.getAscent());
            sendBroadcast(broadcastIntent);
        }else {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.GPSServiceReceiver.ACTION_GPS_DISABLED);
            sendBroadcast(broadcastIntent);
            return;
        }

        //PebbleKit.startAppOnPebble(getApplicationContext(), Constants.WATCH_UUID);
    }

    public class MockLocationProvider extends Thread {

    	private List<String> data;

    	private LocationManager locationManager;

    	public static final String mocLocationProvider = "GpsMockProvider";

    	public MockLocationProvider(LocationManager locationManager,
    	        List<String> data) throws IOException {

    	    this.locationManager = locationManager;
    	    //this.mocLocationProvider = mocLocationProvider;
    	    this.data = data;
    	}

    	@Override
    	public void run() {

    	    for (String str : data) {
    	        try {
    	            Thread.sleep(1000);
    	        } catch (InterruptedException e) {
    	            e.printStackTrace();
    	        }

    	        // Set one position
    	        String[] parts = str.split(",");
    	        Long time = Long.valueOf(parts[0]);
    	        Double latitude = Double.valueOf(parts[1]);
    	        Double longitude = Double.valueOf(parts[2]);
    	        Double altitude = Double.valueOf(parts[3]);
    	        Float accuracy = Float.valueOf(parts[4]);
    	        
    	        Location location = new Location(mocLocationProvider);
    	        location.setLatitude(latitude);
    	        location.setLongitude(longitude);
    	        location.setAltitude(altitude);
    	        location.setAccuracy(accuracy);
    	        location.setTime(1000*time);
    	        location.setSpeed(10 * (float) Math.random());

    	        if (parts.length > 5) {
                    String comment = parts[5];
                    Log.e(TAG, location.toString() + " " + comment);  
                }

    	        // set the time in the location. If the time on this location
    	        // matches the time on the one in the previous set call, it will be
    	        // ignored
    	        //location.setTime(System.currentTimeMillis());

    	        locationManager.setTestProviderLocation(mocLocationProvider,
    	                location);
    	    }
    	}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    private LocationManager _locationMgr = null;
    private LocationListener onLocationChange = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            int resultOnLocationChanged = _myLocation.onLocationChanged(location);
            
            //Log.d(TAG,  "onLocationChanged: " + _myLocation.getTime() + " Accuracy: " + _myLocation.getAccuracy());

            _speed = _myLocation.getSpeed();

            if(_speed < 1) {
                _speed = 0;
            } else {
                _updates++;
            }

            _averageSpeed = _myLocation.getAverageSpeed();
            _distance = _myLocation.getDistance();

            _currentLat = location.getLatitude();
            _currentLon = location.getLongitude();
            
            if (firstLocation == null) {
                firstLocation = location;
            }

            xpos = firstLocation.distanceTo(location) * Math.sin(firstLocation.bearingTo(location)/180*3.1415);
            ypos = firstLocation.distanceTo(location) * Math.cos(firstLocation.bearingTo(location)/180*3.1415); 

            xpos = Math.floor(xpos/10);
            ypos = Math.floor(ypos/10);
            Log.d(TAG,  "xpos="+xpos+"-ypos="+ypos);

            boolean send = false;
            //if(_myLocation.getAccuracy() < 15.0) // not really needed, something similar is done in AdvancedLocation
            if (_speed != _prevspeed || _averageSpeed != _prevaverageSpeed || _distance != _prevdistance || _prevaltitude != _myLocation.getAltitude()) {

                send = true;

                _prevaverageSpeed = _averageSpeed;
                _prevdistance = _distance;
                _prevspeed = _speed;
                _prevaltitude = _myLocation.getAltitude();
                _prevtime = _myLocation.getTime();
            } else if (_prevtime + 5000 < _myLocation.getTime()) {
                Log.d(TAG,  "New GPS data without move");
                
                send = true;
                
                _prevtime = _myLocation.getTime();
            }
            if (send) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.GPSServiceReceiver.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("SPEED", _speed);
                broadcastIntent.putExtra("DISTANCE", _distance);
                broadcastIntent.putExtra("AVGSPEED", _averageSpeed);
                broadcastIntent.putExtra("LAT",_currentLat );
                broadcastIntent.putExtra("LON",_currentLon );
                broadcastIntent.putExtra("ALTITUDE",   _myLocation.getAltitude()); // m
                broadcastIntent.putExtra("ASCENT",     _myLocation.getAscent()); // m
                broadcastIntent.putExtra("ASCENTRATE", (3600f * _myLocation.getAscentRate())); // in m/h
                broadcastIntent.putExtra("SLOPE",      (100f * _myLocation.getSlope())); // in %
                broadcastIntent.putExtra("ACCURACY",   _myLocation.getAccuracy()); // m
                broadcastIntent.putExtra("TIME",_myLocation.getElapsedTime());
                
                //if (xpos != prevXPos || ypos != prevYPos) {
                //if (Math.sqrt((xpos-prevXPos)*(xpos-prevXPos) + (ypos-prevYPos)*(ypos-prevYPos)) > 10) {
                    broadcastIntent.putExtra("XPOS", xpos);
                    broadcastIntent.putExtra("YPOS", ypos);
                    prevXPos = xpos;
                    prevYPos = ypos;
                //}
                broadcastIntent.putExtra("BEARING", _myLocation.getBearing());
                sendBroadcast(broadcastIntent);
            }

            if (MainActivity._liveTracking && resultOnLocationChanged == AdvancedLocation.SAVED) {
	            if (_liveTracking.addPoint(location)) {
	            	String friends = _liveTracking.getFriends(); 
	            	if (friends != "") {
	            		Toast.makeText(getApplicationContext(), friends, Toast.LENGTH_LONG).show();

		                PebbleDictionary dic = new PebbleDictionary();
		                
		                dic.addString(Constants.LIVE_TRACKING_FRIENDS, friends);
		                PebbleKit.sendDataToPebble(getApplicationContext(), Constants.WATCH_UUID, dic);
	            	}
	            }
            }
            
        }

        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub
            
        }        
    };

    private void makeServiceForeground(String titre, String texte) {
        //http://stackoverflow.com/questions/3687200/implement-startforeground-method-in-android
        final int myID = 1000;

        //The intent to launch when the user clicks the expanded notification
        Intent i = new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, i, 0);

        // The following code is deprecated since API 11 (Android 3.x). Notification.Builder could be used instead, but without Android 2.x compatibility 
        Notification notification = new Notification(R.drawable.ic_launcher, "Pebble Bike", System.currentTimeMillis());
        notification.setLatestEventInfo(this, titre, texte, pendIntent);

        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(myID, notification);
    }
    private void removeServiceForeground() {
        stopForeground(true);
    }
}
