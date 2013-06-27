package pl.egalit.vocab.learn.words;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData.WordTableMetaData;
import pl.egalit.vocab.shared.WordDto;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class WordActivity extends SherlockFragmentActivity implements
		LoaderCallbacks<Cursor> {

	private WordsAdapter adapterNewWords;
	private WordsAdapter adapterRepeats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.learning_words);
		adapterNewWords = new WordsAdapter(this, null);
		adapterRepeats = new WordsAdapter(this, null);
		getSupportLoaderManager().initLoader(1, getIntent().getExtras(), this);
		WordFragment fragment = WordFragment.newInstance(adapterNewWords);
		android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.replace(R.id.wordFragment, fragment);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.learningWordsLayout);
		transaction.commit();
		layout.invalidate();

	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Long courseId = bundle.getLong("courseId");
		if (loaderId == 0) {

			return new CursorLoader(this,
					Uri.parse("content://" + WordProviderMetaData.AUTHORITY
							+ "/words/new/" + courseId), new String[] {
							WordTableMetaData.WORD_EXPRESSION,
							WordTableMetaData.WORD_ANSWER,
							WordTableMetaData.WORD_EXAMPLE,
							WordTableMetaData._ID }, null, null, null);
		} else if (loaderId == 1) {

			return new CursorLoader(this, Uri.parse("content://"
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
			WordFragment fragment = (WordFragment) (getSupportFragmentManager()
					.findFragmentById(R.id.wordFragment));
			if (fragment != null) {
				fragment.setNewWordsAdapter(adapterNewWords);
			}

		} else if (loader.getId() == 1) {
			adapterRepeats.swapCursor(c);
			WordFragment fragment = (WordFragment) (getSupportFragmentManager()
					.findFragmentById(R.id.wordFragment));
			if (fragment != null) {
				fragment.setRepeatsAdapter(adapterRepeats);
			}
			getSupportLoaderManager().initLoader(0, getIntent().getExtras(),
					this);
		}
	}

	private void checkIfAreAnyWord() {
		if (adapterNewWords.getCount() == 0 && adapterRepeats.getCount() == 0) {
			finish();
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

	public void answer(View view) {
		view.setVisibility(View.GONE);
		findViewById(R.id.after_anwer_buttons).setVisibility(View.VISIBLE);
		WordFragment fragment = (WordFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.wordFragment));
		fragment.setAnswerTextVisible(true);
	}

	public void badAnswer(View view) {
		updateWordStatus(WordAnswerEnum.NOIDEA, getCurrentWord());
		afterAnswer();
	}

	public void unsureAnswer(View view) {
		updateWordStatus(WordAnswerEnum.UNSURE, getCurrentWord());
		afterAnswer();
	}

	public void goodAnswer(View view) {
		updateWordStatus(WordAnswerEnum.WELL, getCurrentWord());
		afterAnswer();
	}

	private WordDto getCurrentWord() {
		WordFragment fragment = (WordFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.wordFragment));
		return fragment.getCurrentWord();
	}

	private void afterAnswer() {
		WordFragment fragment = (WordFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.wordFragment));
		boolean hasNextWord = fragment.moveToNextWord();
		if (hasNextWord) {
			findViewById(R.id.after_anwer_buttons).setVisibility(View.GONE);
			findViewById(R.id.answer_button).setVisibility(View.VISIBLE);
		} else {
			finish();
		}

	}

	private void updateWordStatus(WordAnswerEnum wordAnswerStatus, WordDto word) {

		Uri uri = Uri.parse("content://" + WordProviderMetaData.AUTHORITY
				+ "/words/state/");
		uri = ContentUris.withAppendedId(uri, word.getId());
		uri = ContentUris.withAppendedId(uri, wordAnswerStatus.ordinal());
		new WordStateUpdater().execute(uri);

	}

	private class WordStateUpdater extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... params) {
			Uri uri = params[0];
			getContentResolver().update(uri, null, null, null);
			return null;
		}

	}

}
