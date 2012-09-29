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
package net.freehal.app.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class VoiceRecHelper {

	public static final int REQUEST_CODE = 1337;

	private static ResultHook resultHook;

	public static boolean hasVoiceRecognition(Context context) {

		// Disable button if no recognition service is present
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		return activities.size() > 0;
	}

	public static void start(SherlockFragmentActivity activity, Context appContext) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "FreeHAL Speech Recognition");
		activity.startActivityForResult(intent, REQUEST_CODE);
	}

	public static void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (resultHook != null) {
				resultHook.onVoiceResult(matches);
			}
		}
	}

	public static void setResultHook(ResultHook resultHook) {
		VoiceRecHelper.resultHook = resultHook;
	}

	public static abstract interface ResultHook {
		public abstract void onVoiceResult(List<String> list);
	};
}
