package pl.egalit.vocab.model;

import pl.egalit.vocab.shared.SchoolDto;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableSchool implements Parcelable {

	public static final Parcelable.Creator<ParcelableSchool> CREATOR = new Parcelable.Creator<ParcelableSchool>() {
		@Override
		public ParcelableSchool createFromParcel(final Parcel source) {
			return new ParcelableSchool(source);

		};

		@Override
		public ParcelableSchool[] newArray(int size) {
			return new ParcelableSchool[size];
		}
	};
	private String name;
	private Long id;
	private String city;

	public ParcelableSchool(SchoolDto school) {

		this.name = school.getName();
		this.id = school.getId();
		this.city = school.getCity();

	}

	public ParcelableSchool() {

	}

	public ParcelableSchool(Parcel source) {
		this.id = source.readLong();
		this.name = source.readString();
		this.city = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(city);
	}

	public SchoolDto getSchool() {

		SchoolDto school = new SchoolDto();
		school.setId(id);
		school.setName(name);
		school.setCity(city);

		return school;
	}
}
