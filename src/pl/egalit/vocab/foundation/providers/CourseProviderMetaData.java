package pl.egalit.vocab.foundation.providers;

import android.net.Uri;

public class CourseProviderMetaData {
	public static final String AUTHORITY = "pl.egalit.vocab.CourseProvider";
	public static final String DATABASE_NAME = "vocab.db";
	public static final int DATABASE_VERSION = 1;
	public static final String COURSES_TABLE_NAME = "courses";
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.pl.egalit.vocab.course";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.pl.egalit.vocab.course";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/courses");
	public static final Uri CONTENT_FRESH_URI = Uri.parse("content://"
			+ AUTHORITY + "/courses/fresh");
	public static final Uri CONTENT_ARCHIVE_URI = Uri.parse("content://"
			+ AUTHORITY + "/courses/archive");
	public static final Uri CONTENT_SINGLE_URI = Uri.parse("content://"
			+ AUTHORITY + "/courses/#");

	private CourseProviderMetaData() {
	};

	public static final class CourseTableMetaData implements
			AbstractTableMetaData {
		public static final String TABLE_NAME = "courses";

		public static final String COURSE_NAME = "name";

		public static final String COURSE_DESCRIPTION = "description";

		/**
		 * indicates if the course was chosen by the user
		 */
		public static final String COURSE_CHOSEN = "chosen";

		public static final String DEFAULT_SORT_ORDER = COURSE_NAME + " ASC";

		/**
		 * Indicates if course is not deleted by the user.
		 */
		public static final String ACTIVE = "active";
		public static final String START_DATE = "startDate";
		public static final String END_DATE = "endDate";

		public static final String INITIALIZED = "initialized";

		public static final String PASSWORD = "password";

	}
}
