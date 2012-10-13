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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.freehal.core.util.FreehalFile;
import net.freehal.core.util.LogUtils;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class Util {

	private static final String TAG = "Util";

	private static Activity activity;
	private static Class<?> activityClass;

	public static String toAscii(String str) {
		byte[] dirty = EncodingUtils.getBytes(str, "UTF-8");
		ArrayList<Byte> clean = new ArrayList<Byte>();
		boolean maybeUmlaut = false;
		for (byte b : dirty) {
			if (maybeUmlaut) {
				switch (b) {
				case -92:
					add(clean, "ae");
					break;
				case -74:
					add(clean, "oe");
					break;
				case -68:
					add(clean, "ue");
					break;
				case -124:
					add(clean, "Ae");
					break;
				case -106:
					add(clean, "Oe");
					break;
				case -100:
					add(clean, "Ue");
					break;
				case -97:
					add(clean, "ss");
					break;
				}
				maybeUmlaut = false;
			} else if (b == -61) {
				maybeUmlaut = true;
			} else {
				clean.add(b);
			}
		}
		byte[] cleanBytes = new byte[clean.size()];
		int n = 0;
		for (byte b : clean) {
			cleanBytes[n++] = b;
		}

		return new String(cleanBytes);
	}

	private static void add(ArrayList<Byte> clean, String toAdd) {
		// skip null byte!
		for (int i = 0; i < toAdd.length(); ++i) {
			clean.add((byte) toAdd.charAt(i));
		}
	}

	public static int min(int a, int b) {
		return a < b ? a : b;
	}

	public static int min(int a, int b, int c) {
		return min(min(a, b), c);
	}

	public static int min(int a, int b, int c, int d) {
		return min(min(a, b, c), d);
	}

	public static PackageInfo getVersion(Context context) {
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// Huh? Really?
			packageInfo = new PackageInfo();
			packageInfo.versionCode = 0;
			packageInfo.versionName = "0";
		}
		return packageInfo;
	}

	public static void setActivity(Activity activity, Class<?> activityClass) {
		Util.activity = activity;
		Util.activityClass = activityClass;
	}

	public static Activity getActivity() {
		return activity;
	}

	public static Class<?> getActivityClass() {
		return activityClass;
	}

	public static String getString(int s) {
		return Util.getActivity().getResources().getString(s);
	}

	public static SharedPreferences getPreferences() {
		return getPreferences(activity.getBaseContext());
	}

	public static SharedPreferences getPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs;
	}

	public static boolean unpackZip(final File zippath, final FreehalFile extractTo) {
		try {
			return unpackZip(new FileInputStream(zippath), extractTo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean unpackZip(final InputStream zipinput, final FreehalFile extractTo) {
		LogUtils.startProgress("unpacking database files");

		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new BufferedInputStream(zipinput));
			ZipEntry ze;

			while ((ze = zis.getNextEntry()) != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;

				final String filename = ze.getName();
				// only extract if it's a file (and not a directory)!
				if (!filename.endsWith("/")) {
					Log.i(TAG, "extract zip: " + filename + " in " + extractTo);
					try {
						extractTo.getChild(filename).write("");
						StringBuilder content = new StringBuilder();
						while ((count = zis.read(buffer)) != -1) {
							baos.write(buffer, 0, count);
							byte[] bytes = baos.toByteArray();
							content.append(new String(bytes));
							baos.reset();
							if (content.length() > 512000) {
								extractTo.getChild(filename).append(content.toString());
								content.delete(0, content.length());
								System.gc();
							}
						}
						if (content.length() > 0) {
							extractTo.getChild(filename).append(content.toString());
						}
						LogUtils.updateProgress();

						/**
						 * File extractpath = new File(extractTo, filename);
						 * extractpath.getParentFile().mkdirs();
						 * FileOutputStream fout = new
						 * FileOutputStream(extractpath);
						 * 
						 * while ((count = zis.read(buffer)) != -1) {
						 * baos.write(buffer, 0, count); byte[] bytes =
						 * baos.toByteArray(); fout.write(bytes); baos.reset();
						 * }
						 * 
						 * fout.close();
						 */
						zis.closeEntry();
					} catch (IOException e) {
						LogUtils.e(e);
						LogUtils.stopProgress();
						return false;
					}
				}
			}

			zis.close();
		} catch (IOException e) {
			LogUtils.e(e);
		}

		LogUtils.stopProgress();
		return true;
	}

	public static boolean unpackZip(int zipId, FreehalFile path) {
		InputStream stream = Util.getActivity().getResources().openRawResource(zipId);
		boolean result = unpackZip(stream, path);
		try {
			stream.close();
		} catch (IOException e) {
			LogUtils.e(e);
		}
		return result;
	}

	public static void sleep(int i) {
		long endTime = System.currentTimeMillis() + 5 * 1000;
		while (System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(endTime - System.currentTimeMillis());
			} catch (Exception e) {}
		}
	}
}
