package net.freehal.compat.android;

import java.io.File;
import java.util.Collection;

import net.freehal.core.util.AbstractFreehalFile;
import net.freehal.core.util.FileUtilsImpl;
import net.freehal.core.util.FreehalFile;

public class FreehalFileSqlite extends AbstractFreehalFile {

	private static final FileUtilsImpl util = new FileUtilsSqlite();

	public FreehalFileSqlite(File file) {
		super(file);
	}

	@Override
	public FreehalFile create(String path) {
		return new FreehalFileSqlite(new File(path));
	}

	@Override
	public FreehalFile create(String dir, String file) {
		return new FreehalFileSqlite(new File(dir, file));
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
		return FileUtilsSqlite.getHelper().isDirectory(this);
	}

	@Override
	public boolean isFile() {
		return FileUtilsSqlite.getHelper().isFile(this);
	}

	@Override
	public long length() {
		return FileUtilsSqlite.getHelper().length(this);
	}

	@Override
	public FreehalFile[] listFiles() {
		Collection<FreehalFile> files = FileUtilsSqlite.getHelper().listFiles(this);
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
