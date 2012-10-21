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

import java.util.Locale;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.util.Log;

public class SpeechHelper implements OnInitListener {

	private static final SpeechHelper helper = new SpeechHelper();

	private static TextToSpeech tts;
	private boolean ready;
	private String textSayOnInit;

	public static SpeechHelper getInstance() {
		return helper;
	}

	public void start() {
		ready = false;
		tts = new TextToSpeech(AndroidUtils.getActivity().getApplicationContext(), this);
	}

	public void say(String text) {
		if (tts == null) {
			Log.e("TTS", "say: tts == null");
			this.textSayOnInit = text;
			start();
		} else if (ready) {
			Log.e("TTS", "say: tts != null");
			tts.speak(Html.fromHtml(text).toString(), TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.GERMANY);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "German is not supported");

				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				AndroidUtils.getActivity().startActivity(installTTSIntent);

				result = tts.setLanguage(Locale.ENGLISH);
			}

			ready = true;

			if (tts != null && textSayOnInit != null && textSayOnInit.length() > 0)
				say(textSayOnInit);
		} else {
			Log.e("TTS", "Initialization failed!");
			ready = true;
		}
	}

	public void stop() {
		if (tts != null) {
			tts.shutdown();
			tts.stop();
			tts = null;
		}
	}
}
