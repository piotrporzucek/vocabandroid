package pl.egalit.vocab.chooseCourse.service;

import java.text.SimpleDateFormat;
import java.util.List;

import pl.egalit.vocab.Setup;
import pl.egalit.vocab.Util;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.foundation.service.AbstractEntityService;
import pl.egalit.vocab.shared.CourseDto;
import pl.egalit.vocab.shared.ListCoursesDto;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class CourseService extends
		AbstractEntityService<ListCoursesDto, CourseDto> {

	public CourseService() {
		super("Course Service");
	}

	@Override
	protected void updateEntities(ListCoursesDto coursesAnswer) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		updateActiveCourses(db, coursesAnswer.getActiveCourses());
		archiveNotReceivedCourses(db,
				coursesAnswer.getArchiveCoursesFromLastUpdate());

	}

	private void updateActiveCourses(SQLiteDatabase db, List<CourseDto> list) {
		for (CourseDto course : list) {
			ContentValues cv = prepareContentValuesForUpdate(course);
			int count = db.update(CourseTableMetaData.TABLE_NAME, cv,
					CourseTableMetaData._ID + "=?", new String[] { course
							.getId().toString() });
			if (count == 0) {
				cv.put(CourseTableMetaData.ACTIVE, course.isActive());
				db.insert(CourseTableMetaData.TABLE_NAME, null, cv);
			}
		}
	}

	private void archiveNotReceivedCourses(SQLiteDatabase db,
			List<CourseDto> archiveCoursesFromLastUpdate) {
		for (CourseDto course : archiveCoursesFromLastUpdate) {
			ContentValues cv = prepareContentValuesForUpdate(course);

			db.update(CourseTableMetaData.TABLE_NAME, cv,
					CourseTableMetaData._ID + "=? AND "
							+ CourseTableMetaData.ACTIVE + "=1",
					new String[] { course.getId().toString() });

		}

	}

	private ContentValues prepareContentValuesForUpdate(CourseDto course) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ContentValues cv = new ContentValues();
		cv.put(CourseTableMetaData.COURSE_DESCRIPTION, course.getDescription());
		cv.put(CourseTableMetaData.COURSE_NAME, course.getName());
		cv.put(CourseTableMetaData.START_DATE,
				dateFormat.format(course.getStartDate()));
		cv.put(CourseTableMetaData.END_DATE,
				dateFormat.format(course.getEndDate()));
		cv.put(CourseTableMetaData.PASSWORD, course.getPassword());
		cv.put(CourseTableMetaData._ID, course.getId());
		cv.put(CourseTableMetaData.ACTIVE, course.isActive() ? "1" : "0");
		return cv;
	}

	@Override
	protected Class<ListCoursesDto> getRestTransportEntityClass() {
		return ListCoursesDto.class;
	}

	@Override
	protected String getEntityName() {
		return CourseTableMetaData.TABLE_NAME;
	}

	@Override
	protected String getRestUrl(Intent intent) {
		return Util.getBaseUrl(getApplicationContext()) + "/courses/"
				+ Setup.SCHOOL_ID + "/" + getLastUpdate(intent);
	}

}
