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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import net.freehal.app.util.Util;
import net.freehal.core.util.AbstractFreehalFile;
import net.freehal.core.util.Factory;
import net.freehal.core.util.FreehalFile;

public class SqliteFile extends AbstractFreehalFile {

	private static FreehalFileSqliteHelper helper = null;

	private void init() {
		if (helper == null) {
			helper = new FreehalFileSqliteHelper(Util.getActivity().getApplicationContext());
		}
	}

	private SqliteFile(File file) {
		super(file);
	}

	public static Factory<FreehalFile, String> newFactory() {
		return new Factory<FreehalFile, String>() {
			@Override
			public FreehalFile newInstance(String b) {
				return new SqliteFile(new File(b));
			}
		};
	}

	@Override
	public FreehalFile getChild(String file) {
		return new SqliteFile(new File(this.getAbsolutePath(), file));
	}

	@Override
	public FreehalFile getChild(FreehalFile file) {
		return new SqliteFile(new File(this.getAbsolutePath(), file.getPath()));
	}

	@Override
	public boolean delete() {
		init();
		helper.delete(this);
		return true;
	}

	@Override
	public boolean isDirectory() {
		init();
		return helper.isDirectory(this);
	}

	@Override
	public boolean isFile() {
		init();
		return helper.isFile(this);
	}

	@Override
	public long length() {
		init();
		return helper.length(this);
	}

	@Override
	public FreehalFile[] listFiles() {
		init();
		Collection<FreehalFile> files = helper.listFiles(this);
		return files.toArray(new FreehalFile[files.size()]);
	}

	@Override
	public boolean mkdirs() {
		// ignore on Android SQLite...
		return true;
	}

	@Override
	public String toString() {
		return "{sqlite://" + super.toString() + "}";
	}

	@Override
	public void append(String content) {
		init();
		helper.append(this, content);
	}

	@Override
	public String read() {
		init();
		return helper.read(this);
	}

	@Override
	public Iterable<String> readLines() {
		init();
		return Arrays.asList(helper.read(this).split("[\r\n]+"));
	}

	@Override
	public void write(final String content) {
		init();
		helper.write(this, content);
	}

	@Override
	public int countLines() {
		int countOfLines = 0;
		Iterable<String> lines = readLines();
		for (@SuppressWarnings("unused")
		String line : lines) {
			++countOfLines;
		}
		return countOfLines;
	}
}
