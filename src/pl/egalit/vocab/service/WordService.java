package pl.egalit.vocab.service;

import java.text.SimpleDateFormat;

import pl.egalit.vocab.Setup;
import pl.egalit.vocab.Util;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData.WordTableMetaData;
import pl.egalit.vocab.foundation.service.AbstractEntityService;
import pl.egalit.vocab.shared.ListWordsDto;
import pl.egalit.vocab.shared.WordDto;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class WordService extends AbstractEntityService<ListWordsDto, WordDto> {

	public WordService() {
		super("Word Service");
	}

	@Override
	protected void updateEntities(ListWordsDto words) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		for (WordDto word : words) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			ContentValues cv = new ContentValues();
			cv.put(WordTableMetaData._COURSE_ID, word.getCourseId());
			cv.put(WordTableMetaData.WORD_ANSWER, word.getAnswer());
			cv.put(WordTableMetaData.WORD_EXPRESSION, word.getExpression());
			cv.put(WordTableMetaData.WORD_EXAMPLE, word.getExample());
			if (word.getLastShownOn() != null) {
				cv.put(WordTableMetaData.WORD_LAST_SHOWN_ON,
						dateFormat.format(word.getLastShownOn()));
			}
			if (word.getNextShownOn() != null) {
				cv.put(WordTableMetaData.WORD_NEXT_SHOW_ON,
						dateFormat.format(word.getNextShownOn()));
			}
			int count = db.update(WordTableMetaData.TABLE_NAME, cv,
					WordTableMetaData._ID + "=?", new String[] { word.getId()
							.toString() });
			if (count == 0) {
				db.insert(WordTableMetaData.TABLE_NAME, null, cv);
			}
		}
		db.close();

	}

	@Override
	protected Class<ListWordsDto> getRestTransportEntityClass() {
		return ListWordsDto.class;
	}

	@Override
	protected String getEntityName() {
		return WordTableMetaData.TABLE_NAME;
	}

	@Override
	protected String getRestUrl(Intent intent) {
		String courseId = getParameter(intent, "entityId", "-1");
		return Util.getBaseUrl(getApplicationContext()) + "/school/"
				+ Setup.getSchoolId(getApplicationContext()) + "/course/"
				+ courseId + "/words/" + getLastUpdate(intent);
	}

}
