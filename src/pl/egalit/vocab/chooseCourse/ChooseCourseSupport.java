package pl.egalit.vocab.chooseCourse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pl.egalit.vocab.model.CourseRowModel;
import pl.egalit.vocab.shared.CourseDto;

public class ChooseCourseSupport {
	static boolean isModelChanged(List<CourseRowModel> oldModel,
			List<CourseRowModel> newModel) {
		boolean changed = (oldModel == null || newModel == null)
				|| oldModel.size() != newModel.size();
		if (!changed) {
			for (int i = 0; i < oldModel.size(); i++) {
				if (!oldModel.get(i).equals(newModel.get(i))) {
					return true;
				}
			}
		}
		return changed;
	}

	static boolean areAnyModelElementsDeselected(List<CourseRowModel> oldModel,
			List<CourseRowModel> newModel) {
		if (oldModel == null && newModel == null) {
			return false;
		}
		if (oldModel.size() != newModel.size()) {
			throw new RuntimeException(
					"OldModel and NewModel should have the same size.");
		}
		for (int i = 0; i < oldModel.size(); i++) {
			CourseRowModel oldModelElement = oldModel.get(i);
			CourseRowModel newModelElement = newModel.get(i);
			if (oldModelElement.isChosen() && !newModelElement.isChosen()) {
				return true;
			}
		}
		return false;
	}

	static boolean anyModelElementChosen(Collection<CourseRowModel> model) {
		if (model != null) {
			for (CourseRowModel course : model) {
				if (course.isChosen()) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<CourseDto> getNewlyChosenCourses(
			List<CourseRowModel> oldModel, List<CourseRowModel> model) {
		List<CourseDto> elements = new ArrayList<CourseDto>();
		for (int i = 0; i < oldModel.size(); i++) {
			if (!oldModel.get(i).isChosen() && model.get(i).isChosen()) {
				elements.add(model.get(i).getData());
			}
		}
		return elements;
	}

	public static List<CourseDto> getNewlyUnChosenCourses(
			List<CourseRowModel> oldModel, List<CourseRowModel> model) {
		return getNewlyChosenCourses(model, oldModel);
	}
}
