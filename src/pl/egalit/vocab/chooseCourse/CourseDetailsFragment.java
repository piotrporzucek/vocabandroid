package pl.egalit.vocab.chooseCourse;

import pl.egalit.vocab.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class CourseDetailsFragment extends SherlockFragment {
	private int courseId = -1;

	public static CourseDetailsFragment newInstance(int courseId) {
		CourseDetailsFragment fragment = new CourseDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("courseId", courseId);
		fragment.setArguments(bundle);
		return fragment;

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		setHasOptionsMenu(true);
	}

	public int getCourseId() {
		return courseId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			courseId = getArguments().getInt("courseId", -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.choose_course_details, container,
				false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		TextView textview = (TextView) getActivity().findViewById(
				R.id.course_detail);
		if (textview == null) {
			Toast.makeText(getActivity(), "Text View is null. Whooops.",
					Toast.LENGTH_LONG);
		} else {
			textview.setText("I'm course: " + courseId);
		}

	}

	public static CourseDetailsFragment newInstance(Bundle extras) {
		return newInstance(extras.getInt("courseId"));
	}
}
