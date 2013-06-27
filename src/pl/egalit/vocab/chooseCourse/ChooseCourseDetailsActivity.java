package pl.egalit.vocab.chooseCourse;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ChooseCourseDetailsActivity extends SherlockFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& getResources().getConfiguration().screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			finish();
			return;
		}
		if (getIntent() != null) {
			CourseDetailsFragment details = CourseDetailsFragment
					.newInstance(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, details).commit();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, ChooseCourseActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
}
