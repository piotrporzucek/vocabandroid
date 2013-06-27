package pl.egalit.vocab.learn.words;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

public class WordFragmentView extends AdapterView<WordsAdapter> {
	private WordsAdapter adapter;
	private int position = 0;

	public WordFragmentView(Context context) {
		super(context);
	}

	@Override
	public View getSelectedView() {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void setSelection(int position) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public WordsAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void setAdapter(WordsAdapter adapter) {
		this.adapter = adapter;
		position = 0;
		removeAllViewsInLayout();
		requestLayout();
	}

	public void moveToNextWord() {
		position++;
		requestLayout();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// if we don't have an adapter, we don't need to do anything
		if (adapter == null || adapter.getCursor() == null) {
			return;
		}

		if (getChildCount() == 0) {
			View newBottomChild = adapter.getView(position, null, this);
			addAndMeasureChild(newBottomChild);
		} else {
			adapter.refreshView(getChildAt(0), getContext(), position);
		}

		positionItems();
	}

	/**
	 * Adds a view as a child view and takes care of measuring it
	 * 
	 * @param child
	 *            The view to add
	 */
	private void addAndMeasureChild(View child) {
		LayoutParams params = child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}
		addViewInLayout(child, -1, params, true);

		int itemWidth = getWidth();
		child.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.UNSPECIFIED);
	}

	/**
	 * Positions the children at the &quot;correct&quot; positions
	 */
	private void positionItems() {
		int top = 0;

		for (int index = 0; index < getChildCount(); index++) {
			View child = getChildAt(index);

			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();
			int left = (getWidth() - width) / 2;

			child.layout(left, top, left + width, top + height);
			top += height;
		}
	}
}
