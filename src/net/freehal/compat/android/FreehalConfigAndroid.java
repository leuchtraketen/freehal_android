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

import java.io.File;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import net.freehal.app.util.Util;
import net.freehal.core.util.FreehalConfigImpl;
import net.freehal.core.util.FreehalFile;
import net.freehal.core.util.FreehalFiles;

public class FreehalConfigAndroid implements FreehalConfigImpl {

	private static final String TAG = "FreehalConfigAndroid";

	private String language;
	private FreehalFile path;

	public FreehalConfigAndroid() {
		language = "en";
		setLanguage(Locale.getDefault().getLanguage());
		Log.i(TAG, "language: " + language);

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			path = FreehalFiles.create(Environment.getExternalStorageDirectory().getPath());
			path.mkdirs();
		} else {
			Toast.makeText(Util.getActivity().getApplicationContext(), "No SD card found!", Toast.LENGTH_LONG);
			path = FreehalFiles.create(Util.getActivity().getApplicationContext().getCacheDir().getPath());
			path.mkdirs();
		}
		path = FreehalFiles.create(path.getAbsolutePath(), "freehal");
		path.mkdirs();
		Log.i(TAG, "path: " + path);
	}

	@Override
	public String getLanguage() {
		return language;
	}

	public FreehalConfigAndroid setLanguage(String language) {
		if (language.equals("de") || language.equals("en"))
			this.language = language;
		return this;
	}

	@Override
	public FreehalFile getPath() {
		return path;
	}

	public FreehalConfigAndroid setPath(File path) {
		// not supported on Android
		return this;
	}

	@Override
	public FreehalFile getLanguageDirectory() {
		return FreehalFiles.create(path, "lang_" + language + "/");
	}

	@Override
	public FreehalFile getCacheDirectory() {
		return FreehalFiles.create(path, "cache_" + language + "/");
	}

}
