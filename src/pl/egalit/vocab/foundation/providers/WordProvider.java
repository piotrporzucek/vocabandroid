package pl.egalit.vocab.foundation.providers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import pl.egalit.vocab.R;
import pl.egalit.vocab.ServiceHelper;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData.WordTableMetaData;
import pl.egalit.vocab.learn.words.WordAnswerEnum;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WordProvider extends AbstractVocabProvider {

	private static final String TAG = "WordProvider";

	private static final Map<String, String> wordProjectionMap;

	private static final UriMatcher wordUriMatcher;

	private static final int WORD_REPEATS_COLLECTION_URI_INDICATOR = 0;
	private static final int WORD_NEW_COLLECTION_URI_INDICATOR = 1;

	private static final int WORD_COLLECTION_URI_INDICATOR = 2;
	private static final int WORD_STATE_UPDATE_URI_INDICATOR = 3;

	private static final int WORD_COLLECTION_SEARCH_URI_INDICATOR = 4;

	static {
		wordProjectionMap = new HashMap<String, String>();
		wordProjectionMap.put(CourseTableMetaData._ID, CourseTableMetaData._ID);
		wordProjectionMap.put(CourseTableMetaData.COURSE_NAME,
				CourseTableMetaData.COURSE_NAME);

		wordUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		wordUriMatcher.addURI(WordProviderMetaData.AUTHORITY,
				"words/repeats/#", WORD_REPEATS_COLLECTION_URI_INDICATOR);
		wordUriMatcher.addURI(WordProviderMetaData.AUTHORITY, "words/new/#",
				WORD_NEW_COLLECTION_URI_INDICATOR);
		wordUriMatcher.addURI(WordProviderMetaData.AUTHORITY, "words/#",
				WORD_COLLECTION_URI_INDICATOR);
		wordUriMatcher.addURI(WordProviderMetaData.AUTHORITY,
				"words/state/#/#", WORD_STATE_UPDATE_URI_INDICATOR);
		wordUriMatcher.addURI(WordProviderMetaData.AUTHORITY, "words/search/*",
				WORD_COLLECTION_SEARCH_URI_INDICATOR);

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		int uriType = wordUriMatcher.match(uri);
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		queryBuilder.setTables(WordProviderMetaData.WORDS_TABLE_NAME);

		Cursor cursor = null;
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (uriType) {
		case WORD_COLLECTION_SEARCH_URI_INDICATOR:
			String query = uri.getLastPathSegment();
			selection = WordTableMetaData.WORD_ANSWER + " LIKE '%" + query
					+ "%'" + " OR " + WordTableMetaData.WORD_EXPRESSION
					+ " LIKE '%" + query + "%'";
			return queryBuilder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);

		case WORD_COLLECTION_URI_INDICATOR:
			String courseId = uri.getPathSegments().get(1);
			queryBuilder.appendWhere("courseId=" + courseId);
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);

			final int requestId = ServiceHelper.getInstance()
					.startOperationGetWords(getContext(), handler, receiver,
							databaseHelper,
							Long.parseLong(uri.getPathSegments().get(1)));
			if (pendingOperations.get(requestId) == null) {
				pendingOperations.put(requestId, new PendingOperation() {
					@Override
					public void perform(boolean emptyResultSet) {
						if (!emptyResultSet) {
							getContext().getContentResolver().notifyChange(
									WordProviderMetaData.CONTENT_WORDS_URI,
									null);
						}

					}
				});
			}
			;

			cursor.setNotificationUri(getContext().getContentResolver(),
					WordProviderMetaData.CONTENT_WORDS_URI);
			return cursor;

		case WORD_NEW_COLLECTION_URI_INDICATOR:
			String limit = getLimit();
			courseId = uri.getPathSegments().get(2);
			queryBuilder.appendWhere("lastShownOn is NULL AND courseId="
					+ courseId);

			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder, limit);
			cursor.setNotificationUri(getContext().getContentResolver(),
					WordProviderMetaData.CONTENT_NEW_WORDS_URI);
			return cursor;

		case WORD_REPEATS_COLLECTION_URI_INDICATOR:
			courseId = uri.getPathSegments().get(2);
			queryBuilder
					.appendWhere("lastShownOn is not null AND nextShowOn is not null AND DATE(nextShowOn) <= DATE('now') AND courseId="
							+ courseId);
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);

			cursor.setNotificationUri(getContext().getContentResolver(),
					WordProviderMetaData.CONTENT_REPEAT_WORDS_URI);
			return cursor;

		}

		return null;
	}

	private String getLimit() {
		SharedPreferences prefs = getContext().getSharedPreferences(
				"pl.egalit.vocab_preferences", 0);
		return prefs.getString(
				getContext().getResources().getString(
						R.string.selected_word_amount_daily), null);
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = wordUriMatcher.match(uri);
		if (uriType != WORD_STATE_UPDATE_URI_INDICATOR) {
			throw new IllegalArgumentException("Unknown URI for update");
		}
		String id = uri.getPathSegments().get(2);
		int state = Integer.parseInt(uri.getPathSegments().get(3));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar nextShowOn = Calendar.getInstance();
		nextShowOn.add(Calendar.DATE, WordAnswerEnum.getDaysForRepeat(state));
		ContentValues cv = new ContentValues();
		cv.put(WordTableMetaData.WORD_LAST_SHOWN_ON,
				dateFormat.format(Calendar.getInstance().getTime()));
		cv.put(WordTableMetaData.WORD_NEXT_SHOW_ON,
				dateFormat.format(nextShowOn.getTime()));

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		return db.update(WordTableMetaData.TABLE_NAME, cv,
				WordTableMetaData._ID + "=?", new String[] { id });

	}

	@Override
	protected String getTAG() {
		return TAG;
	}

}
