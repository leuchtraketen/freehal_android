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
package net.freehal.compat.android;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.freehal.app.util.Util;
import net.freehal.core.util.FileUtilsImpl;
import net.freehal.core.util.FreehalFile;
import net.freehal.core.util.FreehalFiles;
import net.freehal.core.util.LogUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteFileUtils implements FileUtilsImpl {

	private static FreehalSqlHelper helper = null;

	private static void init() {
		if (helper == null) {
			helper = new FreehalSqlHelper(Util.getActivity().getApplicationContext());
		}
	}

	public SqliteFileUtils() {}

	@Override
	public void append(FreehalFile filename, String content) {
		init();
		helper.append(filename, content);
	}

	@Override
	public void delete(FreehalFile filename) {
		init();
		helper.delete(filename);
	}

	@Override
	public String read(FreehalFile filename) {
		init();
		return helper.read(filename);
	}

	@Override
	public Iterable<String> readLines(FreehalFile filename) {
		init();
		LogUtils.i("readLines: " + filename);
		return Arrays.asList(helper.read(filename).split("[\r\n]+"));
	}

	@Override
	public List<String> readLinesAsList(FreehalFile filename) {
		init();
		LogUtils.i("readLinesAsList: " + filename);
		return Arrays.asList(helper.read(filename).split("[\r\n]+"));
	}

	@Override
	public void write(FreehalFile filename, final String content) {
		init();
		LogUtils.i("write: " + filename);
		helper.write(filename, content);
	}

	public static FreehalSqlHelper getHelper() {
		init();
		return helper;
	}

	public static class FreehalSqlHelper extends SQLiteOpenHelper {

		protected static final int DATABASE_VERSION = 2;
		protected static final String DATABASE_NAME = "databasefiles";
		protected static final String DICTIONARY_TABLE_NAME = "files";
		private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE " + DICTIONARY_TABLE_NAME
				+ " (hashcode INTEGER not null, part INTEGER AUTO_INCREMENT, "
				+ "file TEXT not null, content TEXT not null, " + "PRIMARY KEY (hashcode, part));";
		private SQLiteDatabase db;

		FreehalSqlHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DICTIONARY_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {}

		private void init() {
			if (this.db != null)
				if (!this.db.isOpen())
					this.db = null;
			if (this.db == null)
				this.db = this.getWritableDatabase();
		}

		public boolean isFile(FreehalFile filename) {
			LogUtils.i("isFile: " + filename);

			try {
				final String selectQuery = "SELECT 1 FROM files WHERE hashcode = "
						+ filename.getAbsolutePath().hashCode();
				init();

				Cursor cursor = db.rawQuery(selectQuery, null);

				if (cursor.moveToFirst())
					return true;
				else
					return false;

			} catch (SQLiteException e) {
				LogUtils.e(e);
				return false;
			}
		}

		public boolean isDirectory(FreehalFile filename) {
			LogUtils.i("isDirectory: " + filename);

			String path = filename.getAbsolutePath();
			if (!path.endsWith("/"))
				path = path + "/";

			try {
				final String selectQuery = "SELECT 1 FROM files WHERE file LIKE \"" + path + "%\"";
				init();
				Cursor cursor = db.rawQuery(selectQuery, null);

				if (cursor.moveToFirst())
					return true;
				else
					return false;

			} catch (SQLiteException e) {
				LogUtils.e(e);
				return false;
			}
		}

		public String read(FreehalFile filename) {
			LogUtils.i("read: " + filename);

			try {
				final String selectQuery = "SELECT content FROM files WHERE hashcode = "
						+ filename.getAbsolutePath().hashCode();
				init();
				Cursor cursor = db.rawQuery(selectQuery, null);

				StringBuilder content = new StringBuilder();

				if (cursor.moveToFirst()) {
					do {

						content.append(cursor.getString(0));
					} while (cursor.moveToNext());
				}

				return content.toString();
			} catch (SQLiteException e) {
				LogUtils.e(e);
				return "";
			}
		}

		public void write(final FreehalFile filename, final String content) {
			LogUtils.i("write: " + filename);
			/*
			 * Util.getActivity().runOnUiThread(new Runnable() {
			 * @Override public void run() {
			 * Toast.makeText(Util.getActivity().getApplicationContext(),
			 * "write: " + filename, Toast.LENGTH_SHORT).show(); } });
			 */

			try {
				delete(filename);
				append(filename, content);
			} catch (SQLiteException e) {
				LogUtils.e(e);
			}
		}

		public void append(FreehalFile filename, final String content) {
			LogUtils.i("append: " + filename);

			try {
				init();
				ContentValues contentValues = new ContentValues();
				contentValues.put("hashcode", filename.getAbsolutePath().hashCode());
				contentValues.put("file", filename.getAbsolutePath());
				contentValues.put("content", content);
				@SuppressWarnings("unused")
				long affectedColumnId = db.insert("files", null, contentValues);

			} catch (SQLiteException e) {
				LogUtils.e(e);
			}
		}

		public boolean delete(FreehalFile filename) {
			LogUtils.i("delete: " + filename);

			try {
				@SuppressWarnings("unused")
				final String deleteQuery = "DELETE FROM files WHERE hashcode = "
						+ filename.getAbsolutePath().hashCode();
				init();
				// db.delete("files", "file = \"" + filename.getAbsolutePath() +
				// "\"", null);
				db.delete("files", "hashcode = " + filename.getAbsolutePath().hashCode(), null);
				return true;
			} catch (SQLiteException e) {
				LogUtils.e(e);
				return false;
			}
		}

		public Collection<FreehalFile> listFiles(FreehalFile filename) {
			LogUtils.i("listFiles: " + filename);

			String path = filename.getAbsolutePath();
			if (!path.endsWith("/"))
				path = path + "/";

			Set<FreehalFile> files = new HashSet<FreehalFile>();
			try {
				final String selectQuery = "SELECT file FROM files WHERE file LIKE \"" + path + "%\"";
				init();
				Cursor cursor = db.rawQuery(selectQuery, null);

				if (cursor.moveToFirst()) {
					do {
						files.add(FreehalFiles.create(cursor.getString(0)));
					} while (cursor.moveToNext());
				}

			} catch (SQLiteException e) {
				LogUtils.e(e);
			}
			return files;
		}

		public long length(FreehalFile filename) {
			LogUtils.i("length: " + filename);

			try {
				final String selectQuery = "SELECT content FROM files WHERE hashcode = "
						+ filename.getAbsolutePath().hashCode();
				init();
				Cursor cursor = db.rawQuery(selectQuery, null);

				long length = 0;

				if (cursor.moveToFirst()) {
					do {
						length += cursor.getString(0).length();
					} while (cursor.moveToNext());
				}

				return length;
			} catch (SQLiteException e) {
				LogUtils.e(e);
				return 0;
			}
		}
	}

}
