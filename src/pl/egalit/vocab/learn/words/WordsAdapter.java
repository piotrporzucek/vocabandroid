package pl.egalit.vocab.learn.words;

import pl.egalit.vocab.R;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WordsAdapter extends CursorAdapter {

	public WordsAdapter(Context context, Cursor c) {
		super(context, c, 0);

	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
		TextView expression = (TextView) view.findViewById(R.id.expression);
		TextView answer = (TextView) view.findViewById(R.id.answerText);
		TextView example = (TextView) view.findViewById(R.id.answerExample);
		expression.setText(c.getString(0));
		answer.setText(c.getString(1));
		example.setText(c.getString(2));
	}

	public void refreshView(View view, Context context, int position) {
		getCursor().moveToPosition(position);
		bindView(view, context, getCursor());
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return LayoutInflater.from(mContext).inflate(
				R.layout.learning_words_content, arg2, false);
	}

	@Override
	protected void onContentChanged() {
		super.onContentChanged();

	}

}
