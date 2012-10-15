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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import net.freehal.app.FreehalService;
import net.freehal.app.util.Util;
import net.freehal.compat.android.AndroidCompatibility;
import net.freehal.compat.android.AndroidLogUtils;
import net.freehal.compat.android.SqliteFreehalFile;
import net.freehal.compat.android.SqliteTagTable;
import net.freehal.compat.sunjava.StandardFreehalFile;
import net.freehal.compat.sunjava.StandardLogUtils;
import net.freehal.core.answer.AnswerProvider;
import net.freehal.core.answer.AnswerProviders;
import net.freehal.core.database.Database;
import net.freehal.core.database.DatabaseAnswerProvider;
import net.freehal.core.database.DirectoryUtils;
import net.freehal.core.database.FactIndex;
import net.freehal.core.database.StandardDatabase;
import net.freehal.core.database.SynonymIndex;
import net.freehal.core.filter.FactFilters;
import net.freehal.core.filter.FilterNoNames;
import net.freehal.core.filter.FilterNot;
import net.freehal.core.filter.FilterQuestionExtra;
import net.freehal.core.filter.FilterQuestionWhat;
import net.freehal.core.filter.FilterQuestionWho;
import net.freehal.core.grammar.Grammar;
import net.freehal.core.grammar.Grammars;
import net.freehal.core.lang.Languages;
import net.freehal.core.lang.english.EnglishGrammar;
import net.freehal.core.lang.english.EnglishParser;
import net.freehal.core.lang.english.EnglishPredefinedAnswerProvider;
import net.freehal.core.lang.english.EnglishTagger;
import net.freehal.core.lang.english.EnglishWording;
import net.freehal.core.lang.german.GermanGrammar;
import net.freehal.core.lang.german.GermanParser;
import net.freehal.core.lang.german.GermanPredefinedAnswerProvider;
import net.freehal.core.lang.german.GermanRandomAnswerProvider;
import net.freehal.core.lang.german.GermanTagger;
import net.freehal.core.lang.german.GermanWording;
import net.freehal.core.parser.Parser;
import net.freehal.core.parser.Sentence;
import net.freehal.core.pos.Tagger;
import net.freehal.core.pos.Taggers;
import net.freehal.core.pos.storage.DiskTagCachedReader;
import net.freehal.core.pos.storage.TagContainer;
import net.freehal.core.storage.StandardStorage;
import net.freehal.core.storage.Storages;
import net.freehal.core.util.Factory;
import net.freehal.core.util.FreehalFile;
import net.freehal.core.util.FreehalFiles;
import net.freehal.core.util.LogUtils;
import net.freehal.core.util.StringUtils;
import net.freehal.core.wording.Wording;
import net.freehal.core.wording.Wordings;
import net.freehal.core.xml.FactProviders;
import net.freehal.core.xml.SynonymProviders;
import net.freehal.plugin.wikipedia.GermanWikipedia;
import net.freehal.plugin.wikipedia.WikipediaPlugin;

public class FreehalImplOffline extends FreehalImpl {

	private static FreehalImplOffline instance;

	public static FreehalImpl getInstance() {
		if (instance == null)
			instance = new FreehalImplOffline();
		return instance;
	}

	FreehalService mService = null;
	boolean mBound = false;

	private File centralLogFile = null;
	private boolean isInitialized = false;

	private String input;
	private String output;

	private FreehalImplOffline() {
		// file access: use the android sqlite API for all files with
		// "sqlite://" protocol, and a normal file for all other protocols
		FreehalFiles.add(FreehalFiles.ALL_PROTOCOLS, StandardFreehalFile.newFactory());
		FreehalFiles.add("sqlite", SqliteFreehalFile.newFactory());

		// set the language and the base directory (if executed in "bin/", the
		// base directory is ".."). Freehal expects a "lang_xy" directory there
		// which contains the database files.
		Languages.setLanguage(AndroidCompatibility.getLanguage());
		Storages.setStorage(new StandardStorage(AndroidCompatibility.getPath()));

		// how and where to print the log
		StandardLogUtils log = new StandardLogUtils();
		log.to(AndroidLogUtils.AndroidLogStream.create());
		log.to(StandardLogUtils.FileLogStream
				.create(centralLogFile = Storages.inPath("stdout.txt").getFile()));
		LogUtils.set(log);
	}

