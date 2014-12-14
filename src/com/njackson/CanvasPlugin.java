package com.njackson;


import java.util.ArrayList;
import java.util.Arrays;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.pennas.pebblecanvas.plugin.PebbleCanvasPlugin;

public class CanvasPlugin extends PebbleCanvasPlugin {
	private static final String TAG = "PB-CanvasPlugin";
	
	public static final int ID_CANVAS_PB = 1;
	
	private static final String[] MASKS = { "%DIST", "%ALT", "%AVG", "%ASCE", "%BEAR", "%TIME", "%SPD", "%LAT", "%LON", "%ASCR", "%SLOP", "%ACCU", "%HRM"};
	private static final int MASK_DISTANCE = 0;
	private static final int MASK_ALTITUDE = 1;
	private static final int MASK_AVGSPEED = 2;
	private static final int MASK_ASCENT = 3;
	private static final int MASK_BEARING = 4;
	private static final int MASK_TIME = 5;
	private static final int MASK_SPEED = 6;
	private static final int MASK_LAT = 7;
	private static final int MASK_LON = 8;
	private static final int MASK_ASCENTRATE = 9;
	private static final int MASK_SLOPE = 10;
	private static final int MASK_ACCURACY = 11;
	private static final int MASK_HEARTRATE = 12;

	
	// send plugin metadata to Canvas when requested
	@Override
	protected ArrayList<PluginDefinition> get_plugin_definitions(Context context) {
		Log.i(TAG, "get_plugin_definitions");
		
		// create a list of plugins provided by this app
		ArrayList<PluginDefinition> plugins = new ArrayList<PluginDefinition>();
		
		// now playing (text)
		TextPluginDefinition tplug = new TextPluginDefinition();
		tplug.id = ID_CANVAS_PB;
		tplug.name = "Pebble Bike";
		tplug.format_mask_descriptions = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.canvas_format_mask_descs)));
		// populate example content for each field (optional) to be display in the format mask editor
		ArrayList<String> examples = new ArrayList<String>();
		examples.add(current_data.distance);
		examples.add(current_data.altitude);
		examples.add(current_data.avgspeed);
		examples.add(current_data.ascent);
		examples.add(current_data.bearing);
		examples.add(current_data.time);
		examples.add(current_data.speed);
		examples.add(current_data.lat);
		examples.add(current_data.lon);
		examples.add(current_data.ascentrate);
		examples.add(current_data.slope);
		examples.add(current_data.accuracy);
		examples.add(current_data.heartrate);
		tplug.format_mask_examples = examples;
		tplug.format_masks = new ArrayList<String>(Arrays.asList(MASKS));
		tplug.default_format_string = "%DIST %ALT %AVG";
		plugins.add(tplug);
		
		return plugins;
	}
	
	//private static boolean process_just_started = true;
	//private static boolean got_now_playing = false;
	
	// send current text values to canvas when requested
	@Override
	protected String get_format_mask_value(int def_id, String format_mask, Context context, String param) {
	    if (MainActivity.debug) Log.i(TAG, "get_format_mask_value def_id = " + def_id + " format_mask = '" + format_mask + "'");
		
		if (def_id == ID_CANVAS_PB) {
			if (current_data == null) {
				Log.d(TAG, "get_format_mask_value current_data == null");
				return null;
			}
			// which field to return current value for?
			if (format_mask.equals(MASKS[MASK_DISTANCE])) {
				return current_data.distance;
			} else if (format_mask.equals(MASKS[MASK_ALTITUDE])) {
				return current_data.altitude;
			} else if (format_mask.equals(MASKS[MASK_AVGSPEED])) {
				return current_data.avgspeed;
			} else if (format_mask.equals(MASKS[MASK_ASCENT])) {
				return current_data.ascent;
			} else if (format_mask.equals(MASKS[MASK_BEARING])) {
				return current_data.bearing;
			} else if (format_mask.equals(MASKS[MASK_TIME])) {
				return current_data.time;
			} else if (format_mask.equals(MASKS[MASK_SPEED])) {
				return current_data.speed;
			} else if (format_mask.equals(MASKS[MASK_LAT])) {
				return current_data.lat;
			} else if (format_mask.equals(MASKS[MASK_LON])) {
				return current_data.lon;
			} else if (format_mask.equals(MASKS[MASK_ASCENTRATE])) {
				return current_data.ascentrate;
			} else if (format_mask.equals(MASKS[MASK_SLOPE])) {
				return current_data.slope;
			} else if (format_mask.equals(MASKS[MASK_ACCURACY])) {
				return current_data.accuracy;
            } else if (format_mask.equals(MASKS[MASK_HEARTRATE])) {
                return current_data.heartrate;
			}
		}
		Log.i(TAG, "no matching mask found");
		return null;
	}


	// send bitmap value to canvas when requested
	@Override
	protected Bitmap get_bitmap_value(int def_id, Context context, String param) {
	    if (MainActivity.debug) Log.i(TAG, "get_bitmap_value def_id = " + def_id);
		return null;
	}

	private static GPSData current_data = new GPSData();
	public static class GPSData {
		String distance, altitude, avgspeed, ascent, bearing, time, speed, lat, lon, ascentrate, slope, accuracy, heartrate;
	}

	// only notify canvas of an update if it has actually changed
	public static void set_gpsdata_details(GPSData data, Context context) {
	    if (MainActivity.debug) Log.i(TAG, "set_gpsdata_details distance='" + data.distance + " time=" + data.time + " avgspeed=" + data.avgspeed);
		current_data.distance = data.distance;
		current_data.altitude = data.altitude;
		current_data.avgspeed = data.avgspeed;
		current_data.ascent = data.ascent;
		current_data.bearing = data.bearing;
		current_data.time = data.time;
		current_data.speed = data.speed;
		current_data.lat = data.lat;
		current_data.lon = data.lon;
		current_data.ascentrate = data.ascentrate;
		current_data.slope = data.slope;
		current_data.accuracy = data.accuracy;
		current_data.heartrate = data.heartrate;
		notify_canvas_updates_available(ID_CANVAS_PB, context);
	}
}
