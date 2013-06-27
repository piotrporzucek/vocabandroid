package pl.egalit.vocab;

import java.util.HashMap;
import java.util.Map;

import pl.egalit.vocab.foundation.db.MySQLiteHelper;
import pl.egalit.vocab.foundation.db.UpdateStatus;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

@SuppressLint({ "ParserError", "ParserError" })
public class ServiceHelper {

	private static final int SERVICE_ERROR = -1;

	private static ServiceHelper instance;

	private int currentCoursesRequestId = 0;
	private int currentWordsRequestId = 0;

	private Map<Integer, Intent> pendingCourseOperations = new HashMap<Integer, Intent>();
	private Map<Integer, Intent> pendingWordsOperations = new HashMap<Integer, Intent>();

	private ServiceHelper() {

	}

	public static synchronized ServiceHelper getInstance() {
		if (instance == null) {
			instance = new ServiceHelper();
		}
		return instance;
	}

	public int startOperation(final Context context, Handler handler,
			final ResultReceiver callback, MySQLiteHelper databaseHelper,
			final Intent intent, final Map<Integer, Intent> pendingOperations,
			int currentRequestId) {

		Integer pendingRequestId = getPendingRequestId(pendingOperations,
				intent);
		if (pendingRequestId != null) {
			return pendingRequestId;
		}
		ResultReceiver resultReceiver = new ResultReceiver(handler) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				int requestId = resultData.getInt("requestId");
				if (resultCode == SERVICE_ERROR) {
					context.startService(pendingOperations.get(requestId));
				}
				callback.send(resultCode, resultData);
				pendingOperations.remove(requestId);
			}
		};
		intent.putExtra("callback", resultReceiver);
		intent.putExtra("requestId", currentRequestId);
		context.startService(intent);
		pendingCourseOperations.put(currentRequestId, intent);
		return currentRequestId++;
	}

	private void setUpdatingStatus(String tableName, UpdateStatus status,
			MySQLiteHelper databaseHelper) {
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(CourseTableMetaData._STATUS, status.name());
		db.update(CourseTableMetaData.TABLE_NAME, cv, null, null);

	}

	private Integer getPendingRequestId(Map<Integer, Intent> pendingOperations,
			Intent intent) {
		for (Map.Entry<Integer, Intent> entry : pendingOperations.entrySet()) {
			if (entry.getValue().getAction().equals(intent.getAction())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public int startOperationGetCourses(Context context, Handler handler,
			ResultReceiver receiver, MySQLiteHelper databaseHelper) {
		setUpdatingStatus(
				CourseProviderMetaData.CourseTableMetaData.TABLE_NAME,
				UpdateStatus.UPDATING, databaseHelper);
		final Intent intent = new Intent();
		intent.setAction("pl.egalit.vocab.GET_COURSES");
		intent.setData(CourseProviderMetaData.CONTENT_FRESH_URI);
		intent.setType(CourseProviderMetaData.CONTENT_TYPE);
		return startOperation(context, handler, receiver, databaseHelper,
				intent, pendingCourseOperations, currentCoursesRequestId);
	}

	public int startOperationGetWords(Context context, Handler handler,
			ResultReceiver receiver, MySQLiteHelper databaseHelper,
			long courseId) {
		setUpdatingStatus(WordProviderMetaData.WORDS_TABLE_NAME,
				UpdateStatus.UPDATING, databaseHelper);
		final Intent intent = new Intent();
		intent.setAction("pl.egalit.vocab.GET_WORDS");
		intent.setData(WordProviderMetaData.CONTENT_WORDS_URI);
		intent.setType(WordProviderMetaData.CONTENT_TYPE);
		intent.putExtra("entityId", courseId);
		return startOperation(context, handler, receiver, databaseHelper,
				intent, pendingCourseOperations, currentCoursesRequestId);
	}
}
