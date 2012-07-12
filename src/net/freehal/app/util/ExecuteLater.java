package net.freehal.app.util;

import android.os.AsyncTask;

public abstract class ExecuteLater extends AsyncTask<Void, Void, Void> {
	private final int timeToSleep;
	private final int selfInvoke;

	public ExecuteLater(final int timeToSleep) {
		super();
		this.timeToSleep = timeToSleep;
		this.selfInvoke = 0;
	}

	public ExecuteLater(final int timeToSleep, int selfInvoke) {
		super();
		this.timeToSleep = timeToSleep;
		this.selfInvoke = selfInvoke;
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

		if (selfInvoke > 0) {
			final ExecuteLater thisThread = this;
			ExecuteLater selfInvoker = new ExecuteLater(timeToSleep,
					selfInvoke - 1) {
				@Override
				public void run() {
					thisThread.run();
				}
			};
			selfInvoker.execute();
		}
	}

	abstract public void run();
}
