package net.freehal.app.util;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import net.freehal.app.R;
import net.freehal.core.util.LogUtils;
import net.freehal.core.util.LogUtils.ProgressListener;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ProgressNotification {

	private NotificationManager mNotifyManager = null;
	private NotificationCompat2.Builder mBuilder = null;
	private Thread thread = null;

	private double progress = 0;
	private String text = "";

	private boolean running = false;
	private int generation = -1;

	public ProgressNotification() {}

	@SuppressWarnings("deprecation")
	public void create() {
		if (running) {
			running = false;
			destroy();
		}

		// create a new notification!
		++generation;

		// configure the intent
		Intent intent = new Intent(Util.getActivity(), Util.getActivityClass());
		final PendingIntent pendingIntent = PendingIntent.getActivity(Util.getActivity()
				.getApplicationContext(), 0, intent, 0);

		// configure the notification

		mNotifyManager = (NotificationManager) Util.getActivity().getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat2.Builder(Util.getActivity().getApplicationContext());
		mBuilder.setContentTitle("FreeHAL Database").setContentText("Update in progress")
				.setSmallIcon(R.drawable.ic_launcher);

		thread = new Thread() {
			@Override
			public void run() {
				int myGeneration = generation;
				while (myGeneration == generation) {

					mBuilder.setProgress(100 * 1000, ((int) progress * 1000), false);
					if (text != null) {
						double progressToPrint = (int) (progress * 100 * 1000) / (double) (1000);
						text += " (" + progressToPrint + "%)";
						mBuilder.setContentText(text);
						LogUtils.i("progress=" + progress + ", text=" + text);
					} else
						LogUtils.i("progress=" + progress + ", text=" + text);

					// inform the progress bar of updates in progress
                    mNotifyManager.notify(42789, mBuilder.build());

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();

		LogUtils.addProgressListener(new ProgressListener() {

			@Override
			public void onProgressBeginning() {}

			@Override
			public void onProgressEnd() {}

			@Override
			public void onProgressUpdate(double current, double max, String text) {
				updateProgress(current, max, text);
			}
		});

		running = true;
	}

	public void updateProgress(double current, double max, String text) {
		if (!running)
			create();

		if (running) {
			progress = current / max;
			this.text = text;
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}

	public void destroy() {
		if (running) {
			// remove the notification (we're done)
			mNotifyManager.cancel(42789);
			++generation;
			running = false;
		}
	}
}
