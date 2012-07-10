package net.freehal.app.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.freehal.app.R;

import android.content.res.Resources;
import android.view.View;

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
		addItem(new DummyItem("online", res.getString(
				R.string.tab_online_talk)));
		addItem(new DummyItem("offline", res.getString(
				R.string.tab_offline_talk)));
		addItem(new DummyItem("log", res.getString(
				R.string.tab_log)));
		addItem(new DummyItem("graph", res.getString(
				R.string.tab_graph)));
		addItem(new DummyItem("settings", res.getString(
				R.string.tab_settings)));
	}

	public static String validateId(final String id) {
		if (ITEM_MAP.size() == 0)
			return id;

		if (ITEM_MAP.containsKey(id))
			return id;
		else
			return "online";
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}
}
