package pl.egalit.vocab.learn.words;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData.WordTableMetaData;
import pl.egalit.vocab.shared.WordDto;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WordFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private WordsAdapter adapterNewWords;
	private WordsAdapter currentAdapter;
	private WordFragmentView view;
	private WordsAdapter adapterRepeats;

	public static WordFragment newInstance() {
		return new WordFragment();

	}

	public WordFragment() {

	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Long courseId = bundle.getLong("courseId");
		if (loaderId == 0) {

			return new CursorLoader(getActivity(),
					Uri.parse("content://" + WordProviderMetaData.AUTHORITY
							+ "/words/new/" + courseId), new String[] {
							WordTableMetaData.WORD_EXPRESSION,
							WordTableMetaData.WORD_ANSWER,
							WordTableMetaData.WORD_EXAMPLE,
							WordTableMetaData._ID }, null, null, null);
		} else if (loaderId == 1) {

			return new CursorLoader(getActivity(), Uri.parse("content://"
					+ WordProviderMetaData.AUTHORITY + "/words/repeats/"
					+ courseId), new String[] {
					WordTableMetaData.WORD_EXPRESSION,
					WordTableMetaData.WORD_ANSWER,
					WordTableMetaData.WORD_EXAMPLE, WordTableMetaData._ID, },
					null, null, null);
		}
		return null;

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (loader.getId() == 0) {
			adapterNewWords.swapCursor(c);
			checkIfAreAnyWord();
			setNewWordsAdapter(adapterNewWords);
		} else if (loader.getId() == 1) {
			adapterRepeats.swapCursor(c);
			setRepeatsAdapter(adapterRepeats);
			getActivity().getSupportLoaderManager().initLoader(0,
					getActivity().getIntent().getExtras(), this);
		}
		if (currentAdapter.getCursor().getCount() > 0) {
			currentAdapter.getCursor().moveToFirst();
			sayCurrentWord();
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == 0) {
			adapterNewWords.swapCursor(null);
		} else if (loader.getId() == 1) {
			adapterRepeats.swapCursor(null);
		}

	}

	private boolean checkIfAreAnyWord() {
		if (adapterNewWords.getCount() == 0 && adapterRepeats.getCount() == 0) {
			getActivity().finish();
			return false;
		}
		return true;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapterNewWords = new WordsAdapter(getActivity(), null);
		adapterRepeats = new WordsAdapter(getActivity(), null);
		getActivity().getSupportLoaderManager().initLoader(1,
				getActivity().getIntent().getExtras(), this);
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
		getView().findViewById(R.id.answerText).setVisibility(
				isVisible ? View.VISIBLE : View.INVISIBLE);
	}

	public boolean moveToNextWord() {
		setAnswerTextVisible(false);

		boolean hasNextWord = currentAdapter.getCursor().moveToNext();
		if (!hasNextWord && currentAdapter != adapterNewWords
				&& adapterNewWords.getCount() > 0) {
			currentAdapter = adapterNewWords;
			view.setAdapter(currentAdapter);
			sayCurrentWord();
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

	public void sayCurrentWord() {
		WordActivity activity = (WordActivity) getActivity();
		activity.getTextToSpeech().speak(getCurrentWord().getExpression(),
				TextToSpeech.QUEUE_FLUSH, null);
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
		word.setExpression(currentAdapter.getCursor().getString(0));
		return word;
	}
}