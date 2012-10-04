package net.freehal.compat.android;

import java.io.File;
import java.util.Collection;

import net.freehal.core.util.AbstractFreehalFile;
import net.freehal.core.util.FileUtilsImpl;
import net.freehal.core.util.FreehalFile;

public class SqliteFreehalFile extends AbstractFreehalFile {

	private static final FileUtilsImpl util = new SqliteFileUtils();

	public SqliteFreehalFile(File file) {
		super(file);
	}

	@Override
	public FreehalFile create(String path) {
		return new SqliteFreehalFile(new File(path));
	}

	@Override
	public FreehalFile create(String dir, String file) {
		return new SqliteFreehalFile(new File(dir, file));
	}

	@Override
	public boolean delete() {
		util.delete(this);
		return true;
	}

	@Override
	public FileUtilsImpl getFileUtilsImpl() {
		return util;
	}

	@Override
	public boolean isDirectory() {
		return SqliteFileUtils.getHelper().isDirectory(this);
	}

	@Override
	public boolean isFile() {
		return SqliteFileUtils.getHelper().isFile(this);
	}

	@Override
	public long length() {
		return SqliteFileUtils.getHelper().length(this);
	}

	@Override
	public FreehalFile[] listFiles() {
		Collection<FreehalFile> files = SqliteFileUtils.getHelper().listFiles(this);
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
}
