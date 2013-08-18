package pl.egalit.vocab.foundation.providers;

import android.net.Uri;

public class SchoolProviderMetaData {
	public static final String AUTHORITY = "pl.egalit.vocab.SchoolProvider";
	public static final int DATABASE_VERSION = 1;
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.pl.egalit.vocab.school";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.pl.egalit.vocab.school";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/schools");
	public static final Uri CONTENT_FRESH_URI = Uri.parse("content://"
			+ AUTHORITY + "/schools/fresh");
	public static final Uri CONTENT_ARCHIVE_URI = Uri.parse("content://"
			+ AUTHORITY + "/schools/archive");
	public static final Uri CONTENT_SINGLE_URI = Uri.parse("content://"
			+ AUTHORITY + "/schools/#");

	private SchoolProviderMetaData() {
	};

	public static final class SchoolTableMetaData implements
			AbstractTableMetaData {
		public static final String TABLE_NAME = "schools";

		public static final String SCHOOL_NAME = "name";

		public static final String SCHOOL_CITY = "city";

		public static final String DEFAULT_SORT_ORDER = SCHOOL_NAME + " ASC";

	}
}
