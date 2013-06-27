package pl.egalit.vocab.learn.intro;

import pl.egalit.vocab.R;
import pl.egalit.vocab.chooseCourse.ChooseCourseActivity;
import pl.egalit.vocab.foundation.OnDialogDoneListener;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import pl.egalit.vocab.learn.words.WordActivity;
import pl.egalit.vocab.main.MainActivity;
import pl.egalit.vocab.shared.CourseDto;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class LearnIntroActivity extends SherlockFragmentActivity implements
		LoaderCallbacks<Cursor>, OnNavigationListener, OnDialogDoneListener {
	private ChosenCoursesSpinerAdapter adapter;
	private TextView repeatsCountField;
	private TextView newWordsCountField;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.learn_intro);
		getSupportLoaderManager().initLoader(0, null, this);
		adapter = new ChosenCoursesSpinerAdapter(this, null);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter, this);
		actionBar.setTitle(getString(R.string.choose_course));
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		CourseDto course = getCurrentCourse();
		if (course != null) {
			tryToShowCourse(course);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (loaderId == 0) {

			return new CursorLoader(this, CourseProviderMetaData.CONTENT_URI,
					new String[] { CourseTableMetaData._ID,
							CourseTableMetaData.COURSE_NAME,
							CourseTableMetaData.PASSWORD,
							CourseTableMetaData.INITIALIZED },
					CourseTableMetaData.INITIALIZED + "=1 OR "
							+ CourseTableMetaData.ACTIVE + "=1", null,
					CourseTableMetaData.ACTIVE + " DESC,"
							+ CourseTableMetaData.INITIALIZED + " DESC");
		} else if (loaderId == 1) {
			String courseId = bundle.getString("courseId");

			return new CursorLoader(this,
					Uri.parse("content://" + WordProviderMetaData.AUTHORITY
							+ "/words/new/" + courseId), null, null, null, null);
		} else if (loaderId == 2) {
			String courseId = bundle.getString("courseId");
			return new CursorLoader(this, Uri.parse("content://"
					+ WordProviderMetaData.AUTHORITY + "/words/repeats/"
					+ courseId), null, null, null, null);
		} else if (loaderId == 3) {
			pd = ProgressDialog.show(this, "Prosze czekac",
					"Trwa pobieranie kursu...");
			String courseId = bundle.getString("courseId");
			return new CursorLoader(this, Uri.parse("content://"
					+ WordProviderMetaData.AUTHORITY + "/words/" + courseId),
					null, null, null, null);
		}
		return null;

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (loader.getId() == 0) {
			adapter.swapCursor(c);
			if (c.getCount() == 0) {
				noCourses();
			}

		} else if (loader.getId() == 1 && c != null
				&& newWordsCountField != null) {
			newWordsCountField.setText(c.getCount() + "");

		} else if (loader.getId() == 2 && c != null
				&& repeatsCountField != null) {
			repeatsCountField.setText(c.getCount() + "");

		} else if (loader.getId() == 3 && c != null) {
			CourseDto course = getCurrentCourse();
			pd.cancel();
			if (c.getCount() > 0) {
				Bundle bundle = new Bundle();
				bundle.putString("courseId", course.getId().toString());
				getSupportLoaderManager().restartLoader(1, bundle, this);
				getSupportLoaderManager().restartLoader(2, bundle, this);
			}

		}

	}

	private void noCourses() {
		Toast.makeText(this,
				"You have no chosen courses... choose fisrt at least one.",
				Toast.LENGTH_LONG).show();
		startActivity(new Intent(this, ChooseCourseActivity.class));

	}

	private CourseDto getCurrentCourse() {
		CourseDto course = getCourse(getSupportActionBar()
				.getSelectedNavigationIndex());
		return course;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == 0) {
			adapter.swapCursor(null);
		}

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		tryToShowCourse(itemPosition);
		return true;
	}

	private void tryToShowCourse(int itemPosition) {
		CourseDto course = getCourse(itemPosition);
		if (course.isInitialized()) {
			showCourseLearningDetails(course);
		} else {
			authorizeCourse(course);
		}
	}

	private void tryToShowCourse(CourseDto course) {
		if (course.isInitialized()) {
			showCourseLearningDetails(course);
		} else {
			authorizeCourse(course);
		}
	}

	private void authorizeCourse(CourseDto course) {
		CourseAccessAuthorizationDialogFragment courseAccessAuthorizationDialog = CourseAccessAuthorizationDialogFragment
				.newInstance(course);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		courseAccessAuthorizationDialog
				.show(ft,
						CourseAccessAuthorizationDialogFragment.COURSE_ACCESS_AUTH_DIALOG_TAG);

	}

	@Override
	public void onDialogDone(String tag, boolean cancelled) {
		if (!cancelled) {
			CourseDto course = getCurrentCourse();
			ContentValues cv = new ContentValues();
			cv.put(CourseTableMetaData.INITIALIZED, 1);
			getContentResolver().update(
					Uri.withAppendedPath(
							CourseProviderMetaData.CONTENT_SINGLE_URI,
							Long.toString(course.getId())), cv, null, null);
			showCourseLearningDetails(course);
		}
	}

	private void showCourseLearningDetails(CourseDto course) {
		Bundle bundle = new Bundle();
		bundle.putString("courseId", course.getId().toString());
		getSupportLoaderManager().destroyLoader(3);
		getSupportLoaderManager().restartLoader(3, bundle, this);
		LearnIntroFragment newFragment = LearnIntroFragment.init(course);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.replace(R.id.fragment_container, newFragment, course.getName());
		ft.commit();

	}

	public CourseDto getCourse(int position) {
		if (position < 0) {
			return null;
		}
		Cursor cursor = (Cursor) adapter.getItem((position));
		String courseName = cursor.getString(1);
		String password = cursor.getString(2);
		boolean initialized = cursor.getInt(3) != 0;
		long courseId = cursor.getLong(0);
		CourseDto course = new CourseDto(courseId, courseName, password,
				initialized);
		return course;
	}

	public void registerForNewWordsCount(final TextView textView) {
		this.newWordsCountField = textView;
	}

	public void registerForRepeatsCount(final TextView textView) {
		this.repeatsCountField = textView;
	}

	public void startLearning(CourseDto courseDto) {

		Intent intent = new Intent(this, WordActivity.class);
		intent.putExtra("courseId", courseDto.getId());
		startActivity(intent);
	}

	public void onStartLearningButtonClick(View view) {

		startLearning(getCurrentCourse());
	}

}
