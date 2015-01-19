package com.njackson.activityrecognition;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.njackson.Constants;
import com.njackson.application.PebbleBikeApplication;
import com.njackson.events.PebbleService.NewMessage;
import com.njackson.events.status.ActivityRecognitionStatus;
import com.njackson.events.ActivityRecognitionService.NewActivityEvent;
import com.njackson.gps.GPSService;
import com.njackson.oruxmaps.IOruxMaps;
import com.njackson.utils.googleplay.IGooglePlayServices;
import com.njackson.utils.services.IServiceStarter;
import com.njackson.utils.time.ITimer;
import com.njackson.utils.time.ITimerHandler;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by njackson on 01/01/15.
 */
public class ActivityRecognitionService  extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ITimerHandler {

    private static final String TAG = "PB-ActivityRecognitionService";
    @Inject Bus _bus;
    @Inject IGooglePlayServices _googlePlay;
    @Inject @Named("GoogleActivity") GoogleApiClient _recognitionClient;
    @Inject IServiceStarter _serviceStarter;
    @Inject ITimer _timer;
    @Inject SharedPreferences _sharedPreferences;
    @Inject IOruxMaps _oruxMaps;

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 2;
    public static final int DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    private PendingIntent _activityRecognitionPendingIntent;

    private int _type = -1;
    private String _typeString = "";

    @Subscribe
    public void onNewActivityEvent(NewActivityEvent event) {
        boolean autoStart = _sharedPreferences.getBoolean("ACTIVITY_RECOGNITION",false);

        if (autoStart) {

            String msg = event.getActivityTypeString() + "-" + event.getActivityConfidence() + "%";
            Log.d(TAG, msg + " (old:" + _typeString + ")");

            if (event.getActivityType() != _type) {
                if (_sharedPreferences.getBoolean("PREF_DEBUG", false)) {
                    sendMessageToPebble(msg);
                }
                if (!_sharedPreferences.getString("ORUXMAPS_AUTO", "disable").equals("disable")) {
                    _oruxMaps.newWaypoint();
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                switch (event.getActivityType()) {
                    //case DetectedActivity.TILTING: // for test purpose only
                    case DetectedActivity.ON_BICYCLE:
                    case DetectedActivity.RUNNING:
                    case DetectedActivity.WALKING:
                    case DetectedActivity.ON_FOOT:
                        Log.d(TAG, "detect activity bike/run/walking/foot " + event.getActivityConfidence() + "%");
                        if (!_serviceStarter.serviceRunning(GPSService.class)) {
                            Log.d(TAG, "startLocationServices");
                            _serviceStarter.startLocationServices();
                        }
                        _timer.cancel();
                        break;

                    case DetectedActivity.UNKNOWN:
                        // do nothing
                        break;

                    default:
                        // try to stop the GPS in ACTIVITY_RECOGNITION_STILL_TIME seconds
                        if (!_timer.getActive()) {
                            _timer.setTimer(Constants.ACTIVITY_RECOGNITION_STILL_TIME, this);
                        }
                }
            }
            _type = event.getActivityType();
            _typeString = event.getActivityTypeString();
        }
    }
    private void sendMessageToPebble(String message) {
        _bus.post(new NewMessage(message));
    }
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"Started Activity Recognition Service");

        ((PebbleBikeApplication)getApplication()).inject(this);

        _bus.register(this);

        if(!checkGooglePlayServices()) {
            _bus.post(new ActivityRecognitionStatus(ActivityRecognitionStatus.State.PLAY_SERVICES_NOT_AVAILABLE));
            return;
        }

        registerRecognitionCallbacks();
        createIntentService();
        connectToGooglePlayServices();

        _bus.post(new ActivityRecognitionStatus(ActivityRecognitionStatus.State.STARTED));
    }

    private void createIntentService() {
        Intent i = new Intent(this, ActivityRecognitionIntentService.class);
        _activityRecognitionPendingIntent = PendingIntent.getService(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void connectToGooglePlayServices() {
        _recognitionClient.connect();
    }

    private void registerRecognitionCallbacks() {
        _recognitionClient.registerConnectionCallbacks(this);
        _recognitionClient.registerConnectionFailedListener(this);
    }

    @Override
    public void onDestroy (){
        Log.d(TAG,"Destroy Activity Recognition Service");

        _bus.unregister(this);
        _recognitionClient.unregisterConnectionCallbacks(this);
        _recognitionClient.unregisterConnectionFailedListener(this);

        _googlePlay.removeActivityUpdates(_recognitionClient,_activityRecognitionPendingIntent);
        _recognitionClient.disconnect();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG,"Connected to Activity Service");
        _googlePlay.requestActivityUpdates(_recognitionClient, DETECTION_INTERVAL_MILLISECONDS, _activityRecognitionPendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"Connection failed");
    }

    private boolean checkGooglePlayServices() {
        return (_googlePlay.
                isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS);
    }

    @Override
    public void handleTimeout() {
        Log.d(TAG, "handleTimeout");
        if (_serviceStarter.serviceRunning(GPSService.class)) {
            Log.d(TAG, "stopLocationServices");
            _serviceStarter.stopLocationServices();
        }

    }
}
