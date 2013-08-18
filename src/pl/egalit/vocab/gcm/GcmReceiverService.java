package pl.egalit.vocab.gcm;

import pl.egalit.vocab.R;
import pl.egalit.vocab.foundation.db.MySQLiteHelper;
import pl.egalit.vocab.foundation.providers.CourseProviderMetaData.CourseTableMetaData;
import pl.egalit.vocab.main.MainActivity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmReceiverService extends IntentService {

	static final String TAG = "GCMDemo";
	public static final int NOTIFICATION_ID = 1;

	public GcmReceiverService() {
		super("GCM Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context ctx = getApplicationContext();
		MySQLiteHelper databaseHelper = new MySQLiteHelper(ctx);
		String courseId = intent.getExtras().getString("courseId");
		Cursor c = databaseHelper.getReadableDatabase().query(
				CourseTableMetaData.TABLE_NAME,
				new String[] { CourseTableMetaData._ID },
				CourseTableMetaData.INITIALIZED + "=? AND "
						+ CourseTableMetaData.COURSE_CHOSEN + "=? AND "
						+ CourseTableMetaData._ID + "=?",
				new String[] { "1", "1", courseId }, null, null, null);
		if (c.getCount() == 1) {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);

			String messageType = gcm.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification(
						"Send error: " + intent.getExtras().toString(), ctx);
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ intent.getExtras().toString(), ctx);
			} else {
				sendNotification(
						"Lektor udostepnil slownictwo z ostatniej lekcji", ctx);
			}

		}

	}

	// Put the GCM message into a notification and post it.
	private void sendNotification(String msg, Context ctx) {
		NotificationManager mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				ctx).setSmallIcon(R.drawable.ic_stat_vokabes)
				.setContentTitle("Vokabes Nowe slowka").setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager
				.notify(NOTIFICATION_ID, mBuilder.getNotification());
	}

}
