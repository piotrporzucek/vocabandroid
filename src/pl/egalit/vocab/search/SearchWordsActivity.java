package pl.egalit.vocab.search;

import java.util.Locale;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData;
import pl.egalit.vocab.foundation.providers.WordProviderMetaData.WordTableMetaData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class SearchWordsActivity extends SherlockFragmentActivity implements
		LoaderCallbacks<Cursor>, TextToSpeech.OnInitListener {

	private SimpleCursorAdapter adapter;
	private TextToSpeech myTTS;
	private final int MY_DATA_CHECK_CODE = 0;
	private String language;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(
				getResources().getDrawable(R.drawable.vokabes_icon_small));
		setContentView(R.layout.search_results);
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if ("pl.egalit.vokabes.SEARCH".equals(intent.getAction())) {
			String query = intent.getStringExtra("query");
			doMySearch(query);
		}
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

	}

	private void doMySearch(String query) {
		String[] from = new String[] { WordTableMetaData.WORD_EXPRESSION,
				WordTableMetaData.WORD_ANSWER, WordTableMetaData.WORD_EXAMPLE,
				WordTableMetaData.WORD_EXPRESSION };
		int[] to = new int[] { R.id.result_expression, R.id.result_answer,
				R.id.result_example, R.id.result_sound };
		adapter = new SearchWordsAdapter(this, R.layout.search_results_row,
				null, from, to, 0);
		ListView listView = (ListView) findViewById(R.id.search_results_list);
		listView.setAdapter(adapter);
		Bundle bundle = new Bundle();
		bundle.putString("query", query);
		getSupportLoaderManager().initLoader(0, bundle, this);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				myTTS = new TextToSpeech(this, this);
			} else {
				Intent installTTSIntent = new Intent();
				installTTSIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (loaderId == 0) {
			Uri uri = Uri.withAppendedPath(
					WordProviderMetaData.CONTENT_SEARCH_WORDS_URI,
					bundle.getString("query"));
			return new CursorLoader(this, uri, new String[] {
					WordTableMetaData._ID, WordTableMetaData.WORD_EXPRESSION,
					WordTableMetaData.WORD_ANSWER,
					WordTableMetaData.WORD_EXAMPLE }, null, null,
					WordTableMetaData.WORD_EXPRESSION + " ASC");
		}

		return null;

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (loader.getId() == 0) {
			if (c.getCount() == 0) {
				findViewById(R.id.search_results_list).setVisibility(View.GONE);
				findViewById(R.id.search_no_results)
						.setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.search_no_results).setVisibility(View.GONE);
				adapter.swapCursor(c);
			}

		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);

	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			myTTS.setLanguage(getLocale(language));
		}

	}

	public void sayCurrentWord(View view) {
		myTTS.speak(view.getTag().toString(), TextToSpeech.QUEUE_FLUSH, null);
	}

	private Locale getLocale(String language) {
		for (Locale locale : Locale.getAvailableLocales()) {
			if (locale.getLanguage().equals(language)) {
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
		if (myTTS != null) {
			myTTS.shutdown();
		}
		super.onDestroy();

	}

}
