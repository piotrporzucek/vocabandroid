package pl.egalit.vocab.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.egalit.vocab.R;
import pl.egalit.vocab.model.ParcelableSchool;
import pl.egalit.vocab.shared.SchoolDto;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class UserSettingsActivity extends SherlockPreferenceActivity {
	private HashMap<String, List<SchoolDto>> mapCitiesToSchools;
	private ListPreference citiesPreference;
	private ListPreference schoolPreference;
	private Preference exitLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent() == null) {
			finish();
		}
		boolean showOnlySchoolChoice = getIntent().getBooleanExtra(
				"showOnlySchoolChoice", false);
		Parcelable[] schools = getIntent().getParcelableArrayExtra("schools");
		mapCitiesToSchools(schools);
		if (showOnlySchoolChoice) {
			addPreferencesFromResource(R.xml.user_settings_empty);
			setPreferenceScreen(createSchoolChoiceScreen(showOnlySchoolChoice));
		} else {
			addPreferencesFromResource(R.xml.user_settings);
			getPreferenceScreen().addPreference(
					createSchoolChoiceScreen(showOnlySchoolChoice));
		}

	}

	private PreferenceScreen createSchoolChoiceScreen(
			boolean showOnlySchoolChoice) {
		PreferenceScreen screen = getPreferenceManager()
				.createPreferenceScreen(this);
		screen.setTitle(getResources().getString(R.string.schoolSettings));
		schoolPreference = createSchoolPreference(showOnlySchoolChoice);
		citiesPreference = createCitiesPreference();
		screen.addPreference(citiesPreference);
		screen.addPreference(schoolPreference);
		if (showOnlySchoolChoice) {
			exitLink = createExitLink();
			screen.addPreference(exitLink);
		}
		return screen;
	}

	private Preference createExitLink() {
		Preference preference = new Preference(this);
		preference.setSelectable(false);
		preference.setEnabled(false);
		preference.setKey("exitlink");
		preference.setTitle(getResources().getString(
				R.string.exitPreferenceLink));
		preference
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						setResult(RESULT_OK, new Intent().putExtra("schoolId",
								schoolPreference.getValue()));
						finish();
						return true;
					}
				});
		return preference;
	}

	private ListPreference createSchoolPreference(boolean showOnlySchoolChoice) {
		final ListPreference schoolPreference = new ListPreference(this);
		schoolPreference.setKey("selected_school_id");
		schoolPreference.setTitle(getResources()
				.getString(R.string.schoolTitle));
		if (schoolPreference.getValue() != null) {
			schoolPreference.setSummary(schoolPreference.getValue());
		} else {
			schoolPreference.setSummary(getResources().getString(
					R.string.schoolSummary));
		}

		schoolPreference.setDialogTitle(getResources().getString(
				R.string.schoolDialogTitle));
		if (showOnlySchoolChoice) {
			schoolPreference.setEnabled(false);
		}

		schoolPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						List<SchoolDto> schoolsOfTheChosenCity = mapCitiesToSchools
								.get(citiesPreference.getEntry());
						for (SchoolDto school : schoolsOfTheChosenCity) {
							if (Long.parseLong(newValue.toString()) == (school
									.getId())) {
								schoolPreference.setSummary(school.getName());
							}
						}
						if (exitLink != null) {
							exitLink.setSelectable(true);
							exitLink.setEnabled(true);
						}
						deleteRegistrationId();
						return true;
					}
				});
		return schoolPreference;
	}

	private ListPreference createCitiesPreference() {

		final ListPreference citiesPreference = new ListPreference(this);
		citiesPreference.setKey("selected_city");
		citiesPreference.setTitle(getResources().getString(R.string.cityTitle));

		citiesPreference.setDialogTitle(getResources().getString(
				R.string.cityDialogTitle));
		CharSequence[] citiesNames = new CharSequence[mapCitiesToSchools
				.keySet().size()];
		int i = 0;
		for (String cityName : mapCitiesToSchools.keySet()) {
			citiesNames[i++] = cityName;
		}
		citiesPreference.setEntries(citiesNames);
		citiesPreference.setEntryValues(citiesNames);
		citiesPreference.setEnabled(true);
		SharedPreferences prefs = getSharedPreferences(
				"pl.egalit.vocab_preferences", 0);
		String city = prefs.getString(
				getResources().getString(R.string.selected_city), null);

		if (city != null) {
			citiesPreference.setSummary(city);
			loadSchools(city);
		} else {
			citiesPreference.setSummary(getResources().getString(
					R.string.citySummary));
		}

		citiesPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						citiesPreference.setSummary(newValue.toString());
						loadSchools(newValue.toString());
						return true;
					}

				});
		return citiesPreference;
	}

	private void loadSchools(String city) {
		CharSequence[] schoolNames = new CharSequence[mapCitiesToSchools.get(
				city).size()];
		CharSequence[] schoolIds = new CharSequence[mapCitiesToSchools
				.get(city).size()];
		int i = 0;
		for (SchoolDto school : mapCitiesToSchools.get(city)) {
			schoolNames[i] = school.getName();
			schoolIds[i] = school.getId().toString();
			i++;

		}
		schoolPreference.setEntries(schoolNames);
		schoolPreference.setEntryValues(schoolIds);
		schoolPreference.setEnabled(true);
		String schoolName = findChosenSchoolName(city);
		if (schoolName != null) {
			schoolPreference.setSummary(schoolName);
		}
	}

	private String findChosenSchoolName(String city) {
		SharedPreferences prefs = getSharedPreferences(
				"pl.egalit.vocab_preferences", 0);
		String schoolId = prefs.getString(
				getResources().getString(R.string.selected_school_id), null);
		if (schoolId == null) {
			return null;
		}
		for (SchoolDto school : mapCitiesToSchools.get(city)) {
			if (Long.parseLong(schoolId) == school.getId().longValue()) {
				return school.getName();
			}
		}
		return null;
	}

	private void mapCitiesToSchools(Parcelable[] schools) {
		mapCitiesToSchools = new HashMap<String, List<SchoolDto>>();
		if (schools == null) {
			return;
		}
		for (Parcelable school : schools) {
			SchoolDto dto = ((ParcelableSchool) school).getSchool();
			String city = dto.getCity();
			if (mapCitiesToSchools.get(city) == null) {
				mapCitiesToSchools.put(city, new ArrayList<SchoolDto>());
			}
			mapCitiesToSchools.get(city).add(dto);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
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
	public void deleteRegistrationId() {
		final SharedPreferences prefs = getGCMPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MainActivity.PROPERTY_REG_ID, null);

		editor.commit();
	}

}
