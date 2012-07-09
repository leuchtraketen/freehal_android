package net.freehal.app;

import java.util.Locale;

import net.freehal.app.impl.FreehalImpl;
import net.freehal.app.impl.FreehalImplOnline;
import net.freehal.app.select.SelectContent;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DetailFragment extends Fragment implements OnInitListener  {

	public static final String ARG_ITEM_ID = "item_id";
	private TextToSpeech tts;

	//private SelectContent.DummyItem mItem;

	private History history;
	private FreehalImpl onlineImpl;
	private FreehalImpl offlineImpl;

	public DetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = SelectContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}*/
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

	public String getOutput(String input, FreehalImpl impl) {
		return impl.getOutput(input);
	}

	public void gotInput(final EditText edit, final TextView text,
			final FreehalImpl impl) {
		if (history != null) {
			final String input = edit.getText().toString();
			if (input.length() > 0) {
				history.addInput(input);
				text.setText(Html.fromHtml(history.toString()));
				edit.setText("");

				AsyncTask<String, Void, String> async = new AsyncTask<String, Void, String>() {

					@Override
					protected String doInBackground(String... arg0) {
						System.out.println("input: " + input);
						String output = getOutput(input, impl);
						System.out.println("output: " + input);
						return output;
					}

					protected void onPostExecute(String output) {
						history.addOutput(output);
						text.setText(Html.fromHtml(history.toString()));
						tts.speak(output, TextToSpeech.QUEUE_FLUSH, null);
					}

				};
				async.execute();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getArguments().getString(ARG_ITEM_ID) == "1") {
			final View rootView = inflater.inflate(
					R.layout.fragment_conversation_detail, container, false);
			final TextView text = (TextView) rootView
					.findViewById(R.id.conversation_detail);
			final EditText edit = (EditText) rootView
					.findViewById(R.id.edit_message);
			final Button sendButton = (Button) rootView
					.findViewById(R.id.button_send);

			if (tts == null)
				tts = new TextToSpeech(rootView.getContext(), this);
			if (history == null)
				history = new History(rootView.getContext());
			if (onlineImpl == null)
				onlineImpl = new FreehalImplOnline();

			text.setText(R.string.comment_conversation_online);
			edit.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						gotInput(edit, text, onlineImpl);
						return true;
					} else {
						return false;
					}
				}
			});

			sendButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotInput(edit, text, onlineImpl);
				}
			});
			return rootView;

		} else if (getArguments().getString(ARG_ITEM_ID) == "2") {
			View rootView = inflater.inflate(
					R.layout.fragment_conversation_detail, container, false);
				((TextView) rootView.findViewById(R.id.conversation_detail))
						.setText(R.string.comment_conversation_offline);
			return rootView;

		} else if (getArguments().getString(ARG_ITEM_ID) == "3") {
			View rootView = inflater.inflate(R.layout.fragment_settings_detail,
					container, false);
				((TextView) rootView.findViewById(R.id.settings_detail))
						.setText("test");
			return rootView;

		} else {
			View rootView = inflater.inflate(R.layout.fragment_settings_detail,
					container, false);
				((TextView) rootView.findViewById(R.id.settings_detail))
						.setText(getArguments().getString(ARG_ITEM_ID));
		
			return rootView;
		}

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
}
