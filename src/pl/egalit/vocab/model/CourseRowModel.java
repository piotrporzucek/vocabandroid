package pl.egalit.vocab.model;

import pl.egalit.vocab.shared.CourseDto;

public class CourseRowModel {
	private boolean chosen;
	int position;
	private String name;
	private long id;

	public CourseRowModel(CourseDto course, int position) {
		this.name = course.getName();
		this.position = position;
		this.chosen = course.isChosen();
		this.id = course.getId();
	}

	public boolean isChosen() {
		return chosen;
	}

	public void setChosen(boolean chosen) {
		this.chosen = chosen;
	}

	public String getName() {
		return name;
	}

	public CourseDto getData() {
		CourseDto dto = new CourseDto();
		dto.setId(id);
		dto.setName(name);
		return dto;
	}

	public int getPosition() {
		return position;
	}
}