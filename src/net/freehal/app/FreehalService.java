/*******************************************************************************
 * Copyright (c) 2006 - 2012 Tobias Schulz and Contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl.html>.
 ******************************************************************************/
package net.freehal.app;

import net.freehal.core.util.LogUtils;
import net.freehal.core.util.LogUtils.ProgressListener;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FreehalService extends Service {

	final static String TAG = "FreehalService";
	private NotificationManager mNotifyManager = null;
	private NotificationCompat2.Builder mBuilder = null;
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	private int generation = -1;
	private double max = 1;
	private double current = 0;
	private String text = null;
	private String idleText = null;

	final int NOTIFICATION_ID = 42789;

	@Override
	public void onCreate() {
		Log.i(TAG, "debug: Creating service");

		idleText = this.getResources().getString(R.string.notification_idle);

		mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat2.Builder(this);
		mBuilder.setContentTitle("FreeHAL").setContentText(idleText).setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setContentIntent(getPendingIntent());
		mBuilder.setOngoing(true);

		LogUtils.addProgressListener(new ProgressListener() {

			@Override
			public void onProgressUpdate(double current, double max, String text) {
				updateProgress(current, max, text);
			}

			@Override
			public void onProgressBeginning() {
				create();
			}

			@Override
			public void onProgressEnd() {
				Log.e("FreehalService", "destroy()!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				destroy();
			}

			@Override
			public void onSubProgressBeginning() {}

			@Override
			public void onSubProgressEnd() {}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);

		startForeground(NOTIFICATION_ID, mBuilder.build());

		return START_STICKY;
	}

	private void create() {
		++generation;
		new Thread() {
			@Override
			public void run() {
				int n = 1;
				int myGeneration = generation;
				while (myGeneration == generation) {

					mBuilder.setProgress((int) (max * 1000), (int) (current * 1000), false);
					final double progress = current / max;
					if (text != null) {
						double progressToPrint = (int) (progress * 100 * 1000) / (double) (1000);
						mBuilder.setContentText(text + " (" + progressToPrint + "%)");
						LogUtils.i("progress=" + progress + ", text=" + text + " (" + progressToPrint + "%)");
					} else
						LogUtils.i("progress=" + progress + ", text=" + text);
					mBuilder.setNumber(n++);

					// inform the progress bar of updates in progress
					mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				mBuilder.setContentText(idleText).setNumber(n++);
				mBuilder.setProgress(0, 0, false);

				// inform the progress bar of updates in progress
				mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
			}
		}.start();
	}

	private void destroy() {
		generation++;
	}

	public void updateProgress(double current, double max, String text) {
		if (max > 0) {
			this.max = max;
			this.current = current;
		}
		if (text != null)
			this.text = text;
	}

	private PendingIntent getPendingIntent() {
		Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
		intent.putExtra("BY_SERVICE", true);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public FreehalService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return FreehalService.this;
		}
	}
}
