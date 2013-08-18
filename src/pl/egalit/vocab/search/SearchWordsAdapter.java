package pl.egalit.vocab.search;

import pl.egalit.vocab.R;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class SearchWordsAdapter extends SimpleCursorAdapter implements
		ViewBinder {

	public SearchWordsAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		setViewBinder(this);
	}

	@Override
	public boolean setViewValue(View view, Cursor c, int arg2) {
		if (view instanceof ImageButton) {
			view.setTag(c.getString(1));
			return true;
		} else if (view.getId() == R.id.result_example) {
			TextView exampleView = (TextView) view;
			String exampleText = c.getString(3);
			String expressionText = c.getString(1);
			if (exampleText.contains(expressionText)) {
				String html = "<b>" + expressionText + "</b>";
				Spanned spanned = Html.fromHtml(exampleText.replaceAll(
						expressionText, html));
				exampleView.setText(spanned);
			} else {
				exampleView.setText(exampleText);
			}
			return true;

		}
		return false;
	}
}
