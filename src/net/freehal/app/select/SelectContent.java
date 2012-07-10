package net.freehal.app.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.freehal.app.R;

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

	public static void init(View view) {
		ITEMS.clear();
		ITEM_MAP.clear();
		addItem(new DummyItem("1", view.getResources().getString(
				R.string.tab_online_talk)));
		addItem(new DummyItem("2", view.getResources().getString(
				R.string.tab_offline_talk)));
		addItem(new DummyItem("3", view.getResources().getString(
				R.string.tab_log)));
		addItem(new DummyItem("4", view.getResources().getString(
				R.string.tab_graph)));
		addItem(new DummyItem("5", view.getResources().getString(
				R.string.tab_settings)));
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}
}
