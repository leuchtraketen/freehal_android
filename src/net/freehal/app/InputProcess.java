package net.freehal.app;

import java.util.List;

import net.freehal.app.impl.FreehalImpl;
import net.freehal.app.impl.FreehalImplUtil;
import net.freehal.app.util.SpeechHelper;
import net.freehal.app.util.Util;
import net.freehal.app.util.VoiceRecHelper;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class InputProcess {

	public static void recieveInput(final String rawInput,
			final KeyboardOpener keyboardOpener) {

		final History history = History.getInstance();

		final String input = Util.toAscii(rawInput);

		if (input.length() > 0) {
			final int inputNo = history.addInput(input, "");

			final String noOutput = Util.getActivity().getResources()
					.getString(R.string.no_output);
			AsyncTask<String, Void, String> async = new AsyncTask<String, Void, String>() {

				@Override
				protected String doInBackground(String... arg0) {
					System.out.println("input: " + input);
					final FreehalImpl impl = FreehalImplUtil.getInstance();
					impl.setInput(input);
					impl.compute();
					String output = impl.getOutput();
					if (output == null)
						output = noOutput;
					System.out.println("output: " + input);
					return output;
				}

				@Override
				protected void onPostExecute(String output) {
					history.addOutput(output, inputNo + "");

					SpeechHelper.getInstance().say(output);

					if (keyboardOpener != null)
						keyboardOpener.onShowKeyboard();
				}

			};
			async.execute();
		}
	}

	public static class Listener implements View.OnKeyListener,
			View.OnClickListener, VoiceRecHelper.ResultHook {

		final EditText edit;
		final DetailFragment fragment;

		public Listener(EditText edit, DetailFragment fragment) {
			this.edit = edit;
			this.fragment = fragment;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				final String input = edit.getText().toString();
				edit.setText("");
				InputProcess.recieveInput(input, new EditTextKeyboardOpener(
						edit));
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onClick(View v) {
			final String input = edit.getText().toString();
			edit.setText("");
			InputProcess.recieveInput(input, new EditTextKeyboardOpener(edit));
		}

		@Override
		public void onVoiceResult(List<String> list) {
			if (fragment.isAdded() == false)
				return;
			if (list.size() == 0)
				return;

			String best = list.get(0);
			InputProcess.recieveInput(best, new EditTextKeyboardOpener(edit));
		}
	}
}
