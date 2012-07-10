package net.freehal.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.widget.ScrollView;
import android.widget.TextView;

public class History {
	private Context view;
	private ArrayList<String> history;
	private String item_id;
	private int alternateText;

	public History(Context view, String item_id) {
		history = new ArrayList<String>();
		this.view = view;
		this.item_id = item_id;
		this.alternateText = 0;
		restore();
	}

	public void addInput(String input) {
		history.add(tag(getString(R.string.person_user), "b", "") + ": "
				+ input);
		save();
	}

	public void addOutput(String output) {
		history.add(tag(getString(R.string.person_freehal), "b", "") + ": "
				+ output);
		save();
	}

	private File getStorageFile() {
		return new File(view.getCacheDir(), "history_" + item_id);
	}

	private void save() {
		try {
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							getStorageFile())));
			for (String line : history) {
				dos.writeUTF(line);
			}
			dos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void restore() {
		try {
			DataInputStream din = new DataInputStream(new BufferedInputStream(
					new FileInputStream(getStorageFile())));
			try {
				for (;;) {
					history.add(din.readUTF());
				}
			} catch (EOFException e) {
				// EOF reached
			}
			din.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getString(int s) {
		return view.getResources().getString(s);
	}

	@SuppressLint("ParserError")
	private String tag(String s, String tag, String params) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<");
		stringBuilder.append(tag);
		if (params.length() > 0)
			stringBuilder.append(params);
		stringBuilder.append(">");
		stringBuilder.append(s);
		stringBuilder.append("</");
		stringBuilder.append(tag);
		stringBuilder.append(">");
		return stringBuilder.toString();
	}

	@Override
	public String toString() {
		String str = new String();
		for (String line : history) {
			str += line + "<br/>";
		}
		return str;
	}

	public boolean writeTo(final TextView text, final ScrollView mScrollView) {
		if (history.size() > 0) {
			text.setText(Html.fromHtml(this.toString()));
			mScrollView.post(new Runnable() {
				public void run() {
					save();
					mScrollView.smoothScrollTo(0, text.getBottom());
				}
			});
			return true;
		} else {
			if (alternateText != 0)
				text.setText(alternateText);
			return false;
		}
	}

	public void setAlternateText(int alternateText) {
		this.alternateText = alternateText;
	}
}
