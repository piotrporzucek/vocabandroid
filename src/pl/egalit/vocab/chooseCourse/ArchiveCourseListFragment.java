package pl.egalit.vocab.chooseCourse;

import pl.egalit.vocab.Setup;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class ArchiveCourseListFragment extends ChooseCourseListFragment {

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {

		return new CursorLoader(getActivity(),
				CourseProviderMetaData.CONTENT_ARCHIVE_URI, new String[] {
						CourseTableMetaData._ID,
						CourseTableMetaData.COURSE_NAME,
						CourseTableMetaData.COURSE_CHOSEN,
						CourseTableMetaData.LANGUAGE },
				CourseTableMetaData.SCHOOL_ID + "=?", new String[] { ""
						+ Setup.getSchoolId(getActivity()
								.getApplicationContext()) },
				CourseTableMetaData.COURSE_NAME);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor c) {

		if (loader.getId() == 0) {
			adapter.swapCursor(c);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == 0) {
			adapter.swapCursor(null);
		}

	}

}
