package net.freehal.app.impl;

import net.freehal.app.R;
import android.content.res.Resources;

public class FreehalImplOffline extends FreehalImpl {

	private final Resources resources;
	@SuppressWarnings("unused")
	private String input;
	private String output;

	public FreehalImplOffline(Resources resources) {
		this.resources = resources;
	}

	@Override
	public void setInput(String input) {
		this.input = input;
	}

	@Override
	public void compute() {
		output = resources.getString(R.string.not_implemented);
	}

	@Override
	public String getOutput() {
		return output;
	}

	@Override
	public String getLog() {
		return null;
	}

	@Override
	public String getGraph() {
		return null;
	}
}
