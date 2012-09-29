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

import net.freehal.app.util.Util;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FreehalBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "LocationLoggerServiceManager";

	@Override
	public void onReceive(Context context, Intent intent) {

		// just make sure we are getting the right intent (better safe than
		// sorry)
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

			if (Util.getPreferences(context).getBoolean("startAtBoot", true)) {
				ComponentName comp = new ComponentName(context.getPackageName(),
						FreehalService.class.getName());
				ComponentName service = context.startService(new Intent().setComponent(comp));
				if (null == service) {
					// something really wrong here
					Log.e(TAG, "Could not start service " + comp.toString());
				}
			} else {
				Log.e(TAG, "I'm not allowed to start the freehal service!!");
			}

		} else {
			Log.e(TAG, "Received unexpected intent " + intent.toString());
		}
	}
}
