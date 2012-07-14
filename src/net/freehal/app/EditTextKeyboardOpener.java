package net.freehal.app;

import net.freehal.app.util.Util;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class EditTextKeyboardOpener implements KeyboardOpener {

	final EditText edit;

	public EditTextKeyboardOpener(EditText edit) {
		this.edit = edit;
	}

	public void showKeyboard() {
		InputMethodManager inputStatus = (InputMethodManager) Util
				.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputStatus.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
	}

	@Override
	public void onShowKeyboard() {

		edit.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
						History.getInstance().refresh();
				return false;
			}
		});

		edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && Util.getActivity() != null) {
					showKeyboard();
				}
			}
		});

		edit.requestFocus();
		showKeyboard();
	}

}
