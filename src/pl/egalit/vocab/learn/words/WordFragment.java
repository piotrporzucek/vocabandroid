package pl.egalit.vocab.learn.words;

import pl.egalit.vocab.R;
import pl.egalit.vocab.shared.WordDto;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WordFragment extends Fragment {

	private WordsAdapter adapterNewWords;
	private WordsAdapter currentAdapter;
	private WordFragmentView view;

	public WordFragment(WordsAdapter adapter) {
		this.currentAdapter = adapter;
	}

	public static WordFragment newInstance(WordsAdapter adapter) {
		return new WordFragment(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = new WordFragmentView(getActivity());
		return view;
	}

	public void setAnswerTextVisible(boolean isVisible) {
		getView().findViewById(R.id.answer).setVisibility(
				isVisible ? View.VISIBLE : View.INVISIBLE);
	}

	public boolean moveToNextWord() {
		setAnswerTextVisible(false);

		boolean hasNextWord = currentAdapter.getCursor().moveToNext();
		if (!hasNextWord && currentAdapter != adapterNewWords
				&& adapterNewWords.getCount() > 0) {
			currentAdapter = adapterNewWords;
			view.setAdapter(currentAdapter);
			return true;
		} else if (!hasNextWord
				&& (currentAdapter == adapterNewWords || adapterNewWords
						.getCount() == 0)) {
			return false;
		} else {
			view.moveToNextWord();
			return true;
		}

	}

	public void setNewWordsAdapter(WordsAdapter adapterNewWords) {
		this.adapterNewWords = adapterNewWords;
		if (view.getAdapter() == null && adapterNewWords.getCount() > 0) {
			view.setAdapter(adapterNewWords);
			currentAdapter = adapterNewWords;
		}

	}

	public void setRepeatsAdapter(WordsAdapter adapterRepeats) {
		this.currentAdapter = adapterRepeats;
		if (currentAdapter.getCursor().getCount() > 0) {
			view.setAdapter(currentAdapter);
		}

	}

	public WordDto getCurrentWord() {
		WordDto word = new WordDto();
		word.setId(currentAdapter.getCursor().getLong(3));
		return word;
	}
}