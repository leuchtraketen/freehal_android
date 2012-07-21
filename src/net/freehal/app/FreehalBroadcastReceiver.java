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
			
			if (Util.getPreferences().getBoolean("startAtBoot", true)) {
			ComponentName comp = new ComponentName(context.getPackageName(),
					FreehalService.class.getName());
			ComponentName service = context.startService(new Intent()
					.setComponent(comp));
			if (null == service) {
				// something really wrong here
				Log.e(TAG, "Could not start service " + comp.toString());
			}}
			else {
				Log.e(TAG, "I'm not allowed to start the freehal service!!");
			}
			
		} else {
			Log.e(TAG, "Received unexpected intent " + intent.toString());
		}
	}
}
