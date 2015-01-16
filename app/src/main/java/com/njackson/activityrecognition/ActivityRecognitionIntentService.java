package com.njackson.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.njackson.Constants;
import com.njackson.R;
import com.njackson.application.PebbleBikeApplication;
import com.njackson.events.ActivityRecognitionService.NewActivityEvent;
import com.njackson.events.PebbleService.NewMessage;
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

    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
    }

    public ActivityRecognitionIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((PebbleBikeApplication)getApplication()).inject(this);

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            String message = "";
            switch(result.getMostProbableActivity().getType()) {

                case DetectedActivity.ON_BICYCLE:
                    Log.d(TAG, "ON_BICYCLE");
                    message = "ON_BICYCLE";
                    sendReply(result.getMostProbableActivity().getType());
                    break;
                case DetectedActivity.WALKING:
                    Log.d(TAG, "WALKING");
                    message = "WALKING";
                    sendReply(result.getMostProbableActivity().getType());
                    break;
                case DetectedActivity.RUNNING:
                    Log.d(TAG, "RUNNING");
                    message = "RUNNING";
                    sendReply(result.getMostProbableActivity().getType());
                    break;
                case DetectedActivity.TILTING:
                    Log.d(TAG, "TILTING");
                    message = "TILTING";
                    break;
                case DetectedActivity.STILL:
                    Log.d(TAG, "STILL");
                    message = "STILL";

                    sendReply(result.getMostProbableActivity().getType());
            }
            if (_sharedPreferences.getBoolean("DEBUG", false)) {
                sendMessageToPebble(message);
            }
            if (!_sharedPreferences.getString("ORUXMAPS_AUTO", "disable").equals("disable")) {
                OruxMaps.newWaypoint(getApplicationContext());
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