package fr.les_enry.quickcall;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

// http://www.mkyong.com/android/how-to-make-a-phone-call-in-android/

// todo store time at which last call was placed, and use this to avoid call loop, but restart activity on end of call, and apply auto-dial whnever activity resumes, not only onCreate().

public class MainActivity extends Activity {

	private static final String TAG = "QuickCall";

	private static final int CONTACT_PICKER_RESULT = 1001;

	private QuickCallPreferencesUtil quickCallPrefs = null;

	/** Time to wait between battery status checks. */
	private static final long AUTO_DIAL_PAUSE_MS = 1000;

	private Timer autoDialTimer = null;
	private Timer autoCutoutTimer = null;

	/** Needs to be in synch with string resource auto_cutout_array */
	private int autoCutoutTimeouts[] = { 0, 5, 10, 15, 20, 30, 40, 50 };

	private int autoCutoutTimeoutOffset = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		quickCallPrefs = new QuickCallPreferencesUtil(this);

		CheckBox autoDialCheckBox = (CheckBox) findViewById(R.id.autoDialCheckBox);
		autoDialCheckBox.setChecked(quickCallPrefs.getAutoCall());
		autoDialCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;

				quickCallPrefs.atomicPutAutoCall(cb.isChecked());
			}
		});

		EditText phoneNbEditText = (EditText) findViewById(R.id.phoneNbEditText);
		phoneNbEditText.setText(quickCallPrefs.getPhoneNb());
		phoneNbEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				quickCallPrefs.atomicPutPhoneNb(s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		Button callButton = (Button) findViewById(R.id.callButton);
		callButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleCallButtonClick(v);
			}
		});

		Button contactSelectButton = (Button) findViewById(R.id.contactSelectButton);
		contactSelectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleContactSelectButtonClick(v);
			}
		});

		Spinner spinner = (Spinner) findViewById(R.id.autoCutoutSpinner);
		autoCutoutTimeoutOffset = quickCallPrefs.getAutoCutoutTimeoutOffset();
		spinner.setSelection(autoCutoutTimeoutOffset);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				autoCutoutTimeoutOffset = pos;
				quickCallPrefs
						.atomicPutAutoCutoutTimeoutOffset(autoCutoutTimeoutOffset);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Don't change selection
			}
		});

		// Auto start call?
		if (autoDialCheckBox.isChecked()) {
			autoDialTimer = new Timer();
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							makeCallAuto();
						}
					});
				}
			};
			autoDialTimer.schedule(task, AUTO_DIAL_PAUSE_MS);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Handles user tapping the make-call button.
	 * 
	 * @param view
	 */
	void handleCallButtonClick(View view) {
		makeCall();
	}

	/**
	 * Called to make a call automatically. Checks time last call was made and
	 * whether the auto dial checkbox is still checked.
	 * 
	 */
	void makeCallAuto() {
		CheckBox autoDialCheckBox = (CheckBox) findViewById(R.id.autoDialCheckBox);
		if (!autoDialCheckBox.isChecked()) {
			return;
		}

		// Check time of last call
		Date lastCall = quickCallPrefs.getLastCallTime();
		Date now = new Date();
		long delay = quickCallPrefs.getAutoRedialDelayMs();
		if ((lastCall == null) || (now.getTime() - lastCall.getTime() > delay)) {
			makeCall();
		}
	}

	/**
	 * Launches dialer.
	 */
	void makeCall() {
		quickCallPrefs.atomicPutLastCallTime(new Date());

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		final PhoneCallListener phoneListener = new PhoneCallListener(
				telephonyManager);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		EditText phoneNbEditText = (EditText) findViewById(R.id.phoneNbEditText);
		String phoneNb = phoneNbEditText.getText().toString();
		if (!phoneNb.equals("")) {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + phoneNb));
			startActivity(callIntent);

			if (autoCutoutTimeoutOffset > 0) {
				autoCutoutTimer = new Timer();
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// Stop the call
								Log.d(TAG, "Hanging up the call");
								phoneListener.hangup();
							}
						});
					}
				};
				autoCutoutTimer.schedule(task,
						autoCutoutTimeouts[autoCutoutTimeoutOffset] * 1000);
			}
		}
	}

	/**
	 * Opens contact selection activity.
	 * 
	 * @param view
	 */
	void handleContactSelectButtonClick(View view) {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
		pickContactIntent
				.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(pickContactIntent, CONTACT_PICKER_RESULT);
	}

	/**
	 * Handles result of contact selection.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				String phoneNo = null;
				Uri uri = data.getData();
				Cursor cursor = getContentResolver().query(uri, null, null,
						null, null);
				cursor.moveToFirst();

				int phoneIndex = cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				phoneNo = cursor.getString(phoneIndex);

				EditText phoneNbEditText = (EditText) findViewById(R.id.phoneNbEditText);
				phoneNbEditText.setText(phoneNo);
				break;
			}

		} else {
			// gracefully handle failure
			Log.w(TAG, "Warning: activity result not ok");
		}
	}

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

				// Happens as soon as phone goes off te hook, doesn't wait until
				// other end has answered.
				// hangup();
			}

			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// run when class initial and phone call ended,
				// need detect flag from CALL_STATE_OFFHOOK
				Log.i(TAG, "IDLE");

				if (isPhoneCalling) {

					// Restart activity
					Intent i = getBaseContext().getPackageManager()
							.getLaunchIntentForPackage(
									getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);

					isPhoneCalling = false;
				}
			}
		}

		void hangup() {
			try {
				// String serviceManagerName = "android.os.IServiceManager";
				String serviceManagerName = "android.os.ServiceManager";
				String serviceManagerNativeName = "android.os.ServiceManagerNative";
				String telephonyName = "com.android.internal.telephony.ITelephony";

				Class<?> telephonyClass;
				Class<?> telephonyStubClass;
				Class<?> serviceManagerClass;
				// Class<?> serviceManagerStubClass;
				Class<?> serviceManagerNativeClass;
				// Class<?> serviceManagerNativeStubClass;

				Method telephonyEndCall;
				// Method telephonyAnswerCall;
				// Method getDefault;
				//
				// Method[] temps;
				// Constructor[] serviceManagerConstructor;

				// Method getService;
				Object telephonyObject;
				Object serviceManagerObject;

				telephonyClass = Class.forName(telephonyName);
				telephonyStubClass = telephonyClass.getClasses()[0];
				serviceManagerClass = Class.forName(serviceManagerName);
				serviceManagerNativeClass = Class
						.forName(serviceManagerNativeName);

				Method getService = // getDefaults[29];
				serviceManagerClass.getMethod("getService", String.class);

				Method tempInterfaceMethod = serviceManagerNativeClass
						.getMethod("asInterface", IBinder.class);

				Binder tmpBinder = new Binder();
				tmpBinder.attachInterface(null, "fake");

				serviceManagerObject = tempInterfaceMethod.invoke(null,
						tmpBinder);
				IBinder retbinder = (IBinder) getService.invoke(
						serviceManagerObject, "phone");
				Method serviceMethod = telephonyStubClass.getMethod(
						"asInterface", IBinder.class);

				telephonyObject = serviceMethod.invoke(null, retbinder);
				// telephonyCall = telephonyClass.getMethod("call",
				// String.class);
				telephonyEndCall = telephonyClass.getMethod("endCall");
				// telephonyAnswerCall =
				// telephonyClass.getMethod("answerRingingCall");

				telephonyEndCall.invoke(telephonyObject);

			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,
						"FATAL ERROR: could not connect to telephony subsystem");
				Log.e(TAG, "Exception object: " + e);
			}
		}
	}
}
