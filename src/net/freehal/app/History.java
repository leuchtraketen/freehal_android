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
import java.util.List;

import net.freehal.app.impl.FreehalUser;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.widget.ScrollView;
import android.widget.TextView;

public class History {
	private Context view;
	private ArrayList<String> name;
	private ArrayList<String> text;
	private String item_id;
	private int alternateText;
	private HistoryHook hook;

	public History(Context view, String item_id) {
		this.view = view;
		this.item_id = item_id;
		this.alternateText = 0;

		name = new ArrayList<String>();
		text = new ArrayList<String>();

		restore();
	}

	public void addInput(String input) {
		final String user = FreehalUser.get().getUserName(
				getString(R.string.person_user));
		name.add(tag(user, "b", ""));
		text.add(input);
		save();
	}

	public void addOutput(String output) {
		final String user = getString(R.string.person_freehal);
		name.add(tag(user, "b", ""));
		text.add(output);
		save();
	}

	private File getStorageFile(final String column) {
		return new File(view.getCacheDir(), "history_" + item_id + "_" + column);
	}

	private void save() {
		if (!name.isEmpty())
			save(name, "name");
		if (!text.isEmpty())
			save(text, "text");

		if (hook != null)
			hook.onHistoryChanged();
	}

	private void restore() {
		restore(name, "name");
		restore(text, "text");
	}

	private void save(final ArrayList<String> list, final String column) {
		try {
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(
							getStorageFile(column))));
			for (String line : list) {
				dos.writeUTF(line);
			}
			dos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void restore(ArrayList<String> list, final String column) {
		try {
			DataInputStream din = new DataInputStream(new BufferedInputStream(
					new FileInputStream(getStorageFile(column))));
			try {
				for (;;) {
					list.add(din.readUTF());
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
		for (int i = 0; i < name.size() && i < text.size(); ++i) {
			str += name.get(i) + ": " + text.get(i) + "<br/>";
		}
		return str;
	}

	public boolean writeTo(final TextView text, final ScrollView mScrollView) {

		if (size() > 0) {
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

	public List<String> getText() {
		return text;
	}

	public List<String> getName() {
		return name;
	}

	public String getText(int i) {
		return i < text.size() ? text.get(i) : null;
	}

	public String getName(int i) {
		return i < name.size() ? name.get(i) : null;
	}

	public int size() {
		return text.size() <= name.size() ? text.size() : name.size();
	}

	public void setHook(HistoryHook hook) {
		this.hook = hook;
	}

	public void refresh() {
		hook.onHistoryChanged();
	}
}
