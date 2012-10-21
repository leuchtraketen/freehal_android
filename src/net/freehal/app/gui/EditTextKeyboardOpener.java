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
package net.freehal.app.gui;

import net.freehal.app.util.AndroidUtils;
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
		InputMethodManager inputStatus = (InputMethodManager) AndroidUtils.getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
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
				if (hasFocus && AndroidUtils.getActivity() != null) {
					showKeyboard();
				}
			}
		});

		edit.requestFocus();
		showKeyboard();
	}

}
