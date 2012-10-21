package net.freehal.app.util;

import net.freehal.core.util.LogUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class BinderUtility {

	/** Flag indicating whether we have called bind on the service. */
	private boolean mBound = false;

	/** the Serviceconnection provided by the caller */
	private Messenger mServiceMessenger = null;
	private Messenger mReplyMessenger = null;
	private ServiceConnection mInnerConnection = null;

	/** the service to bind to */
	private Class<?> serviceClass = null;

	public static final int MSG_REGISTER_CLIENT = 99;

	public BinderUtility(Class<?> serviceClass, ServiceConnection mConnection, Messenger mReplyMessenger) {
		this.serviceClass = serviceClass;
		this.mInnerConnection = mConnection;
		this.mReplyMessenger = mReplyMessenger;
	}

	public synchronized boolean bind() {
		if (mBound)
			return true;

		// Bind to FreehalService
		Intent intent = new Intent(AndroidUtils.getActivity().getApplicationContext(), serviceClass);
		
		if (AndroidUtils.getActivity().getApplicationContext()
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {

			waitUntilBound();
			registerClient();
		
			return true;
		} else {
			return false;
		}
	}

	private void registerClient() {
		if (mReplyMessenger != null) {
			LogUtils.i("registering client: mReplyMessenger=" + mReplyMessenger);
			Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
			msg.replyTo = mReplyMessenger;
			try {
				mServiceMessenger.send(msg);
			} catch (RemoteException ex) {
				LogUtils.e(ex);
			}
			LogUtils.i("registered client.");
		} else {
			LogUtils.e("not registering client: mReplyMessenger=" + mReplyMessenger);
		}
	}

	private void waitUntilBound() {
		LogUtils.i("wait until bound...");
		while (!mBound)
			AndroidUtils.sleep(500);
		LogUtils.i("bound!");
	}

	public Messenger getServiceMessenger() {
		return mServiceMessenger;
	}

	public synchronized void unbind() {
		// Unbind from FreehalService
		if (mBound) {
			AndroidUtils.getActivity().getApplicationContext().unbindService(mConnection);
			mBound = false;
		}
	}

	public boolean isBound() {
		return mBound;
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mServiceMessenger = new Messenger(service);

			// NotificationService.LocalBinder binder =
			// (NotificationService.LocalBinder) service;
			// mService = binder.getService();
			if (mInnerConnection != null)
				mInnerConnection.onServiceConnected(name, service);

			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;

			if (mInnerConnection != null)
				mInnerConnection.onServiceDisconnected(name);

			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mServiceMessenger = null;
		}
	};

	public void send(Message msg) throws RemoteException {
		mServiceMessenger.send(msg);
	}
}
