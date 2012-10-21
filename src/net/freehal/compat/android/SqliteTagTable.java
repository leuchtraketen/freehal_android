package net.freehal.compat.android;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import net.freehal.app.util.AndroidUtils;
import net.freehal.core.pos.Tags;
import net.freehal.core.pos.storage.MemoryTagContainer;
import net.freehal.core.pos.storage.TagContainer;
import net.freehal.core.util.Factory;
import net.freehal.core.util.FreehalFile;
import net.freehal.core.util.LogUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteTagTable extends MemoryTagContainer implements TagContainer {

	@SuppressWarnings("unused")
	private final String name;
	private TagMapSqliteHelper helper;

	public SqliteTagTable(String name) {
		this.name = name;
		helper = new TagMapSqliteHelper(AndroidUtils.getActivity().getApplicationContext(), name);
	}

	@Override
	public Iterator<Entry<String, Tags>> iterator() {
		return Collections.<Entry<String, Tags>> emptySet().iterator();
	}

	@Override
	public void add(String word, Tags tags) {
		helper.add(word, tags, null);
	}

	@Override
	public boolean containsKey(String word) {
		return helper.containsKey(word);
	}

	@Override
	public Tags get(String word) {
		return helper.get(word);
	}

	@Override
	protected void add(String word, Tags tags, FreehalFile filename) {
		helper.add(word, tags, filename);
	}

	@Override
	public boolean add(FreehalFile filename) {
		helper.beginTransaction();
		boolean result = super.add(filename);
		helper.endTransaction();
		return result;
	}

	public static Factory<TagContainer, String> newFactory() {
		return new Factory<TagContainer, String>() {
			@Override
			public TagContainer newInstance(String b) {
				return new SqliteTagTable(b);
			}
		};
	}

	private static class TagMapSqliteHelper extends SQLiteOpenHelper {

		protected static final int DATABASE_VERSION = 2;
		protected static final String DATABASE_NAME = "tagger";
		protected static String TABLE_NAME;
		private static String TABLE_CREATE;
		private SQLiteDatabase db;

		TagMapSqliteHelper(Context context, String name) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			TABLE_NAME += "tags_" + name;
			TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (hashcode INTEGER not null, word TEXT not null,"
					+ " category TEXT not null, gender TEXT not null," + " filename TEXT not null,"
					+ " PRIMARY KEY (hashcode, word, filename));";
		}

		public void beginTransaction() {
			init();
			db.beginTransaction();
		}

		public void endTransaction() {
			init();
			db.endTransaction();
		}

		public Tags get(String word) {
			LogUtils.i("read from tag map: word=" + word);

			try {
				final String selectQuery = "SELECT category, gender FROM " + TABLE_NAME
						+ " WHERE hashcode = " + word.hashCode() + " and word = \"" + word + "\"";
				init();
				Cursor cursor = db.rawQuery(selectQuery, null);

				if (cursor.moveToFirst()) {
					do {
						Tags tags = new Tags(cursor.getString(0), cursor.getString(1));
						LogUtils.i("result: tags=" + tags);
						return tags;

					} while (cursor.moveToNext());
				}

				return null;
			} catch (SQLiteException e) {
				LogUtils.e(e);
				return null;
			}
		}

		public boolean containsKey(String word) {
			return get(word) != null;
		}

		public void add(String word, Tags tags, FreehalFile filename) {
			LogUtils.i("add to tag map: word=" + word + ", tags=" + tags + ", filename=" + filename);

			try {
				init();
				ContentValues contentValues = new ContentValues();
				contentValues.put("hashcode", word.hashCode());
				contentValues.put("word", word);
				contentValues.put("category", tags.hasCategory() ? tags.getCategory() : "");
				contentValues.put("gender", tags.hasGender() ? tags.getGender() : "");
				contentValues.put("filename", filename != null ? filename.getName() : "");

				@SuppressWarnings("unused")
				long affectedColumnId = db.insertWithOnConflict(TABLE_NAME, null, contentValues,
						SQLiteDatabase.CONFLICT_IGNORE);

			} catch (SQLiteException e) {
				LogUtils.e(e);
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {}

		private void init() {
			if (db != null)
				if (!db.isOpen())
					db = null;
			if (db == null)
				db = this.getWritableDatabase();
		}
	}
}
