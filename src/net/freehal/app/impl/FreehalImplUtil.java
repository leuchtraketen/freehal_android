package net.freehal.app.impl;

public class FreehalImplUtil {

	private static String current;

	static {
		current = "online";
	}

	public static FreehalImpl getInstance(String item) {
		final FreehalImpl impl;
		if (item.equals("online")) {
			current = item;
			impl = FreehalImplOnline.getInstance();
		} else if (item.equals("offline")) {
			current = item;
			impl = FreehalImplOffline.getInstance();
		} else {
			impl = null;
		}
		return impl;
	}

	public static FreehalImpl getInstance() {
		return getInstance(current);
	}

	public static String getCurrent() {
		return current;
	}

	public static void setCurrent(String current) {
		if (current.equals("online") || current.equals("offline")) {
			FreehalImplUtil.current = current;
		}
	}
}
