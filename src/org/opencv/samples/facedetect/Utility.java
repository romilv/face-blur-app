package org.opencv.samples.facedetect;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Utility {
	
	private static final String TAG = "PhotoPrivacy::Utility";
	private static final String URL_NOTIFY = "http://rocky-citadel-2836.herokuapp.com/user/";
	private static final String URL_GET_CLOSEST_USERS = "http://rocky-citadel-2836.herokuapp.com/user/";
	private static String uniqueUserId;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	
	public static String getUserId() {
		if (uniqueUserId == null)
			Log.e(TAG, "requested uniqueUserId not instantiated");
		return uniqueUserId;
	}
	
	public static String getUrlNotify() {
		return URL_NOTIFY;
	}
	
	public static String getUrlClosestUsers() {
		return URL_GET_CLOSEST_USERS;
	}
	
	public synchronized static void setUniqueUserId (Context context) {
		if (uniqueUserId == null) {
			SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
			uniqueUserId = sharedPrefs.getString(PREF_UNIQUE_ID, null);
			if (uniqueUserId == null) {
				uniqueUserId = UUID.randomUUID().toString();
				Editor editor = sharedPrefs.edit();
				editor.putString(PREF_UNIQUE_ID, uniqueUserId);
				editor.commit();
			}
		}
	}
}