package com.njackson.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.njackson.Constants;
import com.njackson.R;
import com.njackson.application.PebbleBikeApplication;
import com.njackson.events.ActivityRecognitionService.NewActivityEvent;
import com.njackson.events.PebbleService.NewMessage;
import com.njackson.oruxmaps.IOruxMaps;
import com.njackson.oruxmaps.OruxMaps;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: server
 * Date: 19/05/2013
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
public class ActivityRecognitionIntentService extends IntentService {

    private static final String TAG = "PB-ActivityRecognitionIntentService";
    @Inject Bus _bus;
    @Inject SharedPreferences _sharedPreferences;
    @Inject IOruxMaps _oruxMaps;
    String _type = "";

    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
        _type = "";
    }

    public ActivityRecognitionIntentService(String name) {
        super(name);
        _type = "";
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((PebbleBikeApplication)getApplication()).inject(this);

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            String type = "";
            switch(result.getMostProbableActivity().getType()) {

                case DetectedActivity.ON_BICYCLE:
                    Log.d(TAG, "ON_BICYCLE");
                    type = "ON_BICYCLE";
                    sendReply(result.getMostProbableActivity().getType());
                    break;
                case DetectedActivity.WALKING:
                    Log.d(TAG, "WALKING");
                    type = "WALKING";
                    sendReply(result.getMostProbableActivity().getType());
                    break;
                case DetectedActivity.RUNNING:
                    Log.d(TAG, "RUNNING");
                    type = "RUNNING";
                    sendReply(result.getMostProbableActivity().getType());
                    break;
                case DetectedActivity.TILTING:
                    Log.d(TAG, "TILTING");
                    type = "TILTING";
                    break;
                case DetectedActivity.STILL:
                    Log.d(TAG, "STILL");
                    type = "STILL";
                    sendReply(result.getMostProbableActivity().getType());
                case DetectedActivity.IN_VEHICLE:
                    Log.d(TAG, "IN_VEHICLE");
                    type = "IN_VEHICLE";
                    break;
                case DetectedActivity.ON_FOOT:
                    Log.d(TAG, "ON_FOOT");
                    type = "ON_FOOT";
                    break;
                case DetectedActivity.UNKNOWN:
                    Log.d(TAG, "UNKNOWN");
                    //type = "UNKNOWN";
                    break;
                default:
                    type = "def" + result.getMostProbableActivity().getType();
            }
            if (!type.equals(_type) && !type.equals("")) {
                _type = type;
                if (_sharedPreferences.getBoolean("PREF_DEBUG", false)) {
                    sendMessageToPebble(type);
                }
                if (!_sharedPreferences.getString("ORUXMAPS_AUTO", "disable").equals("disable")) {
                    _oruxMaps.newWaypoint();
                }
                Toast.makeText(this, type, Toast.LENGTH_SHORT).show();
            }
         }
    }

    private void sendReply(int type) {
        _bus.post(new NewActivityEvent(type));
    }
    private void sendMessageToPebble(String message) {

        _bus.post(new NewMessage(message));
    }

}