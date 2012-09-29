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
		} catch (InterruptedException e) {}
		return null;
	}

	@Override
	protected void onPostExecute(Void output) {
		run();

		if (selfInvoke > 0) {
			final ExecuteLater thisThread = this;
			ExecuteLater selfInvoker = new ExecuteLater(timeToSleep, selfInvoke - 1) {
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
