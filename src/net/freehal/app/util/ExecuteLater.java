package net.freehal.app.util;

import android.os.AsyncTask;

public abstract class ExecuteLater extends AsyncTask<Void, Void, Void> {
	private final int timeToSleep;

	public ExecuteLater(final int timeToSleep) {
		super();
		this.timeToSleep = timeToSleep;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			Thread.sleep(timeToSleep);
		} catch (InterruptedException e) {
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void output) {
		run();
	}

	abstract public void run();
}
