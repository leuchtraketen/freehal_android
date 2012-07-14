package net.freehal.app.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import net.freehal.app.util.HttpUtil;

public class FreehalImplOnline extends FreehalImpl {

	private static FreehalImplOnline instance;

	private String input;
	private String output;
	private String log;
	private String graph;

	private FreehalImplOnline() {
	}

	public static FreehalImpl getInstance() {
		if (instance == null)
			instance = new FreehalImplOnline();
		return instance;
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
		return "not implemented.";
	}

	@Override
	public int getVersionCode() {
		return -1;
	}
}
