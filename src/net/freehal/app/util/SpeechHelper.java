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
		tts = new TextToSpeech(Util.getActivity().getApplicationContext(), this);
	}

	public void say(String text) {
		if (tts == null) {
			Log.e("TTS", "say: tts == null");
			this.textSayOnInit = text;
			start();
		} else if (ready) {
			Log.e("TTS", "say: tts != null");
			tts.speak(Html.fromHtml(text).toString(), TextToSpeech.QUEUE_FLUSH,
					null);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.GERMANY);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "German is not supported");

				Intent installTTSIntent = new Intent();
				installTTSIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				Util.getActivity().startActivity(installTTSIntent);

				result = tts.setLanguage(Locale.ENGLISH);
			}

			ready = true;

			if (tts != null && textSayOnInit != null
					&& textSayOnInit.length() > 0)
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