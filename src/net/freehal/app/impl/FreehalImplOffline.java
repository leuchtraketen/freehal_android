package net.freehal.app.impl;

import net.freehal.app.R;
import net.freehal.app.util.Util;

public class FreehalImplOffline extends FreehalImpl {

	private static FreehalImplOffline instance;

	@SuppressWarnings("unused")
	private String input;
	private String output;

	private FreehalImplOffline() {
	}

	public static FreehalImpl getInstance() {
		if (instance == null)
			instance = new FreehalImplOffline();
		return instance;
	}

	@Override
	public void setInput(String input) {
		this.input = input;
	}

	@Override
	public void compute() {
		output = Util.getActivity().getResources()
				.getString(R.string.not_implemented);
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

	@Override
	public String getVersionName() {
		return "not installed";
	}

	@Override
	public int getVersionCode() {
		return -1;
	}
}
