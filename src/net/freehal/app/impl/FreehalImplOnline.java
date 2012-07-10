package net.freehal.app.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.res.Resources;

import net.freehal.app.util.HttpUtil;

public class FreehalImplOnline extends FreehalImpl {
	
	@SuppressWarnings("unused")
	private final Resources resources;

	public FreehalImplOnline(Resources resources) {
		this.resources = resources;
	}

	@Override
	public String getOutput(String input) {
		String url = "https://www.tobias-schulz.eu/demo-api?q=";
		try {
			url += URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			final String output = HttpUtil.executeHttpGet(url).trim();
			if (output.length() > 0) {
				return output;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error! " + e.getMessage();
		}
	}
}
