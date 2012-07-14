package net.freehal.app.impl;

public abstract class FreehalImpl {

	public abstract void setInput(String input);

	public abstract String getOutput();

	public abstract String getLog();

	public abstract String getGraph();

	public abstract void compute();

	public abstract String getVersionName();

	public abstract int getVersionCode();
	
}
