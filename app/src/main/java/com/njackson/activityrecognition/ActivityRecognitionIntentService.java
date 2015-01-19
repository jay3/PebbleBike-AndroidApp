package com.njackson.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.njackson.application.PebbleBikeApplication;
import com.njackson.events.ActivityRecognitionService.NewActivityEvent;
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
            sendReply(result.getMostProbableActivity().getType(), result.getMostProbableActivity().getConfidence());
         }
    }

    private void sendReply(int type, int confidence) {
        _bus.post(new NewActivityEvent(type, confidence));
    }

}