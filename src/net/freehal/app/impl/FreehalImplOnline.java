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
package net.freehal.app.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import net.freehal.app.util.ExecuteLater;
import net.freehal.app.util.HttpUtil;

public class FreehalImplOnline extends FreehalImpl {

	private static FreehalImplOnline instance;

	public static FreehalImpl getInstance() {
		if (instance == null)
			instance = new FreehalImplOnline();
		return instance;
	}

	private String input;
	private String output;
	private String log;
	private String graph;
	private int version;

	private FreehalImplOnline() {
		version = -1;
	}

	@Override
	public void initialize() {
		retrieveVersion();
	}

	@Override
	public void setInput(String input) {
		this.input = input;
	}

	@Override
	public void compute() {
		Random generator = new Random();
		long random = generator.nextLong();
		while (random < Math.pow(10, 8))
			random *= generator.nextLong();

		String url = "https://www.tobias-schulz.eu/demo-api?sep=" + random;
		try {
			url += "&user=" + FreehalUser.get().getEmailAddr("");
			url += "&q=" + URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		output = null;
		log = "";
		graph = "";
		try {
			output = HttpUtil.executeHttpGet(url).trim();
			if (output.length() > 0) {
				String[] splitted = output.split(String.valueOf(random), 3);
				output = splitted[0].trim();
				if (splitted.length > 1)
					log = splitted[1].trim();
				if (splitted.length > 2)
					graph = splitted[2].trim();
				System.out.println("log=" + log);
				System.out.println("graph=" + graph);
				System.out.println("splitted.length=" + splitted.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
			output = "Error! " + e.getMessage();
		}
	}

	@Override
	public String getOutput() {
		return output;
	}

	@Override
	public String getGraph() {
		return graph;
	}

	@Override
	public String getLog() {
		return log;
	}

	@Override
	public String getVersionName() {
		if (version == -1)
			retrieveVersion();
		return version == -1 ? "unknown" : "Revision " + version;
	}

	@Override
	public int getVersionCode() {
		if (version == -1)
			retrieveVersion();
		return version;
	}

	private void retrieveVersion() {
		ExecuteLater later = new ExecuteLater(0) {

			@Override
			public void run() {}

			@Override
			protected Void doInBackground(Void... params) {
				// main repository
				final String url = "https://freehal.googlecode.com/svn/trunk/";

				try {
					output = HttpUtil.executeHttpGet(url).trim();
					if (output.length() > 0) {
						String[] splitted = output.split("Revision ", 2);
						if (splitted.length == 2) {
							splitted = splitted[1].split(":", 2);
							if (splitted.length == 2) {
								version = Integer.parseInt(splitted[0]);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		later.execute();
	}
}
