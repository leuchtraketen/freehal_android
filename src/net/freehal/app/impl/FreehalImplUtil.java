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
package net.freehal.app.impl;

public class FreehalImplUtil {

	private static String current = "online";

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
