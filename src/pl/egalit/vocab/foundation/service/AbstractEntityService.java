package pl.egalit.vocab.foundation.service;

import java.util.Calendar;
import java.util.Collections;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.db.MySQLiteHelper;
import pl.egalit.vocab.foundation.db.UpdateStatus;
import pl.egalit.vocab.foundation.providers.AbstractTableMetaData;
import pl.egalit.vocab.shared.HasCollection;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

public abstract class AbstractEntityService<T extends HasCollection, E> extends
		IntentService {

	public static final String FORCE_CONNECTION_AVAILABLE = "forceConnectionAvailable";
	public static final int EMPTY_RESULTSET = 0;
	public static final int NOT_EMPTY_RESULTSET = 1;
	private static final String CALLBACK = "callback";
	private static final String REQUEST_ID = "requestId";
	private static final String TAG = "AbstractEntityService";

	public AbstractEntityService(String name) {
		super(name);

	}

	protected MySQLiteHelper databaseHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		databaseHelper = new MySQLiteHelper(getApplicationContext());
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	protected long getLastUpdate(Intent intent) {
		String[] params = intent.getStringArrayExtra("params");
		String[] paramNames = intent.getStringArrayExtra("paramNames");

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables("UPDATE_STATUS");

		StringBuilder selectionBuilder = new StringBuilder();
		String[] selectionArgs = applyParamsFromIntent(params, paramNames,
				selectionBuilder);
		String selection = selectionBuilder.toString();

		Cursor result = queryBuilder.query(db, new String[] { "lastUpdate" },
				selection, selectionArgs, null, null, null);

		if (!result.moveToFirst()) {
			ContentValues cv = new ContentValues();
			cv.put("lastUpdate", 0);
			cv.put("entity", getEntityName());
			applyParametersToContentValues(params, paramNames, cv);
			db.insert("UPDATE_STATUS", null, cv);
			db.close();
			return 0;
		} else {
			db.close();
			long lastUpdateTime = result.getLong(0);
			return lastUpdateTime;
		}

	}

	private void applyParametersToContentValues(String[] params,
			String[] paramNames, ContentValues cv) {
		if (params != null && paramNames != null) {
			for (int i = 0; i < paramNames.length; i++) {
				cv.put(paramNames[i], params[i]);
			}
		}
	}

	private String[] applyParamsFromIntent(String[] params,
			String[] paramNames, StringBuilder selection) {
		String[] selectionArgs;
		selection.append("entity=?");
		if (params != null && paramNames != null) {
			if (params.length != paramNames.length) {
				throw new RuntimeException(
						"Params and Paramnames must have the same number of elements.");
			}
			selectionArgs = new String[params.length + 1];
			selectionArgs[0] = getEntityName();

			for (int i = 0; i < params.length; i++) {
				selectionArgs[i + 1] = params[i];
			}
			for (int i = 0; i < paramNames.length; i++) {
				selection.append(" AND " + paramNames[i] + "=?");
			}

		} else {
			selectionArgs = new String[] { getEntityName() };
		}
		return selectionArgs;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ResultReceiver callback = (ResultReceiver) intent.getExtras().get(
				CALLBACK);

		String url = getRestUrl(intent);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAcceptEncoding(Collections
				.singletonList(ContentCodingType.GZIP));

		HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new MappingJacksonHttpMessageConverter());

		ResponseEntity<T> result = null;
		try {

			result = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					getRestTransportEntityClass());
		} catch (RuntimeException ex) {
			if (intent.getBooleanExtra(FORCE_CONNECTION_AVAILABLE, false)) {
				Toast.makeText(this,
						getResources().getString(R.string.noConnection),
						Toast.LENGTH_SHORT).show();
			}
		}

		Bundle resultData = new Bundle();
		resultData.putInt(REQUEST_ID, intent.getExtras().getInt(REQUEST_ID));
		if (result != null && !result.getBody().isEmpty()) {
			updateEntities(result.getBody());
			setUpdateStatusForEntity(intent);
			setUpdatingStatus(UpdateStatus.READY);
			callback.send(NOT_EMPTY_RESULTSET, resultData);
			return;

		} else {
			setUpdatingStatus(UpdateStatus.READY);
			callback.send(EMPTY_RESULTSET, resultData);
		}

	}

	private void setUpdatingStatus(UpdateStatus status) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(AbstractTableMetaData._STATUS, status.name());
		db.update(getEntityName(), cv, null, null);
		db.close();

	}

	protected abstract Class<T> getRestTransportEntityClass();

	protected abstract String getEntityName();

	protected abstract String getRestUrl(Intent intent);

	protected abstract void updateEntities(T result);

	protected long getLastUpdateTimeNewValue() {
		return Calendar.getInstance().getTimeInMillis();
	}

	protected void setUpdateStatusForEntity(Intent intent) {

		String[] params = intent.getStringArrayExtra("params");
		String[] paramNames = intent.getStringArrayExtra("paramNames");

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables("UPDATE_STATUS");
		String[] selectionArgs = null;
		String selection = null;
		if (params != null && paramNames != null) {
			StringBuilder selectionBuilder = new StringBuilder();
			selectionArgs = applyParamsFromIntent(params, paramNames,
					selectionBuilder);
			selection = selectionBuilder.toString();
		}

		ContentValues cv = new ContentValues();
		cv.put("lastUpdate", getLastUpdateTimeNewValue());
		cv.put("entity", getEntityName());
		applyParametersToContentValues(params, paramNames, cv);
		int count = db.update("UPDATE_STATUS", cv, selection, selectionArgs);
		if (count == 0) {
			db.insert("UPDATE_STATUS", null, cv);
		}
	}

	protected String getParameter(Intent intent, String paramName,
			String defaultValue) {
		String[] params = intent.getStringArrayExtra("params");
		String[] paramNames = intent.getStringArrayExtra("paramNames");
		for (int i = 0; i < paramNames.length; i++) {
			if (paramNames[i].equals(paramName)) {
				return params[i];
			}
		}

		return defaultValue;
	}

}
