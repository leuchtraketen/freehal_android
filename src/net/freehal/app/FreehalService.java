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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FreehalService extends Service {

	final static String TAG = "FreehalService";

	// The ID we use for the notification (the onscreen alert that appears at
	// the notification
	// area at the top of the screen as an icon -- and as text as well if the
	// user expands the
	// notification area).
	final int NOTIFICATION_ID = 1;

	NotificationManager mNotificationManager;
	Notification mNotification = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "debug: Creating service");

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);

		setUpAsForeground(this.getResources().getString(R.string.notification_idle));

		return START_STICKY;
	}

	private PendingIntent getPendingIntent() {
		Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
		intent.putExtra("BY_SERVICE", true);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}

	/** Updates the notification. */
	@SuppressWarnings("deprecation")
	void updateNotification(String text) {
		PendingIntent pi = this.getPendingIntent();
		mNotification.setLatestEventInfo(getApplicationContext(), "FreeHAL", text, pi);
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	}

	/**
	 * Configures service as a foreground service. A foreground service is a
	 * service that's doing something the user is actively aware of (such as
	 * playing music), and must appear to the user as a notification. That's why
	 * we create the notification here.
	 */
	@SuppressWarnings("deprecation")
	void setUpAsForeground(String text) {
		PendingIntent pi = this.getPendingIntent();
		mNotification = new Notification();
		mNotification.tickerText = text;
		mNotification.icon = R.drawable.ic_launcher;
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotification.setLatestEventInfo(getApplicationContext(), "FreeHAL", text, pi);
		startForeground(NOTIFICATION_ID, mNotification);
	}

}
