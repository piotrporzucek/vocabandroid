package pl.egalit.vocab;

import pl.egalit.vocab.gcm.GcmReceiverService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Handling of GCM messages.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intentService = new Intent(context, GcmReceiverService.class);
		intentService.putExtra("courseId",
				intent.getExtras().getString("courseId"));
		context.startService(intentService);
	}

}