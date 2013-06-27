package pl.egalit.vocab.chooseCourse;

import pl.egalit.vocab.R;
import pl.egalit.vocab.model.CourseRowModel;
import pl.egalit.vocab.shared.CourseDto;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CourseCursorAdapter extends SimpleCursorAdapter {
	private final ChooseCourseListFragment chooseCourseListFragment;

	public CourseCursorAdapter(ChooseCourseListFragment chooseCourseListFragment) {
		super(chooseCourseListFragment.getActivity(),
				R.layout.choose_course_row, null, new String[] {
						CourseDto.CHOSEN_PROPERTY, CourseDto.NAME_PROPERTY },
				new int[] { R.id.course_chosen, R.id.course_details_link }, 0);
		this.chooseCourseListFragment = chooseCourseListFragment;
		setViewBinder(new CourseAdapterViewBinder(chooseCourseListFragment));

	}

	@Override
	public Cursor swapCursor(Cursor c) {
		Cursor result = super.swapCursor(c);
		if (c != null) {
			chooseCourseListFragment.refreshModel(c);
		}
		return result;
	}

	@Override
	protected void onContentChanged() {
		super.onContentChanged();
		chooseCourseListFragment.refreshModel(getCursor());
	}

	public static class CourseAdapterViewBinder implements ViewBinder {

		private ChooseCourseListFragment chooseCourseListFragment;

		public CourseAdapterViewBinder(
				ChooseCourseListFragment chooseCourseListFragment2) {
			this.chooseCourseListFragment = chooseCourseListFragment2;
		}

		@Override
		public boolean setViewValue(View view, final Cursor c, int columnIndex) {

			if (view instanceof CheckBox) {
				CheckBox cb = (CheckBox) view;
				cb.setChecked(c.getInt(2) != 0);
				cb.setText(c.getString(1));
				cb.setTag(c.getPosition());
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Integer position = (Integer) buttonView.getTag();
						CourseRowModel modelElement = chooseCourseListFragment
								.getModelElement(position);
						if (modelElement != null) {
							modelElement.setChosen(isChecked);
						}
						chooseCourseListFragment.refreshMenu();
					}
				});
				return true;
			} else if (view instanceof Button) {
				view.setTag(c.getPosition());
				return true;
			}
			return false;
		}
	}

}
