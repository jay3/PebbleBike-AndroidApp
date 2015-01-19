package com.njackson.events.ActivityRecognitionService;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by server on 21/03/2014.
 */
public class NewActivityEvent {

    private int _activityType;
    private int _activityConfidence;
    public int getActivityType() { return _activityType; }
    public int getActivityConfidence() { return _activityConfidence; }
    public String getActivityTypeString() {
        switch (_activityType) {
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            default:
                return "def" + _activityType;
        }
    }

    public NewActivityEvent(int activityType, int confidence) {
        _activityType = activityType;
        _activityConfidence = confidence;
    }

}
