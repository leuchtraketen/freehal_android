package net.freehal.app;

import java.util.Locale;

import net.freehal.app.impl.FreehalImpl;
import net.freehal.app.impl.FreehalImplOffline;
import net.freehal.app.impl.FreehalImplOnline;
import net.freehal.app.util.ExecuteLater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class DetailFragment extends Fragment implements OnInitListener {

	public static String ARG_ITEM_ID = "item_id";

	private Activity activity;
	private TextToSpeech tts;
	private History history;
	private FreehalImpl onlineImpl;
	private FreehalImpl offlineImpl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown tts!
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	public String getOutput(final String input, final FreehalImpl impl) {
		return impl.getOutput(input);
	}

	public void gotInput(final EditText edit, final TextView text,
			final ScrollView scrollView, final FreehalImpl impl) {

		if (history == null)
			return;

		final String input = edit.getText().toString();

		if (input.length() > 0) {
			history.addInput(input);
			history.writeTo(text, scrollView);
			edit.setText("");

			final DetailFragment frag = this;
			AsyncTask<String, Void, String> async = new AsyncTask<String, Void, String>() {

				@Override
				protected String doInBackground(String... arg0) {
					System.out.println("input: " + input);
					String output = getOutput(input, impl);
					if (frag.isAdded() && frag.isVisible()) {
						if (output == null)
							output = getResources().getString(
									R.string.no_output);
					}
					System.out.println("output: " + input);
					return output;
				}

				@Override
				protected void onPostExecute(String output) {
					history.addOutput(output);
					if (frag.isAdded() && frag.isVisible()) {
						history.writeTo(text, scrollView);
						tts.speak(Html.fromHtml(output).toString(),
								TextToSpeech.QUEUE_FLUSH, null);
						showKeyboard(edit, scrollView, 1000);
					}
				}

			};
			async.execute();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final String item = getArguments().getString(ARG_ITEM_ID);

		if (item == "1" || item == "2") {
			return onCreateViewConversation(inflater, container,
					savedInstanceState, item);
		} else if (item == "3") {
			return onCreateViewSettings(inflater, container,
					savedInstanceState, item);
		} else {
			return onCreateViewConversation(inflater, container,
					savedInstanceState, "1");
		}
	}

	public View onCreateViewConversation(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState, final String item) {

		final View rootView = inflater.inflate(
				R.layout.fragment_conversation_detail, container, false);
		final TextView text = (TextView) rootView
				.findViewById(R.id.conversation_detail);
		final EditText edit = (EditText) rootView
				.findViewById(R.id.edit_message);
		final ScrollView scrollView = (ScrollView) rootView
				.findViewById(R.id.conversation_scrollview);
		final Button sendButton = (Button) rootView
				.findViewById(R.id.button_send);

		tts = new TextToSpeech(rootView.getContext(), this);
		history = createHistory(rootView, item);
		final FreehalImpl impl = chooseFreehalImpl(item);

		edit.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					gotInput(edit, text, scrollView, impl);
					return true;
				} else {
					scrollView.smoothScrollTo(0, edit.getBottom());
					return false;
				}
			}
		});
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gotInput(edit, text, scrollView, impl);
			}
		});

		history.writeTo(text, scrollView);
		showKeyboard(edit, scrollView, 1000);

		return rootView;
	}

	private void showKeyboard(final EditText edit, final ScrollView scrollView,
			final int timeToSleep) {
		if (edit.getOnFocusChangeListener() == null)
			edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && activity != null) {
						InputMethodManager inputStatus = (InputMethodManager) activity
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputStatus.showSoftInput(edit,
								InputMethodManager.SHOW_IMPLICIT);
					}
				}
			});

		ExecuteLater asyncFocus = new ExecuteLater(timeToSleep) {
			@Override
			public void run() {
				edit.requestFocus();
			}
		};
		asyncFocus.execute();
	}

	private History createHistory(View rootView, String item) {
		History hist = new History(rootView.getContext(), item);
		if (item == "1") {
			hist.setAlternateText(R.string.comment_conversation_online);
		} else if (item == "2") {
			hist.setAlternateText(R.string.comment_conversation_offline);
		}
		return hist;
	}

	private FreehalImpl chooseFreehalImpl(String item) {

		final FreehalImpl impl;
		if (item == "1") {
			if (onlineImpl == null)
				onlineImpl = new FreehalImplOnline(this.getResources());
			impl = onlineImpl;
			history.setAlternateText(R.string.comment_conversation_online);
		} else if (item == "2") {
			if (offlineImpl == null)
				offlineImpl = new FreehalImplOffline(this.getResources());
			impl = offlineImpl;
			history.setAlternateText(R.string.comment_conversation_offline);
		} else {
			impl = null;
		}
		return impl;
	}

	public View onCreateViewSettings(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState, final String item) {

		View rootView = inflater.inflate(R.layout.fragment_settings_detail,
				container, false);
		((TextView) rootView.findViewById(R.id.settings_detail))
				.setText("test");
		return rootView;
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.GERMANY);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
		}
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
