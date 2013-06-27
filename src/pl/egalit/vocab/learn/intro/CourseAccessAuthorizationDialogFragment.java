package pl.egalit.vocab.learn.intro;

import org.springframework.util.StringUtils;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.OnDialogDoneListener;
import pl.egalit.vocab.foundation.security.HashGenerator;
import pl.egalit.vocab.shared.CourseDto;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CourseAccessAuthorizationDialogFragment extends DialogFragment
		implements View.OnClickListener {

	public static final String COURSE_ACCESS_AUTH_DIALOG_TAG = "CourseAccessAuthorizationDialog";

	private EditText et;

	private String passwordHash;

	public static CourseAccessAuthorizationDialogFragment newInstance(
			CourseDto course) {
		CourseAccessAuthorizationDialogFragment obj = new CourseAccessAuthorizationDialogFragment();
		obj.setPasswordHash(course.getPassword());
		return obj;
	}

	@Override
	public void onAttach(Activity activity) {

		try {
			@SuppressWarnings("unused")
			OnDialogDoneListener test = (OnDialogDoneListener) activity;

		} catch (ClassCastException ex) {
			Log.e(COURSE_ACCESS_AUTH_DIALOG_TAG,
					"Activity should implement OnDialogDone.");
		}
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.course_authorization_prompt,
				container, false);
		getDialog().setTitle(R.string.enterCoursePassword);
		Button dismissBtn = (Button) view.findViewById(R.id.btn_dismiss);
		dismissBtn.setOnClickListener(this);
		Button checkBtn = (Button) view.findViewById(R.id.btn_check);
		checkBtn.setOnClickListener(this);
		et = (EditText) view.findViewById(R.id.inputtext);
		if (savedInstanceState != null) {
			et.setText(savedInstanceState.getString("input"));
		}
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putCharSequence("input", et.getText());
		arg0.putString("passwordHash", passwordHash);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		int style = DialogFragment.STYLE_NORMAL, theme = 0;
		setStyle(style, theme);
		if (savedInstanceState != null) {
			passwordHash = savedInstanceState.getString("passwordHash");
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_dismiss) {
			dismiss();
			return;
		} else if (v.getId() == R.id.btn_check) {
			if (isPasswordCorrect(et.getText().toString(), passwordHash)) {
				dismiss();
				((OnDialogDoneListener) getActivity()).onDialogDone(
						COURSE_ACCESS_AUTH_DIALOG_TAG, false);
			} else {
				Toast.makeText(
						getActivity(),
						getResources().getString(
								R.string.incorrectCoursePassword),
						Toast.LENGTH_LONG).show();
			}

		}

	}

	private boolean isPasswordCorrect(String text, String passwordHash) {
		return (StringUtils.hasText(text) && passwordHash.equals(HashGenerator
				.getHash(text)));
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

}
