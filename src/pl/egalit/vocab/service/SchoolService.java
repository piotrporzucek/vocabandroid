package pl.egalit.vocab.service;

import pl.egalit.vocab.Util;
import pl.egalit.vocab.foundation.providers.SchoolProviderMetaData.SchoolTableMetaData;
import pl.egalit.vocab.foundation.service.AbstractEntityService;
import pl.egalit.vocab.shared.ListSchoolsDto;
import pl.egalit.vocab.shared.SchoolDto;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class SchoolService extends
		AbstractEntityService<ListSchoolsDto, SchoolDto> {

	public SchoolService() {
		super("School Service");
	}

	@Override
	protected void updateEntities(ListSchoolsDto schools) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		for (SchoolDto school : schools) {
			ContentValues cv = new ContentValues();
			cv.put(SchoolTableMetaData._ID, school.getId());
			cv.put(SchoolTableMetaData.SCHOOL_CITY, school.getCity());
			cv.put(SchoolTableMetaData.SCHOOL_NAME, school.getName());
			int count = db.update(SchoolTableMetaData.TABLE_NAME, cv,
					SchoolTableMetaData._ID + "=?", new String[] { school
							.getId().toString() });
			if (count == 0) {
				db.insert(SchoolTableMetaData.TABLE_NAME, null, cv);
			}
		}
		db.close();

	}

	@Override
	protected Class<ListSchoolsDto> getRestTransportEntityClass() {
		return ListSchoolsDto.class;
	}

	@Override
	protected String getEntityName() {
		return SchoolTableMetaData.TABLE_NAME;
	}

	@Override
	protected String getRestUrl(Intent intent) {
		return Util.getBaseUrl(getApplicationContext()) + "/schools/"
				+ getLastUpdate(intent);
	}

}
