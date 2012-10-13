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

import java.util.HashMap;
import java.util.Map;

public class FreehalImpls {

	private static Map<String, FreehalImpl> impls;
	private static String current;
	static {
		impls = new HashMap<String, FreehalImpl>();
		impls.put("offline", FreehalImplOffline.getInstance());
		impls.put("online", FreehalImplOnline.getInstance());
		current = "offline";
	}

	public static FreehalImpl getInstance(String key) {
		if (impls.containsKey(key))
			return impls.get(key);
		else
			return null;
	}

	public static FreehalImpl getInstance() {
		return getInstance(current);
	}

	public static String getCurrent() {
		return current;
	}

	public static void setCurrent(String current) {
		if (current.equals("online") || current.equals("offline")) {
			FreehalImpls.current = current;
		}
	}

	/**
	 * This is run at the very beginning of the application to ensure that the
	 * static variables of this class are initialized...
	 */
	public static void initialize() {}
}
