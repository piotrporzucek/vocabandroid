package pl.egalit.vocab.foundation.providers;

import java.util.HashMap;
import java.util.Map;

import pl.egalit.vocab.ServiceHelper;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("ParserError")
public class CourseProvider extends AbstractVocabProvider {

	private static final String TAG = "ContentProvider";

	private static final Map<String, String> courseProjectionMap;

	private static final UriMatcher courseUriMatcher;

	private static final int COURSES_COLLECTION_URI_INDICATOR = 0;
	private static final int COURSES_COLLECTION_FRESH_URI_INDICATOR = 1;
	private static final int COURSES_COLLECTION_ARCHIVE_URI_INDICATOR = 2;
	private static final int COURSES_SINGLE_URI_INDICATOR = 3;

	static {
		courseProjectionMap = new HashMap<String, String>();
		courseProjectionMap.put(CourseTableMetaData._ID,
				CourseTableMetaData._ID);
		courseProjectionMap.put(CourseTableMetaData.COURSE_NAME,
				CourseTableMetaData.COURSE_NAME);

		courseUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		courseUriMatcher.addURI(CourseProviderMetaData.AUTHORITY,
				"courses/fresh", COURSES_COLLECTION_FRESH_URI_INDICATOR);
		courseUriMatcher.addURI(CourseProviderMetaData.AUTHORITY,
				"courses/archive", COURSES_COLLECTION_ARCHIVE_URI_INDICATOR);
		courseUriMatcher.addURI(CourseProviderMetaData.AUTHORITY, "courses/#",
				COURSES_SINGLE_URI_INDICATOR);

		courseUriMatcher.addURI(CourseProviderMetaData.AUTHORITY, "courses",
				COURSES_COLLECTION_URI_INDICATOR);

	}

	@Override
	public Cursor query(final Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		int uriType = courseUriMatcher.match(uri);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(CourseProviderMetaData.COURSES_TABLE_NAME);
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = CourseTableMetaData.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (uriType) {
		case COURSES_COLLECTION_ARCHIVE_URI_INDICATOR:

			return getCursorArchiveCourses(projection, selection,
					selectionArgs, sortOrder, queryBuilder, db);
		case COURSES_COLLECTION_FRESH_URI_INDICATOR:

			return getCursorFreshCourses(projection, selection, selectionArgs,
					queryBuilder, orderBy, db);

		case COURSES_COLLECTION_URI_INDICATOR:

			return getCursorCourses(uri, projection, selection, selectionArgs,
					queryBuilder, orderBy, db);

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

	}

	private Cursor getCursorArchiveCourses(String[] projection,
			String selection, String[] selectionArgs, String sortOrder,
			SQLiteQueryBuilder queryBuilder, SQLiteDatabase db) {
		Cursor cursor = queryBuilder.query(db, projection,
				CourseTableMetaData.COURSE_CHOSEN + "=1 AND "
						+ CourseTableMetaData.ACTIVE + "=0 AND " + selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(),
				CourseProviderMetaData.CONTENT_ARCHIVE_URI);
		return cursor;
	}

	private Cursor getCursorFreshCourses(String[] projection, String selection,
			String[] selectionArgs, SQLiteQueryBuilder queryBuilder,
			String orderBy, SQLiteDatabase db) {
		Cursor cursor;
		cursor = queryBuilder.query(db, projection, CourseTableMetaData.ACTIVE
				+ "=1 "
				+ (TextUtils.isEmpty(selection) ? "" : " AND " + selection),
				selectionArgs, null, null, orderBy);
		final int requestId = ServiceHelper.getInstance()
				.startOperationGetCourses(getContext(), handler, receiver,
						databaseHelper);
		if (pendingOperations.get(requestId) == null) {
			pendingOperations.put(requestId, new PendingOperation() {
				@Override
				public void perform(boolean emptyResultSet) {

					getContext().getContentResolver().notifyChange(
							CourseProviderMetaData.CONTENT_URI, null);

				}
			});
		}

		cursor.setNotificationUri(getContext().getContentResolver(),
				CourseProviderMetaData.CONTENT_URI);
		return cursor;
	}

	private Cursor getCursorCourses(final Uri uri, String[] projection,
			String selection, String[] selectionArgs,
			SQLiteQueryBuilder queryBuilder, String orderBy, SQLiteDatabase db) {
		Cursor cursor;
		cursor = queryBuilder.query(db, projection,
				(TextUtils.isEmpty(selection) ? "" : "" + selection),
				selectionArgs, null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (courseUriMatcher.match(uri)) {
		case COURSES_COLLECTION_URI_INDICATOR:
			return CourseProviderMetaData.CONTENT_TYPE;
		default:
			return null;
		}

	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count;
		switch (courseUriMatcher.match(uri)) {
		case COURSES_COLLECTION_URI_INDICATOR:
			ContentValues cv = new ContentValues();
			cv.put(CourseTableMetaData.ACTIVE, false);
			count = db.update(CourseTableMetaData.TABLE_NAME, cv, selection,
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		db.close();
		getContext().getContentResolver().notifyChange(
				CourseProviderMetaData.CONTENT_URI, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		int count;
		switch (courseUriMatcher.match(uri)) {
		case COURSES_COLLECTION_URI_INDICATOR:
			count = db.update(CourseTableMetaData.TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case COURSES_SINGLE_URI_INDICATOR:
			String courseId = (uri.getPathSegments().get(1));
			count = db.update(
					CourseTableMetaData.TABLE_NAME,
					values,
					CourseTableMetaData._ID
							+ "="
							+ courseId
							+ (TextUtils.isEmpty(selection) ? "" : " AND"
									+ selection), selectionArgs);
			if (count == 0) {
				Log.w(TAG, "No update for Course with Id = " + courseId);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		db.close();
		getContext().getContentResolver().notifyChange(
				CourseProviderMetaData.CONTENT_URI, null);
		return count;
	}

	@Override
	protected String getTAG() {
		return TAG;
	}

}
