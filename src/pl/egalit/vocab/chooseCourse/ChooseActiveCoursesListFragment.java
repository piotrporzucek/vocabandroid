package pl.egalit.vocab.chooseCourse;

import pl.egalit.vocab.Setup;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class ChooseActiveCoursesListFragment extends ChooseCourseListFragment {

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {

		return new CursorLoader(getActivity(),
				CourseProviderMetaData.CONTENT_FRESH_URI, new String[] {
						CourseTableMetaData._ID,
						CourseTableMetaData.COURSE_NAME,
						CourseTableMetaData.COURSE_CHOSEN,
						CourseTableMetaData._STATUS,
						CourseTableMetaData.LANGUAGE },
				CourseTableMetaData.ACTIVE + "=? AND "
						+ CourseTableMetaData.SCHOOL_ID + "=?", new String[] {
						"1",
						""
								+ Setup.getSchoolId(getActivity()
										.getApplicationContext()) },
				CourseTableMetaData.COURSE_NAME);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor c) {

		if (loader.getId() == 0) {
			adapter.swapCursor(c);
			CursorLoader cl = (CursorLoader) loader;
			cl.setUri(CourseProviderMetaData.CONTENT_URI);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == 0) {
			adapter.swapCursor(null);
		}

	}
}
