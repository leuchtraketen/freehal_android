package net.freehal.compat.android;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.freehal.app.util.Util;
import net.freehal.core.util.AbstractFreehalFile;
import net.freehal.core.util.FreehalFile;

public class SqliteFreehalFile extends AbstractFreehalFile {

	private static FreehalSqliteHelper helper = null;

	private void init() {
		if (helper == null) {
			helper = new FreehalSqliteHelper(Util.getActivity().getApplicationContext());
		}
	}

	public SqliteFreehalFile(File file) {
		super(file);
	}

	@Override
	public FreehalFile getFile(String path) {
		return new SqliteFreehalFile(new File(path));
	}

	@Override
	public FreehalFile getChild(String file) {
		return new SqliteFreehalFile(new File(this.getAbsolutePath(), file));
	}

	@Override
	public FreehalFile getChild(FreehalFile file) {
		return new SqliteFreehalFile(new File(this.getAbsolutePath(), file.getPath()));
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
	public List<String> readLinesAsList() {
		init();
		return Arrays.asList(helper.read(this).split("[\r\n]+"));
	}

	@Override
	public void write(final String content) {
		init();
		helper.write(this, content);
	}

}
