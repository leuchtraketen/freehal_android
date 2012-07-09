package net.freehal.app.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.freehal.app.util.HttpUtil;

public class FreehalImplOnline extends FreehalImpl {

	@Override
	public String getOutput(String input) {
		String url = "https://www.tobias-schulz.eu/demo-api?q=";
		try {
			url += URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			return HttpUtil.executeHttpGet(url);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error! " + e.getMessage();
		}
	}
}
