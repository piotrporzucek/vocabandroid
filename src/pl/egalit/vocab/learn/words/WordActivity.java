package pl.egalit.vocab.learn.words;

import java.util.Locale;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import pl.egalit.vocab.model.Language;
import pl.egalit.vocab.model.ParcelableCourse;
import pl.egalit.vocab.shared.WordDto;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class WordActivity extends SherlockFragmentActivity implements
		TextToSpeech.OnInitListener {

	private final int MY_DATA_CHECK_CODE = 0;
	private TextToSpeech textToSpeech;
	private Language language;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ParcelableCourse pc = getIntent().getExtras().getParcelable("course");
		language = Language.getLanguageForCode(pc.getCourse().getLanguage());

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(getResources().getDrawable(
				R.drawable.vokabes_icon_small));
		setContentView(R.layout.learning_words);
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				textToSpeech = new TextToSpeech(this, this);
			} else {
				Intent installTTSIntent = new Intent();
				installTTSIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}

		}
	}

	public void answer(View view) {
		view.setVisibility(View.GONE);
		findViewById(R.id.after_anwer_buttons).setVisibility(View.VISIBLE);
		WordFragment fragment = (WordFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.wordFragment));
		((TextView) findViewById(R.id.expression)).setTextColor(getResources()
				.getColor(R.color.expressionAnswered));
		fragment.setAnswerTextVisible(true);
	}

	public void sayCurrentWord(View view) {
		WordFragment fragment = (WordFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.wordFragment));
		fragment.sayCurrentWord();
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
			((TextView) findViewById(R.id.expression))
					.setTextColor(getResources()
							.getColor(android.R.color.black));
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

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			textToSpeech.setLanguage(getLocale(language));
		}
		WordFragment fragment = WordFragment.newInstance();
		android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.replace(R.id.wordFragment, fragment);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.learningWordsLayout);
		transaction.commit();
		layout.invalidate();

	}

	private Locale getLocale(Language language) {
		for (Locale locale : Locale.getAvailableLocales()) {
			if (locale.getLanguage().equals(language.getCode())) {
				return locale;
			}
		}
		Toast.makeText(this,
				getResources().getString(R.string.not_supported_language),
				Toast.LENGTH_LONG).show();
		return Locale.UK;
	}

	@Override
	protected void onDestroy() {
		if (textToSpeech != null) {
			textToSpeech.shutdown();
		}
		super.onDestroy();

	}

	public TextToSpeech getTextToSpeech() {
		return textToSpeech;
	}

}
