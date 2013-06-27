package pl.egalit.vocab.foundation.providers;

import android.net.Uri;

public class WordProviderMetaData {
	public static final String AUTHORITY = "pl.egalit.vocab.WordProvider";
	public static final String DATABASE_NAME = "courses.db";
	public static final int DATABASE_VERSION = 1;
	public static final String WORDS_TABLE_NAME = "words";
	public static final Uri CONTENT_NEW_WORDS_URI = Uri.parse("content://"
			+ AUTHORITY + "/words/new/#");
	public static final Uri CONTENT_REPEAT_WORDS_URI = Uri.parse("content://"
			+ AUTHORITY + "/words/repeats/#");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.pl.egalit.vocab.word";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.pl.egalit.vocab.word";
	public static final Uri CONTENT_WORDS_URI = Uri.parse("content://"
			+ AUTHORITY + "/words/#");
	public static final Uri CONTENT_WORDS_URI_STATE = Uri.parse("content://"
			+ AUTHORITY + "/words/state/#/#");

	private WordProviderMetaData() {
	};

	public static final class WordTableMetaData implements
			AbstractTableMetaData {
		public static final String TABLE_NAME = "words";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/words");

		public static final String WORD_EXPRESSION = "expression";
		public static final String WORD_ANSWER = "answer";
		public static final String WORD_NEXT_SHOW_ON = "nextShowOn";
		public static final String WORD_LAST_SHOWN_ON = "lastShownOn";
		public static final String _COURSE_ID = "courseId";
		public static final String WORD_EXAMPLE = "example";

	}
}
