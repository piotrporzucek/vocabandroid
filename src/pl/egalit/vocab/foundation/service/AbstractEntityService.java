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

public abstract class AbstractEntityService<T extends HasCollection, E> extends
		IntentService {

	public static final int EMPTY_RESULTSET = 0;
	public static final int NOT_EMPTY_RESULTSET = 1;
	private static final String CALLBACK = "callback";
	private static final String REQUEST_ID = "requestId";

	public AbstractEntityService(String name) {
		super(name);
	}

	protected MySQLiteHelper databaseHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		databaseHelper = new MySQLiteHelper(getApplicationContext());
	}

	protected long getLastUpdate(Intent intent) {
		long entityId = intent.getLongExtra("entityId", -1);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables("UPDATE_STATUS");
		String[] selectionArgs;
		String selection = "entity=?";
		if (entityId != -1) {
			selectionArgs = new String[] { getEntityName(), entityId + "" };
			selection += " AND entity_id=?";
		} else {
			selectionArgs = new String[] { getEntityName() };
		}
		Cursor result = queryBuilder.query(db, new String[] { "lastUpdate" },
				selection, selectionArgs, null, null, null);

		if (!result.moveToFirst()) {
			ContentValues cv = new ContentValues();
			cv.put("lastUpdate", 0);
			cv.put("entity", getEntityName());
			cv.put("entity_id", entityId);
			db.insert("UPDATE_STATUS", null, cv);
			db.close();
			return 0;
		} else {
			db.close();
			return result.getLong(0);
		}

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

		ResponseEntity<T> result = restTemplate.exchange(url, HttpMethod.GET,
				requestEntity, getRestTransportEntityClass());

		Bundle resultData = new Bundle();
		resultData.putInt(REQUEST_ID, intent.getExtras().getInt(REQUEST_ID));
		if (!result.getBody().isEmpty()) {
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
		long entityId = +intent.getLongExtra("entityId", -1);
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("lastUpdate", getLastUpdateTimeNewValue());
		cv.put("entity", getEntityName());
		String[] selectionArgs;
		String selection = "entity=?";
		if (entityId != -1) {
			selectionArgs = new String[] { getEntityName(), entityId + "" };
			selection += " AND entity_id=?";
		} else {
			selectionArgs = new String[] { getEntityName() };
		}
		int count = db.update("UPDATE_STATUS", cv, selection, selectionArgs);
		if (count == 0) {
			db.insert("UPDATE_STATUS", null, cv);
		}
	}
}
