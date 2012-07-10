package net.freehal.app.util;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class Util {

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
}
