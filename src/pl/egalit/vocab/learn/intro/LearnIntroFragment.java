package pl.egalit.vocab.learn.intro;

import pl.egalit.vocab.R;
import pl.egalit.vocab.model.ParcelableCourse;
import pl.egalit.vocab.shared.CourseDto;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LearnIntroFragment extends Fragment {
	private static final String COURSE_PROPERTY = "course";

	public static LearnIntroFragment init(CourseDto course) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(COURSE_PROPERTY, new ParcelableCourse(course));
		LearnIntroFragment f = new LearnIntroFragment();
		f.setArguments(bundle);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.learn_intro_content, container,
				false);

		ParcelableCourse pc = getArguments().getParcelable(COURSE_PROPERTY);
		TextView newWordsText = (TextView) view
				.findViewById(R.id.learn_intro_new_words);
		TextView repeatsText = (TextView) view
				.findViewById(R.id.learn_intro_repeats);
		LearnIntroActivity activity = (LearnIntroActivity) getActivity();
		activity.registerForNewWordsCount(newWordsText);
		activity.registerForRepeatsCount(repeatsText);

		return view;
	}

}
