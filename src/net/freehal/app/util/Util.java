package net.freehal.app.util;

import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class Util {

	private static Activity activity;

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
			packageInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// Huh? Really?
			packageInfo = new PackageInfo();
			packageInfo.versionCode = 0;
			packageInfo.versionName = "0";
		}
		return packageInfo;
	}

	public static void setActivity(Activity activity) {
		Util.activity = activity;
	}

	public static Activity getActivity() {
		return activity;
	}

	public static String getString(int s) {
		return Util.getActivity().getResources().getString(s);
	}

	public static SharedPreferences getPreferences() {
		return getPreferences(activity.getBaseContext());
	}

	public static SharedPreferences getPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs;
	}
}
