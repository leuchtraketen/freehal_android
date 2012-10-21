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
package net.freehal.app.gui;

import net.freehal.app.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;

public class SelectContent {

	public static class DummyItem {

		public String id;
		public String title;

		public DummyItem(String id, String title) {
			this.id = id;
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}
	}

	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

	public static void init(Resources res) {
		ITEMS.clear();
		ITEM_MAP.clear();
		addItem(new DummyItem("online", res.getString(R.string.tab_online_talk)));
		addItem(new DummyItem("offline", res.getString(R.string.tab_offline_talk)));
		addItem(new DummyItem("log", res.getString(R.string.tab_log)));
		addItem(new DummyItem("graph", res.getString(R.string.tab_graph)));
		addItem(new DummyItem("about", res.getString(R.string.tab_about)));
	}

	public static String validateId(final String id) {
		if (ITEM_MAP.size() == 0)
			return id;

		if (ITEM_MAP.containsKey(id))
			return id;
		else
			return "about";
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}
}
