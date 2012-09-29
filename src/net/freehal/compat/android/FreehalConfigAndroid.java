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

public class FreehalConfigAndroid implements FreehalConfigImpl {
	
	private static final String TAG = "FreehalConfigAndroid";

	private String language;
	private File path;

	public FreehalConfigAndroid() {
		language = "en";
		setLanguage(Locale.getDefault().getLanguage());
		Log.i(TAG, "language: " + language);
		
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			path = Environment.getExternalStorageDirectory();
			path.mkdirs();
		} else {
			Toast.makeText(Util.getActivity().getApplicationContext(), "No SD card found!", Toast.LENGTH_LONG);
			path = Util.getActivity().getApplicationContext().getCacheDir();
			path.mkdirs();
		}
		path = new File(path.getAbsoluteFile(), "freehal");
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
	public File getPath() {
		return path;
	}

	public FreehalConfigAndroid setPath(File path) {
		// not supported on Android
		return this;
	}

	@Override
	public File getLanguageDirectory() {
		return new File(path, "lang_" + language + "/").getAbsoluteFile();
	}

	@Override
	public File getCacheDirectory() {
		return new File(path, "cache_" + language + "/").getAbsoluteFile();
	}

}
