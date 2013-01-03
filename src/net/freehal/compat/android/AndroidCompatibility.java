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
package net.freehal.compat.android;

import java.util.Locale;

import net.freehal.app.util.AndroidUtils;
import net.freehal.core.lang.Language;
import net.freehal.core.lang.english.EnglishLanguage;
import net.freehal.core.lang.german.GermanLanguage;
import net.freehal.core.util.FreehalFile;
import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class AndroidCompatibility {

	private static final String TAG = "AndroidCompatibility";

	public static Language getLanguage() {
		final String language = Locale.getDefault().getLanguage();
		Log.i(TAG, "language: " + language);
		if (language.equals("de"))
			return new GermanLanguage();
		else
			return new EnglishLanguage();
	}

	@SuppressLint("ShowToast")
	public static FreehalFile getPath() {
		FreehalFile path;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			path = new FreehalFile(Environment.getExternalStorageDirectory().getPath());
			path.mkdirs();
		} else {
			Toast.makeText(AndroidUtils.getActivity().getApplicationContext(), "No SD card found!",
					Toast.LENGTH_LONG).show();
			path = new FreehalFile(AndroidUtils.getActivity().getApplicationContext().getCacheDir().getPath());
			path.mkdirs();
		}
		path = path.getChild("freehal");
		path.mkdirs();
		Log.i(TAG, "path: " + path);
		return path;
	}
}
