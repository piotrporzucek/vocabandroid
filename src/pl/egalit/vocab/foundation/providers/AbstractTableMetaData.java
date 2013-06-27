package pl.egalit.vocab.foundation.providers;

import android.provider.BaseColumns;

public interface AbstractTableMetaData extends BaseColumns {
	/**
	 * indicates if on the row is currently performing a transaction.
	 */
	public static final String _STATUS = "_status";
}
