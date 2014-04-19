package fr.les_enry.quickcall;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class QuickCallPreferencesUtil implements QuickCallPreferences {
	SharedPreferences sharedPrefs;
	
	SharedPreferences getSharedPrefs() {
		return sharedPrefs;
	}

	QuickCallPreferencesUtil(Context context) {
		sharedPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
	}
	
	String getPhoneNb() {
		return sharedPrefs.getString(PHONE_NB, "");
	}
	
	void atomicPutPhoneNb(String newNb) {
		sharedPrefs.edit().putString(PHONE_NB, newNb).apply();
	}
	
	void atomicRemovePhoneNb() {
		sharedPrefs.edit().remove(PHONE_NB).apply();
	}
	
	boolean getAutoCall() {
		return sharedPrefs.getBoolean(AUTO_CALL, false);
	}
	
	void atomicPutAutoCall(boolean enabled) {
		sharedPrefs.edit().putBoolean(AUTO_CALL, enabled).apply();
	}
	
	int getAutoCutoutTimeoutOffset() {
		return sharedPrefs.getInt(AUTO_CUTOUT_TIMEOUT_OFFSET, 0);
	}
	
	void atomicPutAutoCutoutTimeoutOffset(int autoCutoutOffset) {
		sharedPrefs.edit().putInt(AUTO_CUTOUT_TIMEOUT_OFFSET, autoCutoutOffset).apply();
	}
	
	long getAutoRedialDelayMs() {
		return sharedPrefs.getLong(AUTO_REDIAL_DELAY_MS, 5 * 60 * 1000);
	}
	
	void atomicPutAutoRedialDelayMs(long autoRedialDelayMs) {
		sharedPrefs.edit().putLong(AUTO_REDIAL_DELAY_MS, autoRedialDelayMs).apply();
	}
	
	Date getLastCallTime() {
		long lastCall = sharedPrefs.getLong(LAST_CALL_TIME, Long.MIN_VALUE);
		
		return lastCall == Long.MIN_VALUE ? null : new Date(lastCall);
	}
	
	void atomicPutLastCallTime(Date lastCall) {
		if (lastCall != null) {
			long millis = lastCall.getTime();
			sharedPrefs.edit().putLong(LAST_CALL_TIME, millis).apply();
		}
	}
	
	//	boolean getCallInProgress() {
//		return sharedPrefs.getBoolean(CALL_IN_PROGRESS, false);
//	}
//	
//	void atomicPutCallInProgress(boolean enabled) {
//		sharedPrefs.edit().putBoolean(CALL_IN_PROGRESS, enabled).apply();
//	}
}
