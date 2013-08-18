package pl.egalit.vocab.foundation.providers;

import java.util.HashMap;
import java.util.Map;

import pl.egalit.vocab.ServiceHelper;
import pl.egalit.vocab.foundation.providers.SchoolProviderMetaData.SchoolTableMetaData;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

@SuppressLint("ParserError")
public class SchoolProvider extends AbstractVocabProvider {

	private static final String TAG = "SchoolContentProvider";

	private static final Map<String, String> schoolProjectionMap;

	private static final UriMatcher schoolUriMatcher;

	private static final int SCHOOLS_COLLECTION_URI_INDICATOR = 0;
	private static final int SCHOOLS_COLLECTION_FRESH_URI_INDICATOR = 1;

	static {
		schoolProjectionMap = new HashMap<String, String>();
		schoolProjectionMap.put(SchoolTableMetaData._ID,
				SchoolTableMetaData._ID);
		schoolProjectionMap.put(SchoolTableMetaData.SCHOOL_NAME,
				SchoolTableMetaData.SCHOOL_NAME);

		schoolUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		schoolUriMatcher.addURI(SchoolProviderMetaData.AUTHORITY,
				"schools/fresh", SCHOOLS_COLLECTION_FRESH_URI_INDICATOR);

		schoolUriMatcher.addURI(SchoolProviderMetaData.AUTHORITY, "schools",
				SCHOOLS_COLLECTION_URI_INDICATOR);

	}

	@Override
	public Cursor query(final Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		int uriType = schoolUriMatcher.match(uri);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder
				.setTables(SchoolProviderMetaData.SchoolTableMetaData.TABLE_NAME);
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = SchoolTableMetaData.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		switch (uriType) {
		case SCHOOLS_COLLECTION_FRESH_URI_INDICATOR:

			return getCursorFreshSchools(projection, selection, selectionArgs,
					queryBuilder, orderBy, db);

		case SCHOOLS_COLLECTION_URI_INDICATOR:

			Cursor c = getCursorSchools(uri, projection, selection,
					selectionArgs, queryBuilder, orderBy, db);
			if (c.getCount() == 0) {
				return getCursorFreshSchools(projection, selection,
						selectionArgs, queryBuilder, orderBy, db);
			} else {
				return c;
			}

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

	}

	private Cursor getCursorFreshSchools(String[] projection, String selection,
			String[] selectionArgs, SQLiteQueryBuilder queryBuilder,
			String orderBy, SQLiteDatabase db) {
		Cursor cursor;
		cursor = queryBuilder.query(db, projection, selection, selectionArgs,
				null, null, orderBy);
		final int requestId = ServiceHelper.getInstance()
				.startOperationGetSchools(getContext(), handler, receiver,
						databaseHelper);
		if (pendingOperations.get(requestId) == null) {
			pendingOperations.put(requestId, new PendingOperation() {
				@Override
				public void perform(boolean emptyResultSet) {

					getContext().getContentResolver().notifyChange(
							SchoolProviderMetaData.CONTENT_URI, null);

				}
			});
		}

		cursor.setNotificationUri(getContext().getContentResolver(),
				SchoolProviderMetaData.CONTENT_URI);
		return cursor;
	}

	private Cursor getCursorSchools(final Uri uri, String[] projection,
			String selection, String[] selectionArgs,
			SQLiteQueryBuilder queryBuilder, String orderBy, SQLiteDatabase db) {
		Cursor cursor;
		cursor = queryBuilder.query(db, projection, selection, selectionArgs,
				null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (schoolUriMatcher.match(uri)) {
		case SCHOOLS_COLLECTION_URI_INDICATOR:
			return SchoolProviderMetaData.CONTENT_TYPE;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getTAG() {
		return TAG;
	}

}
