package pl.egalit.vocab.model;

import pl.egalit.vocab.shared.CourseDto;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableCourse implements Parcelable {

	public static final Parcelable.Creator<ParcelableCourse> CREATOR = new Parcelable.Creator<ParcelableCourse>() {
		@Override
		public ParcelableCourse createFromParcel(final Parcel source) {
			return new ParcelableCourse(source);

		};

		@Override
		public ParcelableCourse[] newArray(int size) {
			return new ParcelableCourse[size];
		}
	};
	private String name;
	private Long id;
	private boolean chosen;
	private String language;

	public ParcelableCourse(CourseDto course) {

		this.name = course.getName();
		this.id = course.getId();
		this.language = course.getLanguage();

	}

	public ParcelableCourse() {

	}

	public ParcelableCourse(Parcel source) {
		this.id = source.readLong();
		this.name = source.readString();
		this.chosen = source.readByte() != 0;
		this.language = source.readString();
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
		dest.writeString(language);
	}

	public CourseDto getCourse() {
		CourseDto course = new CourseDto();
		course.setId(id);
		course.setName(name);
		course.setLanguage(language);
		return course;

	}

}
