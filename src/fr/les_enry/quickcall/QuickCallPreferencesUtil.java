package fr.les_enry.quickcall;

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
	
//	boolean getCallInProgress() {
//		return sharedPrefs.getBoolean(CALL_IN_PROGRESS, false);
//	}
//	
//	void atomicPutCallInProgress(boolean enabled) {
//		sharedPrefs.edit().putBoolean(CALL_IN_PROGRESS, enabled).apply();
//	}
}
