package pl.egalit.vocab.chooseCourse;

import pl.egalit.vocab.R;
import pl.egalit.vocab.model.CourseRowModel;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.CheckBox;

public class SaveCoursesDialogFragment extends DialogFragment implements
		OnClickListener {

	private ChooseCourseListFragment parentFragment;
	private CourseRowModel courseRowModel;
	private CheckBox checkBox;
	static final String SAVE_COURSES_DIALOG_TAG = "SaveCoursesConfirm";

	public static SaveCoursesDialogFragment newInstance(
			ChooseCourseListFragment chooseCourseListFragment, CheckBox cb) {
		SaveCoursesDialogFragment obj = new SaveCoursesDialogFragment();
		obj.parentFragment = chooseCourseListFragment;
		obj.checkBox = cb;
		return obj;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.saveCoursesDialog_title))
				.setPositiveButton(R.string.saveCoursesDialog_ok, this)
				.setNegativeButton(R.string.saveCoursesDialog_cancel, this)
				.setMessage(R.string.saveCoursesDialog_message);
		return builder.create();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		int style = DialogFragment.STYLE_NO_FRAME, theme = 0;
		setStyle(style, theme);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			checkBox.setChecked(true);
			dismiss();
			return;
		case DialogInterface.BUTTON_POSITIVE:
			parentFragment.saveCourses();
			dismiss();
			return;
		}

	}
}
