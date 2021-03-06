package pl.egalit.vocab.chooseCourse;

import java.util.ArrayList;
import java.util.List;

import pl.egalit.vocab.foundation.providers.CourseProviderMetaData;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.main.MainActivity;
import pl.egalit.vocab.model.CourseRowModel;
import pl.egalit.vocab.shared.CourseDto;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;

public abstract class ChooseCourseListFragment extends SherlockListFragment
		implements LoaderCallbacks<Cursor> {

	private static final String CHOSEN_COURSES_POSITIONS_PROPERTY = "chosenCoursesPositions";
	private ArrayList<CourseRowModel> oldModel;
	private ArrayList<Integer> chosenCoursesPositions;
	private List<CourseRowModel> model;
	protected CourseCursorAdapter adapter;
	private ThreadGroup saveThreads = null;

	CourseRowModel getModelElement(int position) {
		return getModel().get(position);
	}

	public ChooseCourseListFragment() {

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ArrayList<Integer> checkedPositions = new ArrayList<Integer>();
		if (model == null)
			return;
		for (CourseRowModel modelElement : model) {
			if (modelElement.isChosen()) {
				checkedPositions.add(modelElement.getPosition());
			}
		}
		outState.putIntegerArrayList(CHOSEN_COURSES_POSITIONS_PROPERTY,
				checkedPositions);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		saveThreads = new ThreadGroup("save threads");
		if (savedInstanceState != null
				&& savedInstanceState
						.containsKey(CHOSEN_COURSES_POSITIONS_PROPERTY)) {
			chosenCoursesPositions = savedInstanceState
					.getIntegerArrayList(CHOSEN_COURSES_POSITIONS_PROPERTY);
		}
		adapter = new CourseCursorAdapter(this);
		setListAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(getSherlockActivity(),
					MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void confirmSave(CheckBox cb) {
		SaveCoursesDialogFragment frag = SaveCoursesDialogFragment.newInstance(
				this, cb);
		android.support.v4.app.FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		frag.show(transaction,
				SaveCoursesDialogFragment.SAVE_COURSES_DIALOG_TAG);

	}

	private void addToModel(List<CourseRowModel> model, CourseDto dto,
			int position) {
		CourseRowModel m = new CourseRowModel(dto, position);
		model.add(m);
		if (chosenCoursesPositions != null
				&& chosenCoursesPositions.contains(position)) {
			m.setChosen(true);

		} else if (chosenCoursesPositions != null) {
			m.setChosen(false);

		}

	}

	public List<CourseRowModel> getModel() {
		return model;
	}

	public void refreshModel(Cursor cursor) {
		model = new ArrayList<CourseRowModel>();
		oldModel = new ArrayList<CourseRowModel>();
		int position = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			CourseDto course = new CourseDto();
			course.setChosen(cursor.getInt(2) != 0);
			course.setId(cursor.getLong(0));
			course.setName(cursor.getString(1));
			addToModel(model, course, position);
			addToModel(oldModel, course, position);
			position++;

		}

	}

	public List<CourseDto> getChosenCourses() {
		List<CourseDto> courses = new ArrayList<CourseDto>();
		for (CourseRowModel modelElement : model) {
			if (modelElement.isChosen()) {
				courses.add(modelElement.getData());
			}
		}
		return courses;
	}

	public List<CourseDto> getNewlyChosenCourses() {
		return ChooseCourseSupport.getNewlyChosenCourses(oldModel, model);
	}

	public List<CourseDto> getNewlyUnchosenCourses() {
		return ChooseCourseSupport.getNewlyUnChosenCourses(oldModel, model);
	}

	public void saveCourses() {

		List<CourseDto> newlyChosenCourses = getNewlyChosenCourses();
		List<CourseDto> newlyUnchosenCourses = getNewlyUnchosenCourses();
		updateChosenState(newlyChosenCourses, true);
		updateChosenState(newlyUnchosenCourses, false);

	}

	private void updateChosenState(final List<CourseDto> currentCourses,
			final boolean isChosen) {
		if (currentCourses.isEmpty()) {
			return;
		}
		Runnable r = new Runnable() {

			@Override
			public void run() {
				ContentValues cv = new ContentValues();
				cv.put(CourseTableMetaData.COURSE_CHOSEN, isChosen);

				String repChar = "?,";
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < currentCourses.size(); i++) {
					buffer.append(repChar);
				}
				String[] selectionArgs = new String[currentCourses.size()];
				for (int i = 0; i < currentCourses.size(); i++) {
					selectionArgs[i] = currentCourses.get(i).getId().toString();
				}
				getActivity().getContentResolver().update(
						CourseProviderMetaData.CONTENT_URI,
						cv,
						CourseTableMetaData._ID + " IN (" + buffer.toString()
								+ "-1)", selectionArgs);
			}
		};
		new Thread(saveThreads, r).start();

	}

	@Override
	public void onDestroy() {
		saveThreads.interrupt();
		super.onDestroy();

	}

	public void courseCheckedChange(boolean isChecked, CheckBox cb) {
		if (ChooseCourseSupport.areAnyModelElementsDeselected(oldModel, model)) {
			confirmSave(cb);
		} else {
			saveCourses();

		}
	}

	public void cancelUnselect(CourseRowModel courseRowModel) {

	}

}
