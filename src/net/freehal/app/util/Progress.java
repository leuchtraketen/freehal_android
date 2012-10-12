package net.freehal.app.util;

import java.util.ArrayList;
import java.util.List;

import net.freehal.core.util.LogUtils;
import net.freehal.core.util.LogUtils.ProgressListener;

public class Progress {

	private static List<ProgressImplementation> impls = new ArrayList<ProgressImplementation>();

	private static boolean enabledProgressListener = false;
	private static double progressListenerMin = 0;
	private static double progressListenerMax = 0;

	public static void addImplementation(ProgressImplementation impl) {
		if (impl != null)
			impls.add(impl);
	}

	public static void registerProgressListener() {
		LogUtils.addProgressListener(new ProgressListener() {

			@Override
			public void onProgressUpdate(double current, double max) {
				if (enabledProgressListener) {
					for (ProgressImplementation impl : impls) {
						impl.updateProgress(progressListenerMin + (progressListenerMax - progressListenerMin)
								* current / max);
					}
				}
			}

			@Override
			public void onProgressBeginning() {}

			@Override
			public void onProgressEnd() {}
		});
	}

	public static void enableProgressListener(int min, int max) {
		enabledProgressListener = true;
		progressListenerMin = min;
		progressListenerMax = max;
	}

	public static void disableProgressListener() {
		enabledProgressListener = false;
	}

	public static void update(int progress, String text) {
		for (ProgressImplementation impl : impls) {
			impl.update(progress, text);
		}
	}

	public static void updateProgress(double progress) {
		for (ProgressImplementation impl : impls) {
			impl.updateProgress(progress);
		}
	}

	public static void updateText(String text) {
		for (ProgressImplementation impl : impls) {
			impl.updateText(text);
		}
	}

	public static interface ProgressImplementation {

		void updateProgress(double progress);

		void updateText(String text);

		void update(double progress, String text);

	}

}
