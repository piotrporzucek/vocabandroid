package pl.egalit.vocab.foundation.db;

import java.io.File;

import pl.egalit.vocab.foundation.providers.CourseProviderMetaData;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData.WordTableMetaData;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

@SuppressLint({ "ParserError", "ParserError" })
public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = Environment
			.getExternalStorageDirectory() + File.separator + "vocab.db";
	private static final int DATABASE_VERSION = 4;

	private static final String DATABASE_CREATE_COURSES_TABLE = "create table "
			+ CourseProviderMetaData.COURSES_TABLE_NAME + "("
			+ CourseTableMetaData._ID + " integer primary key, "
			+ CourseTableMetaData._STATUS + " text,"
			+ CourseTableMetaData.START_DATE + " text,"
			+ CourseTableMetaData.END_DATE + " text,"
			+ CourseTableMetaData.COURSE_NAME + " text unique not null, "
			+ CourseTableMetaData.PASSWORD + " text not null, "
			+ CourseTableMetaData.COURSE_DESCRIPTION + " test,"
			+ CourseTableMetaData.COURSE_CHOSEN
			+ " integer not null default 0," + CourseTableMetaData.ACTIVE
			+ " integer not null default 1," + CourseTableMetaData.INITIALIZED
			+ " integer not null default 0)";

	private static final String DATABASE_CREATE_UPDATE_STATUS_TABLE = "create table "
			+ "UPDATE_STATUS"
			+ "("
			+ "ENTITY"
			+ " text, "
			+ "ENTITY_ID long, "
			+ "LASTUPDATE" + " long)";

	private static final String DATABASE_CREATE_WORDS_TABLE = "create table "
			+ WordProviderMetaData.WORDS_TABLE_NAME + "("
			+ WordTableMetaData._ID + " integer primary key, "
			+ WordTableMetaData.WORD_LAST_SHOWN_ON + " text,"
			+ WordTableMetaData.WORD_NEXT_SHOW_ON + " text,"
			+ WordTableMetaData.WORD_EXAMPLE + " text,"
			+ WordTableMetaData._COURSE_ID + " integer not null, "
			+ WordTableMetaData._STATUS + " text,"
			+ WordTableMetaData.WORD_EXPRESSION + " text not null, "
			+ WordTableMetaData.WORD_ANSWER + " text not null, "
			+ "FOREIGN KEY (" + WordTableMetaData._COURSE_ID + ") REFERENCES "
			+ CourseProviderMetaData.COURSES_TABLE_NAME + " ("
			+ CourseProviderMetaData.CourseTableMetaData._ID + "))";

	private static final String DATABASE_DROP_COURSES = "DROP TABLE IF EXISTS "
			+ CourseProviderMetaData.COURSES_TABLE_NAME;
	private static final String DATABASE_DROP_UPDATE_STATUS = "DROP TABLE IF EXISTS "
			+ "UPDATE_STATUS";
	private static final String DATABASE_DROP_WORDS = "DROP TABLE IF EXISTS "
			+ WordTableMetaData.TABLE_NAME;

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		if (!database.isReadOnly()) {
			database.execSQL("PRAGMA foreign_keys=ON;");
		}
		database.execSQL(DATABASE_CREATE_COURSES_TABLE);
		database.execSQL(DATABASE_CREATE_UPDATE_STATUS_TABLE);
		database.execSQL(DATABASE_CREATE_WORDS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL(DATABASE_DROP_WORDS);
		db.execSQL(DATABASE_DROP_COURSES);
		db.execSQL(DATABASE_DROP_UPDATE_STATUS);
		onCreate(db);
	}

}
