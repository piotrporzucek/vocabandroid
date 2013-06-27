package pl.egalit.vocab.learn.intro;

import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

public class ChosenCoursesSpinerAdapter extends SimpleCursorAdapter {

	public ChosenCoursesSpinerAdapter(Context context, Cursor c) {
		super(context, android.R.layout.simple_spinner_dropdown_item, c,
				new String[] { CourseTableMetaData.COURSE_NAME },
				new int[] { android.R.id.text1 }, 0);

	}

}
