package net.freehal.app;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;

public class History {
	private Context view;
	private ArrayList<String> history;

	public History(Context view) {
		history = new ArrayList<String>();
		this.view = view;
	}

	public void addInput(String input) {
		history.add(tag(getString(R.string.person_user), "b", "") + ": "
				+ input);
	}

	public void addOutput(String output) {
		history.add(tag(getString(R.string.person_freehal), "b", "") + ": "
				+ output);
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
}
