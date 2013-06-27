package pl.egalit.vocab.chooseCourse;

import java.util.ArrayList;
import java.util.List;

import pl.egalit.vocab.R;
import pl.egalit.vocab.model.ParcelableCourse;
import pl.egalit.vocab.shared.CourseDto;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

@SuppressLint("ParserError")
public class ChooseCourseActivity extends SherlockFragmentActivity {
	static final String TAG = "ChooseCourseActivity";

	private List<CourseDto> courses;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.choose_course);
		ActionBar actionBar = getSupportActionBar();
		actionBar
				.setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_TABS);

		Tab tab = actionBar
				.newTab()
				.setText(R.string.currentCourses)
				.setTabListener(
						new TabListener<CurrentCourseListFragment>(this,
								"currentCourses",
								CurrentCourseListFragment.class));
		actionBar.addTab(tab);
		tab = actionBar
				.newTab()
				.setText(R.string.archiveCourses)
				.setTabListener(
						new TabListener<ArchiveCourseListFragment>(this,
								"archivedCourses",
								ArchiveCourseListFragment.class));

		actionBar.addTab(tab);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	public static class TabListener<T extends Fragment> implements
			ActionBar.TabListener {
		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public TabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Check if the fragment is already initialized
			if (mFragment == null) {
				// If not, instantiate and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.detach(mFragment);
			}
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// User selected the already selected tab. Usually do nothing.
		}
	}

	private boolean isMultiPane() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public void showDetails(View view) {
		showDetails((Integer) (view.getTag()));

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (courses != null) {
			ArrayList<ParcelableCourse> parceableCourses = new ArrayList<ParcelableCourse>();
			for (CourseDto course : courses) {
				parceableCourses.add(new ParcelableCourse(course));
			}
			outState.putParcelableArrayList("courses", parceableCourses);
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey("courses")) {
			ArrayList<ParcelableCourse> parelableCourses = savedInstanceState
					.getParcelableArrayList("courses");
			List<CourseDto> courses = new ArrayList<CourseDto>();
			for (ParcelableCourse parcelableCourse : parelableCourses) {
				courses.add(parcelableCourse.getCourse());
			}
			// setCourses(courses);
		}
	}

	public void showDetails(int courseId) {
		Log.v(TAG, "show Details with " + courseId);

		if (isMultiPane() && findViewById(R.id.choose_course_details) != null) {
			CourseDetailsFragment detailsPane = (CourseDetailsFragment) getSupportFragmentManager()
					.findFragmentById(R.id.choose_course_details);
			if (detailsPane == null || detailsPane.getCourseId() != courseId) {
				detailsPane = CourseDetailsFragment.newInstance(courseId);
				android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				transaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.addToBackStack("details");
				transaction.replace(R.id.choose_course_details, detailsPane);
				transaction.commit();

			}
		} else {
			Intent intent = new Intent();
			intent.setClass(this, ChooseCourseDetailsActivity.class);
			intent.putExtra("courseId", courseId);
			startActivity(intent);
		}
	}

}
