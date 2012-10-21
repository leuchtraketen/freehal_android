package net.freehal.app.notification;

import net.freehal.app.util.AndroidUtils;
import net.freehal.app.util.BinderUtility;
import net.freehal.core.util.LogUtils;
import net.freehal.core.util.LogUtils.ProgressListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class RemoteProgressListener implements ProgressListener {

	private BinderUtility binderUtility = null;
	private Messenger mServiceMessenger = null;

	@Override
	public void onProgressUpdate(double current, double max, String text) {
		if (bind()) {
			Message msg = Message.obtain(null, NotificationService.MSG_UPDATE, 0, 0);
			Bundle bundle = new Bundle();
			bundle.putDouble(NotificationService.DATA_PROGRESS, current);
			bundle.putDouble(NotificationService.DATA_MAX, max);
			bundle.putString(NotificationService.DATA_TEXT, text);
			msg.setData(bundle);
			try {
				LogUtils.i("Sending message to service: " + msg);
				mServiceMessenger.send(msg);
			} catch (RemoteException ex) {
				LogUtils.e(ex);
			}
		} else {
			LogUtils.e("cannot create, not bound!");
		}
	}

	@Override
	public void onProgressBeginning() {
		AndroidUtils.getActivity().startService(
				new Intent(AndroidUtils.getActivity().getApplicationContext(), NotificationService.class));
		binderUtility = new BinderUtility(NotificationService.class, mConnection, null);

		LogUtils.i("Binding to service...");
		if (bind()) {
			LogUtils.i("Bound to service.");
			create();
		}
	}

	private void create() {
		if (bind()) {
			LogUtils.i("create...");
			Message msg = Message.obtain(null, NotificationService.MSG_CREATE, 0, 0);
			try {
				LogUtils.i("sending message to service: " + msg);
				mServiceMessenger.send(msg);
			} catch (RemoteException ex) {
				LogUtils.e(ex);
			}
		} else {
			LogUtils.e("cannot create, not bound!");
		}
	}

	@Override
	public void onProgressEnd() {
		destroy();
	}

	private void destroy() {
		if (bind()) {
			LogUtils.i("destroy...");
			Message msg = Message.obtain(null, NotificationService.MSG_DESTROY, 0, 0);
			try {
				LogUtils.i("sending message to service: " + msg);
				mServiceMessenger.send(msg);
			} catch (RemoteException ex) {
				LogUtils.e(ex);
			}
		} else {
			LogUtils.e("cannot destroy, not bound!");
		}
	}

	@Override
	public void onSubProgressBeginning() {}

	@Override
	public void onSubProgressEnd() {}

	boolean bind() {
		if (binderUtility == null)
			binderUtility = new BinderUtility(NotificationService.class, mConnection, null);
		return binderUtility.bind();
	}

	void unbind() {
		if (binderUtility != null) {
			binderUtility.unbind();
			binderUtility = null;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mServiceMessenger = new Messenger(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mServiceMessenger = null;
		}
	};
}
