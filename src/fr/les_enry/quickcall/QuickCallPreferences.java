package fr.les_enry.quickcall;

public interface QuickCallPreferences {
	
	/** Name of the file in which data it stored. */
	static final String SHARED_PREFERENCES = "QuickCallPreferences";
	
	/** Boolean indicating whether call should be made automatically when activity starts. */
	static final String AUTO_CALL = "AUTO_CALL";
	
	/** Phone number to call. */
	static final String PHONE_NB = "PHONE_NB";
	
	/** Call in progress. */
//	static final String CALL_IN_PROGRESS ="CALL_IN_PROGRESS"; 
}
