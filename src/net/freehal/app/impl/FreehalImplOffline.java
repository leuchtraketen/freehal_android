package net.freehal.app.impl;

import net.freehal.app.R;
import android.content.res.Resources;

public class FreehalImplOffline extends FreehalImpl {

	private final Resources resources;

	public FreehalImplOffline(Resources resources) {
		this.resources = resources;
	}

	@Override
	public String getOutput(String input) {
		return resources.getString(R.string.not_implemented);
	}

}
