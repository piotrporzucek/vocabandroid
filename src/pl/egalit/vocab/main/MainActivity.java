package pl.egalit.vocab.main;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import pl.egalit.vocab.R;
import pl.egalit.vocab.Setup;
import pl.egalit.vocab.Util;
import pl.egalit.vocab.chooseCourse.ChooseCourseActivity;
import pl.egalit.vocab.foundation.providers.SchoolProviderMetaData;
import pl.egalit.vocab.foundation.providers.SchoolProviderMetaData.SchoolTableMetaData;
import pl.egalit.vocab.learn.intro.LearnIntroActivity;
import pl.egalit.vocab.model.ParcelableSchool;
import pl.egalit.vocab.shared.RegistrationInfoDto;
import pl.egalit.vocab.shared.SchoolDto;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends SherlockFragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {
	protected static final int SPLASH_ACTIVITY_CODE = 0;
	private static final String SHOULD_SPLASH_RUN_PROPERTY = "shouldSplashRun";
	protected boolean shouldSplashRun = true;

	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";

	/**
	 * Default lifespan (7 days) of a reservation until it is considered
	 * expired.
	 */
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

	private static final String TAG = "MainActivity";
	protected int maxSplashTime = 5000;

	private static final String SENDER_ID = "353612404730";
	private String regid;

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		// Inflate the options menu from XML
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

		MenuItem searchItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();

		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));

		searchView.setIconifiedByDefault(false);
		searchView.setSubmitButtonEnabled(false);

		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText.length() > 0) {

				} else {
					// Do something when there's no input
				}
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {

				InputMethodManager imm = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

				Intent intent = new Intent();
				intent.setAction("pl.egalit.vokabes.SEARCH");
				intent.putExtra("query", query);
				startActivity(intent);
				return false;
			}
		});

		return true;

	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.user_settings) {
			getSupportLoaderManager().restartLoader(2, null, this);
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SHOULD_SPLASH_RUN_PROPERTY, shouldSplashRun);
	}

	private GoogleCloudMessaging gcm;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		getSupportActionBar().setIcon(
				getResources().getDrawable(R.drawable.vokabes_icon_small));

		PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
		context = getApplicationContext();
		regid = getRegistrationId(context);
		gcm = GoogleCloudMessaging.getInstance(context);
		setContentView(R.layout.splash);
		if (Setup.getSchoolId(getApplicationContext()) == -1) {
			if (isOnline()) {
				getSupportLoaderManager().initLoader(1, null, this);
			} else {

				AlertDialog.Builder b = new AlertDialog.Builder(this)
						.setTitle(
								getResources().getString(
										R.string.noConnectionDialogTile))
						.setPositiveButton("OK", this)
						.setMessage(
								getResources().getString(R.string.noConnection));
				b.create().show();
			}

		} else {
			registerDevice(savedInstanceState);
		}

	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Setup.getSchoolId(getApplicationContext()) != -1) {
			registerDevice(data.getExtras());
		} else {
			finish();
		}
	}

	private void registerDevice(Bundle savedInstanceState) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if ("".equals(regid)) {
					registerBackground();
				} else {
					showMainContent();
				}
			}
		}, 3000);
		if (savedInstanceState != null) {
			shouldSplashRun = savedInstanceState.getBoolean(
					SHOULD_SPLASH_RUN_PROPERTY, true);
		}
	}

	/**
	 * Gets the current registration id for application on GCM service.
	 * <p>
	 * If result is empty, the registration has failed.
	 * 
	 * @return registration id, or empty string if the registration is not
	 *         complete.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			Log.v(TAG, "Registration not found.");
			return "";
		}
		// check if app was updated; if so, it must clear registration id to
		// avoid a race condition if GCM sends a message
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion || isRegistrationExpired()) {
			Log.v(TAG, "App version changed or registration expired.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Checks if the registration has expired.
	 * 
	 * <p>
	 * To avoid the scenario where the device sends the registration to the
	 * server but the server loses it, the app developer may choose to
	 * re-register after REGISTRATION_EXPIRY_TIME_MS.
	 * 
	 * @return true if the registration has expired.
	 */
	private boolean isRegistrationExpired() {
		final SharedPreferences prefs = getGCMPreferences();
		// checks if the information is not stale
		long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME,
				-1);
		return System.currentTimeMillis() > expirationTime;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's shared preferences.
	 */
	private void registerBackground() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging
								.getInstance(MainActivity.this);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration id=" + regid;

					registerInVokabes(regid);

					// Save the regid - no need to register again.
					setRegistrationId(MainActivity.this, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if (!"".equals(regid)) {
					showMainContent();
				}

			}

		}.execute(null, null, null);
	}

	protected void registerInVokabes(final String regid) {
		// You should send the registration ID to your server over
		// HTTP,
		// so it can use GCM/HTTP or CCS to send messages to your
		// app.
		RegistrationInfoDto registration = new RegistrationInfoDto();
		registration.setSchoolId(Setup.getSchoolId(getApplicationContext()));
		registration.setDeviceRegistrationId(regid);
		HttpEntity<RegistrationInfoDto> requestEntity = new HttpEntity<RegistrationInfoDto>(
				registration);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new MappingJacksonHttpMessageConverter());

		try {
			restTemplate.postForEntity(Util.getBaseUrl(getApplicationContext())
					+ "/register/aa", requestEntity, RegistrationInfoDto.class);
		} catch (Exception ex) {
			Log.e(TAG, "Error :" + ex.getMessage());
			Toast.makeText(this,
					getResources().getString(R.string.noConnection),
					Toast.LENGTH_LONG).show();
		}

	}

	private void showMainContent() {
		getSupportActionBar().show();
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setIcon(
				getResources().getDrawable(R.drawable.vokabes_icon_small));
		setContentView(R.layout.main);
		shouldSplashRun = false;
	}

	/**
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration id
	 */
	public void setRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = getAppVersion(context);
		Log.v(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		long expirationTime = System.currentTimeMillis()
				+ REGISTRATION_EXPIRY_TIME_MS;

		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}

	public void showCourses(View view) {
		startActivity(new Intent(this, LearnIntroActivity.class));
	}

	public void showCoursesSettings(View view) {
		startActivity(new Intent(this, ChooseCourseActivity.class));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		if (loaderId == 1) {
			return new CursorLoader(this,
					SchoolProviderMetaData.CONTENT_FRESH_URI, new String[] {
							SchoolTableMetaData._ID,
							SchoolTableMetaData.SCHOOL_NAME,
							SchoolTableMetaData.SCHOOL_CITY }, null, null,
					SchoolTableMetaData.SCHOOL_CITY);
		} else {
			return new CursorLoader(this, SchoolProviderMetaData.CONTENT_URI,
					new String[] { SchoolTableMetaData._ID,
							SchoolTableMetaData.SCHOOL_NAME,
							SchoolTableMetaData.SCHOOL_CITY }, null, null,
					SchoolTableMetaData.SCHOOL_CITY);
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor c) {
		Intent intent = new Intent().setClass(this, UserSettingsActivity.class);
		if (c.getCount() == 0) {
			CursorLoader cl = (CursorLoader) loader;
			cl.setUri(SchoolProviderMetaData.CONTENT_URI);
			return;
		}
		if (loader.getId() == 1) {

			prepareUserSettingsIntent(c, intent, true);
			getSupportLoaderManager().destroyLoader(1);
			startActivityForResult(intent, 0);
		} else {
			prepareUserSettingsIntent(c, intent, false);
			getSupportLoaderManager().destroyLoader(2);
			this.startActivity(intent);
		}

	}

	private void prepareUserSettingsIntent(final Cursor c, Intent intent,
			boolean showOnlySchoolChoice) {
		Parcelable[] parceableSchools = new Parcelable[c.getCount()];
		int i = 0;
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			long id = c.getLong(0);
			String name = c.getString(1);
			String city = c.getString(2);
			SchoolDto dto = new SchoolDto();
			dto.setCity(city);
			dto.setId(id);
			dto.setName(name);
			parceableSchools[i++] = (new ParcelableSchool(dto));
		}
		intent.putExtra("showOnlySchoolChoice", showOnlySchoolChoice);
		intent.putExtra("schools", parceableSchools);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		finish();
	}

}
