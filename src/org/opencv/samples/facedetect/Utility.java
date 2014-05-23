 package org.opencv.samples.facedetect;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utility {
	
	private static final String TAG = "PhotoPrivacy::Utility";
	// app's backend link
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
		// if uId is null then load from shared preferences
		if (uniqueUserId == null) {
			SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
			uniqueUserId = sharedPrefs.getString(PREF_UNIQUE_ID, null);
			
			// if upon loading from shared preferences, uId is still null it implies this is first time app is opened
			// assign new id to the device for current and future sessions
			if (uniqueUserId == null) {
				uniqueUserId = UUID.randomUUID().toString();
				Editor editor = sharedPrefs.edit();
				editor.putString(PREF_UNIQUE_ID, uniqueUserId);
				editor.commit();
			}
		}
		
		Log.i(TAG, uniqueUserId);
	}
	
	// check if network is accessible and settings are properly configured on device
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		return false;
	}
}
