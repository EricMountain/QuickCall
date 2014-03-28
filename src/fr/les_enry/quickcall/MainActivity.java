package fr.les_enry.quickcall;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

// http://www.mkyong.com/android/how-to-make-a-phone-call-in-android/


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button callButton = (Button) findViewById(R.id.callButton);
		callButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleCallButtonClick(v);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	void handleCallButtonClick(View view) {
		TelephonyManager telephonyManager = (TelephonyManager) this
			.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneCallListener phoneListener = new PhoneCallListener(telephonyManager);
		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);

		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:0652421814"));
		startActivity(callIntent);
	}
	
	//monitor phone call activities
	private class PhoneCallListener extends PhoneStateListener {
 
		private boolean isPhoneCalling = false;
 
		private static final String TAG = "QCPhoneListener";
 
		private final TelephonyManager telephonyManager;
		
		PhoneCallListener(TelephonyManager tm) {
			telephonyManager = tm;
		}
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
 
			if (TelephonyManager.CALL_STATE_RINGING == state) {
				// phone ringing
				Log.i(TAG, "RINGING, number: " + incomingNumber);
			}
 
			// http://stackoverflow.com/questions/599443/how-to-hang-up-outgoing-call-in-android
			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// active
				Log.i(TAG, "OFFHOOK");
 
				isPhoneCalling = true;
				
				try {
			        //String serviceManagerName = "android.os.IServiceManager";
			        String serviceManagerName = "android.os.ServiceManager";
			        String serviceManagerNativeName = "android.os.ServiceManagerNative";
			        String telephonyName = "com.android.internal.telephony.ITelephony";

			        Class telephonyClass;
			        Class telephonyStubClass;
			        Class serviceManagerClass;
			        Class serviceManagerStubClass;
			        Class serviceManagerNativeClass;
			        Class serviceManagerNativeStubClass;

			        Method telephonyCall;
			        Method telephonyEndCall;
			        Method telephonyAnswerCall;
			        Method getDefault;

			        Method[] temps;
			        Constructor[] serviceManagerConstructor;

			        // Method getService;
			        Object telephonyObject;
			        Object serviceManagerObject;

			        telephonyClass = Class.forName(telephonyName);
			        telephonyStubClass = telephonyClass.getClasses()[0];
			        serviceManagerClass = Class.forName(serviceManagerName);
			        serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

			        Method getService = // getDefaults[29];
			                serviceManagerClass.getMethod("getService", String.class);

			        Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
			                "asInterface", IBinder.class);

			        Binder tmpBinder = new Binder();
			        tmpBinder.attachInterface(null, "fake");

			        serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
			        IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
			        Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);

			        telephonyObject = serviceMethod.invoke(null, retbinder);
			        //telephonyCall = telephonyClass.getMethod("call", String.class);
			        telephonyEndCall = telephonyClass.getMethod("endCall");
			        //telephonyAnswerCall = telephonyClass.getMethod("answerRingingCall");

			        // TODO happens too early.  Use a sleep? :/
			        //telephonyEndCall.invoke(telephonyObject);

			    } catch (Exception e) {
			        e.printStackTrace();
			        Log.e(TAG, "FATAL ERROR: could not connect to telephony subsystem");
			        Log.e(TAG, "Exception object: " + e);
			}			}
 
			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// run when class initial and phone call ended, 
				// need detect flag from CALL_STATE_OFFHOOK
				Log.i(TAG, "IDLE");
 
				if (isPhoneCalling) {
 
					Log.i(TAG, "restart app");
 
					// restart app
					Intent i = getBaseContext().getPackageManager()
						.getLaunchIntentForPackage(
							getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
 
					isPhoneCalling = false;
				}
 
			}
		}
	}
 
}
