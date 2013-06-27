package pl.egalit.vocab.foundation.providers;

import pl.egalit.vocab.foundation.db.MySQLiteHelper;
import android.content.ContentProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.SparseArray;

public abstract class AbstractVocabProvider extends ContentProvider {
	protected SparseArray<PendingOperation> pendingOperations = new SparseArray<PendingOperation>();
	protected Handler handler = new Handler();

	protected ResultReceiver receiver = new ResultReceiver(handler) {
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			Integer requestId = resultData.getInt("requestId");
			if (pendingOperations.get(requestId) != null) {
				pendingOperations.get(requestId).perform(resultCode == 0);
				pendingOperations.remove(requestId);
			} else {
				Log.e(getTAG(), "Missing operation requestId=!" + requestId);
			}

		}
	};

	protected static interface PendingOperation {
		public void perform(boolean emptyResultSet);
	}

	protected MySQLiteHelper databaseHelper;

	@Override
	public boolean onCreate() {
		databaseHelper = new MySQLiteHelper(getContext());
		return true;
	}

	protected abstract String getTAG();
}
