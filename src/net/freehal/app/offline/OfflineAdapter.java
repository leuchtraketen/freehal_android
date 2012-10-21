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
package net.freehal.app.offline;

import net.freehal.app.util.BinderUtility;
import net.freehal.app.util.FreehalAdapter;
import net.freehal.app.util.AndroidUtils;
import net.freehal.core.util.LogUtils;
import net.freehal.core.util.StringUtils;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class OfflineAdapter extends FreehalAdapter {

	private static OfflineAdapter instance;

	public static FreehalAdapter getInstance() {
		if (instance == null)
			instance = new OfflineAdapter();
		return instance;
	}

	private String input;
	private String output;

	/** Messenger for communicating with the service. */
	private Messenger mReplyMessenger = null;

	/** Utility class for binding to the service */
	private BinderUtility binderUtility = null;

	private OfflineAdapter() {
		OfflineImplementation.register();
	}

	@Override
	public void initialize() {
		mReplyMessenger = new Messenger(new IncomingHandler());
	}

	@Override
	public void setInput(String input) {
		this.input = input;
		LogUtils.i("Input: " + input);
	}

	@Override
	public synchronized void compute() {
		LogUtils.i("Binding to service...");
		if (bind()) {
			LogUtils.i("Bound to service.");
			output = null;

			Message msg = Message.obtain(null, OfflineService.MSG_INPUT, 0, 0);
			msg.replyTo = getReplyMessenger();
			Bundle bundle = new Bundle();
			bundle.putString(OfflineService.DATA_TEXT, input);
			msg.setData(bundle);

			try {
				LogUtils.i("Sending message to service: msg=" + msg + ", replyTo=" + msg.replyTo);
				binderUtility.send(msg);

				while (output == null) {
					if (!binderUtility.isBound())
						throw new Exception("Error while waiting for reply: "
								+ "not bound to OfflineService any more!");

					AndroidUtils.sleep(500);
				}
				LogUtils.d("output != null, output=" + output);
			} catch (Exception ex) {
				LogUtils.e(ex);
				output = StringUtils.asString(ex);
			}

			LogUtils.i("Unbinding from service...");
			unbind();
			LogUtils.i("Unbound from service.");
		} else {
			LogUtils.e("Could not bind to service!");
			output = "Could not bind to service!";
		}
	}

	private Messenger getReplyMessenger() {
		return mReplyMessenger;
	}

	@Override
	public String getOutput() {
		return output;
	}

	@Override
	public String getLog() {
		final String log = OfflineImplementation.getLogFile().read();
		return (log != null) ? log : "";
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

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {}

		public void onServiceDisconnected(ComponentName className) {}
	};

	/**
	 * Handler of incoming messages from service.
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OfflineService.MSG_OUTPUT:
				final Bundle data = msg.getData();
				if (data.containsKey(OfflineService.DATA_TEXT)) {
					output = data.getString(OfflineService.DATA_TEXT);
				} else {
					LogUtils.e("MSG_OUTPUT message from service " + "does not contain DATA_TEXT field: msg="
							+ msg + ", bundle=" + data);
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	boolean bind() {
		if (binderUtility == null)
			binderUtility = new BinderUtility(OfflineService.class, mConnection, mReplyMessenger);
		return binderUtility.bind();
	}

	void unbind() {
		if (binderUtility != null) {
			binderUtility.unbind();
			binderUtility = null;
		}
	}
}