	public synchronized void initialize() {
		// now, as logging and file system stuff is set up, start the real
		// initialization process!
		bind();
		LogUtils.startProgress("init");

		LogUtils.updateProgress("unpacking internal database files to sdcard...");

		final String thisVersion = "1";
		// check whether the standard database files have been unpacked
		FreehalFile versionFile = Storages.getPath().getChild(".version");
		final String currentVersion = versionFile.read();
		if (currentVersion == null || !thisVersion.equals(currentVersion)) {
			// unpack the zip file which contains the standard database
			Util.unpackZip(net.freehal.app.R.raw.database, Storages.getPath());
			// write the version file
			versionFile.write(thisVersion);
		}

		LogUtils.updateProgress("initializing grammar...");

		final boolean isGerman = Languages.getLanguage().equals("de");

		// initialize the grammar
		// (also possible: EnglishGrammar, GermanGrammar, FakeGrammar)
		Grammar grammar = isGerman ? new GermanGrammar() : new EnglishGrammar();
		grammar.readGrammar(FreehalFiles.getFile("grammar.txt"));
		Grammars.setGrammar(grammar);

		LogUtils.updateProgress("initializing part of speech tagger...");

		// initialize the part of speech tagger
		// (also possible: EnglishTagger, GermanTagger, FakeTagger)
		// the parameter is either a TaggerCacheMemory (faster, higher
		// memory usage) or a TaggerCacheDisk (slower, less memory
		// usage)
		Factory<TagContainer, String> cacheFactory = SqliteTagTable.newFactory();
		Tagger tagger = isGerman ? new GermanTagger(cacheFactory) : new EnglishTagger(cacheFactory);
		tagger.readTagsFrom(FreehalFiles.getFile("guessed.pos"));
		tagger.readTagsFrom(FreehalFiles.getFile("brain.pos"));
		tagger.readTagsFrom(FreehalFiles.getFile("memory.pos"));
		tagger.readRegexFrom(FreehalFiles.getFile("regex.pos"));
		tagger.readToggleWordsFrom(FreehalFiles.getFile("toggle.csv"));
		Taggers.setTagger(tagger);

		LogUtils.updateProgress("updating database cache...");

		// how to phrase the output sentences
		// (also possible: EnglishPhrase, GermanPhrase, FakePhrase)
		Wording phrase = isGerman ? new GermanWording() : new EnglishWording();
		Wordings.setWording(phrase);

		// we need to store facts...
		FactIndex facts = new FactIndex();
		// ... and synonyms
		SynonymIndex synonyms = new SynonymIndex();
		// add both to their utility classes
		FactProviders.addFactProvider(facts);
		SynonymProviders.addSynonymProvider(synonyms);
		// both are components of a database!
		Database database = new StandardDatabase();
		database.addComponent(facts);
		database.addComponent(synonyms);
		// set the default key size for the database cache indices
		DirectoryUtils.Key.setGlobalKeyLength(2);
		// update the cache of that database...
		// while updating the cache, a cache_xy/ directory will be
		// filled with
		// information from the database files in lang_xy/
		database.updateCache();

		LogUtils.updateProgress("set up plugins...");

		// the Wikipedia plugin is a FactProvider too!
		WikipediaPlugin wikipedia = new WikipediaPlugin(new GermanWikipedia());
		FactProviders.addFactProvider(wikipedia);

		// Freehal has different ways to find an answer for an input
		AnswerProviders.add(isGerman ? new GermanPredefinedAnswerProvider()
				: new EnglishPredefinedAnswerProvider());
		AnswerProviders.add(wikipedia);
		AnswerProviders.add(new DatabaseAnswerProvider(facts));
		AnswerProviders.add(isGerman ? new GermanRandomAnswerProvider() : null);
		AnswerProviders.add(new FakeAnswerProvider());

		// fact filters are used to filter the best-matching fact in the
		// database
		FactFilters.getInstance().add(new FilterNot()).add(new FilterNoNames()).add(new FilterQuestionWho())
				.add(new FilterQuestionWhat()).add(new FilterQuestionExtra());

		// android.os.Debug.stopMethodTracing();

		unbind();
		LogUtils.stopProgress();

		isInitialized = true;
	}

	private synchronized void bind() {
		// Bind to FreehalService
		Intent intent = new Intent(Util.getActivity().getApplicationContext(), FreehalService.class);
		Util.getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		while (!mBound)
			Util.sleep(1000);
	}

	private synchronized void unbind() {
		// Unbind from FreehalService
		if (mBound) {
			Util.getActivity().getApplicationContext().unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	public void setInput(String input) {
		this.input = input;
	}

	@Override
	public synchronized void compute() {
		if (!isInitialized)
			initialize();

		bind();
		LogUtils.startProgress("answer");
		LogUtils.updateProgress("find an answer for \"" + input + "\"");

		// also possible: EnglishParser, GermanParser, FakeParser
		Parser p = Languages.getLanguage().isCode("de") ? new GermanParser(input) : new EnglishParser(input);

		// parse the input and get a list of sentences
		final List<Sentence> inputParts = p.getSentences();

		List<String> outputParts = new ArrayList<String>();
		// for each sentence...
		for (Sentence s : inputParts) {
			// get the answer using the AnswerProvider API
			outputParts.add(AnswerProviders.getAnswer(s));
		}
		// put all answers together
		output = StringUtils.join(" ", outputParts);
		LogUtils.i("Input: " + input);
		LogUtils.i("Output: " + output);

		LogUtils.stopProgress();
		unbind();
	}

	@Override
	public String getOutput() {
		return output;
	}

	@Override
	public String getLog() {
		if (centralLogFile != null) {
			try {
				return new Scanner(centralLogFile).useDelimiter("\\Z").next();
			} catch (FileNotFoundException e) {
				LogUtils.e(e);
				return StringUtils.asString(e);
			}
		}
		return "";
	}

	@Override
	public String getGraph() {
		return null;
	}

	@Override
	public String getVersionName() {
		return "not installed";
	}

	@Override
	public int getVersionCode() {
		return -1;
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			FreehalService.LocalBinder binder = (FreehalService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
}

class FakeAnswerProvider implements AnswerProvider {

	@Override
	public String getAnswer(Sentence s) {
		return "Hello World!";
	}

}
