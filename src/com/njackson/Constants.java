package com.njackson;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: njackson
 * Date: 22/05/2013
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */

public class Constants {
    static final UUID WATCH_UUID = java.util.UUID.fromString("5dd35873-3bb6-44d6-8255-0e61bc3b97f5");
    
    static final int LIVE_TRACKING_FRIENDS = 0x10;
    static final int ALTITUDE_DATA = 0x13;
    static final int STATE_CHANGED = 0x14;
    
    static final String PREFS_NAME = "PebbleBikePrefs";

    static final int PLAY_PRESS = 0x0;
    static final int STOP_PRESS = 0x1;
    static final int REFRESH_PRESS = 0x2;
    static final int CMD_BUTTON_PRESS = 0x4;
    
    static final int STATE_STOP = 0x0;
    static final int STATE_START = 0x1;

    static final int IMPERIAL = 0x0;
    static final int METRIC = 0x1;

    static final double MS_TO_KPH = 3.6;
    static final double MS_TO_MPH = 2.23693629;
    static final double M_TO_KM = 0.001;
    static final double M_TO_MILES = 0.000621371192;
    static final double M_TO_M = 1;
    static final double M_TO_FEET = 3.2808399;
}

