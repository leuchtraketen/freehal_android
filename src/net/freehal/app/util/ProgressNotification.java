package net.freehal.app.util;

import net.freehal.app.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ProgressNotification implements Progress.ProgressImplementation {

	private NotificationManager notificationManager = null;
	private Notification notification = null;
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

		Activity activity = Util.getActivity();
		Context context = Util.getActivity().getApplicationContext();

		// configure the intent
		Intent intent = new Intent(activity, Util.getActivityClass());
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		// configure the notification
		notification = new Notification(R.drawable.ic_launcher, "simulating a download",
				System.currentTimeMillis());
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.progressnotification);
		notification.contentIntent = pendingIntent;
		notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_launcher);
		notification.contentView.setTextViewText(R.id.status_text, "simulation in progress");
		progress = 0;
		notification.contentView.setProgressBar(R.id.status_progress, 100, (int) progress, false);

		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(42 + generation, notification);

		thread = new Thread() {
			@Override
			public void run() {
				int myGeneration = generation;
				while (myGeneration == generation) {

					// if (enabledProgressListener) {
					double progressToPrint = (int) (progress * 1000) / (double) (1000);
					notification.contentView.setTextViewText(R.id.status_text, text + " (" + progressToPrint
							+ "%)");
					// } else {
					// notification.contentView.setTextViewText(R.id.status_text,
					// text);
					// }

					notification.contentView.setProgressBar(R.id.status_progress, 100, (int) progress, false);

					// inform the progress bar of updates in progress
					notificationManager.notify(42 + myGeneration, notification);

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();

		running = true;
	}

	public void updateProgress(double d) {
		if (!running)
			create();

		if (running)
			progress = d;
	}

	public void updateText(String text) {
		if (running)
			this.text = text;
		else
			this.text = "";
	}

	public void update(double progress, String text) {
		updateProgress(progress);
		updateText(text);
	}

	public void destroy() {
		if (running) {
			// remove the notification (we're done)
			notificationManager.cancel(42 + generation);
			++generation;
			running = false;
		}
	}
}
