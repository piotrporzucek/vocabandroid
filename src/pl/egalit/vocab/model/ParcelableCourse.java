package pl.egalit.vocab.model;

import pl.egalit.vocab.shared.CourseDto;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableCourse implements Parcelable {

	public static final Parcelable.Creator<CourseDto> CREATOR = new Parcelable.Creator<CourseDto>() {
		public CourseDto createFromParcel(final Parcel source) {
			CourseDto course = new CourseDto();
			course.setId(source.readLong());
			course.setName(source.readString());

			return course;

		};

		@Override
		public CourseDto[] newArray(int size) {
			return new CourseDto[size];
		}
	};
	private String name;
	private Long id;
	private boolean chosen;
	private CourseDto course;

	public ParcelableCourse(CourseDto course) {

		this.name = course.getName();
		this.id = course.getId();
		this.course = course;
	}

	public ParcelableCourse() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeByte(chosen ? (byte) 1 : 0);
	}

	public CourseDto getCourse() {
		if (course == null) {
			course = new CourseDto();
			course.setId(id);
			course.setName(name);
		}
		return course;

	}

}
