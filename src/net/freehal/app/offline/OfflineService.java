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

import net.freehal.app.notification.RemoteProgressListener;
import net.freehal.app.util.AndroidUtils;
import net.freehal.app.util.BinderUtility;
import net.freehal.app.util.FreehalUser;
import net.freehal.core.util.LogUtils;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class OfflineService extends Service {

	final static String TAG = "OfflineService";

	public static final int MSG_INPUT = 1;
	public static final int MSG_OUTPUT = 2;
	public static final String DATA_TEXT = "input";

	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private Messenger mReplyTo = null;

	@Override
	public void onCreate() {
		Log.i(TAG, "creating service");
		AndroidUtils.setActivity(this, OfflineService.class);
		FreehalUser.init(this.getApplicationContext());
		OfflineImplementation.register();
		LogUtils.addProgressListener(new RemoteProgressListener());
		Log.i(TAG, "created service");
	}

	private static String compute(Bundle data) {
		if (data.containsKey(DATA_TEXT)) {
			final String input = data.getString(DATA_TEXT);
			return compute(input);
		} else {
			return null;
		}
	}

	private static String compute(String input) {
		return OfflineImplementation.compute(input);
	}

	private static void reply(String answer, Messenger replyTo) {
		Message backMsg = Message.obtain(null, MSG_OUTPUT, 0, 0);
		Bundle bundle = new Bundle();
		bundle.putString(DATA_TEXT, answer);
		backMsg.setData(bundle);
		try {
			LogUtils.i("sending reply: text=" + answer + ", replyTo=" + replyTo + ", msg=" + backMsg);
			if (replyTo != null) {
				replyTo.send(backMsg);
				LogUtils.i("reply sent!");
			} else {
				LogUtils.e("not sending reply: replyTo is null!");
			}
		} catch (android.os.RemoteException e1) {
			LogUtils.e("Exception sending message: " + e1);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(getApplicationContext(), "someone binds to " + TAG, Toast.LENGTH_SHORT).show();
		return mMessenger.getBinder();
	}

	public void handleInput(final Bundle bundle) {
		// Toast.makeText(AndroidUtils.getActivity().getApplicationContext(),
		// "test", Toast.LENGTH_SHORT).show();

		new Thread() {
			@Override
			public void run() {
				reply(compute(bundle), mReplyTo);
			}
		}.start();
	}

	/**
	 * Handler of incoming messages from clients.
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case MSG_INPUT:
				handleInput(msg.getData());
				break;
			case BinderUtility.MSG_REGISTER_CLIENT:
				if (msg.replyTo != null) {
					mReplyTo = msg.replyTo;
					LogUtils.i("registered client: mReplyTo=" + mReplyTo);
				} else {
					LogUtils.i("registering client failed: mReplyTo=" + mReplyTo);
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}