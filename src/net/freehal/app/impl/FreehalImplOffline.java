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
import java.util.ArrayList;
import java.util.List;

import net.freehal.app.compat.android.FileUtilsAndroid;
import net.freehal.app.compat.android.FreehalConfigAndroid;
import net.freehal.app.compat.android.LogUtilsAndroid;
import net.freehal.app.util.ExecuteLater;
import net.freehal.app.util.Util;
import net.freehal.compat.sunjava.LogUtilsStandard;
import net.freehal.core.answer.AnswerProvider;
import net.freehal.core.answer.AnswerProviders;
import net.freehal.core.database.DatabaseAnswerProvider;
import net.freehal.core.database.DatabaseImpl;
import net.freehal.core.database.DiskDatabase;
import net.freehal.core.filter.FactFilters;
import net.freehal.core.filter.FilterNoNames;
import net.freehal.core.filter.FilterNot;
import net.freehal.core.filter.FilterQuestionExtra;
import net.freehal.core.filter.FilterQuestionWhat;
import net.freehal.core.filter.FilterQuestionWho;
import net.freehal.core.grammar.AbstractGrammar;
import net.freehal.core.lang.english.EnglishGrammar;
import net.freehal.core.lang.english.EnglishParser;
import net.freehal.core.lang.english.EnglishPhrase;
import net.freehal.core.lang.english.EnglishPredefinedAnswerProvider;
import net.freehal.core.lang.english.EnglishTagger;
import net.freehal.core.lang.german.GermanGrammar;
import net.freehal.core.lang.german.GermanParser;
import net.freehal.core.lang.german.GermanPhrase;
import net.freehal.core.lang.german.GermanPredefinedAnswerProvider;
import net.freehal.core.lang.german.GermanRandomAnswerProvider;
import net.freehal.core.lang.german.GermanTagger;
import net.freehal.core.parser.AbstractParser;
import net.freehal.core.parser.Sentence;
import net.freehal.core.phrase.AbstractPhrase;
import net.freehal.core.pos.AbstractTagger;
import net.freehal.core.pos.TaggerCache;
import net.freehal.core.pos.TaggerCacheDisk;
import net.freehal.core.util.FileUtils;
import net.freehal.core.util.FreehalConfig;
import net.freehal.core.util.LogUtils;
import net.freehal.core.util.StringUtils;

public class FreehalImplOffline extends FreehalImpl {

	private static FreehalImplOffline instance;

	private File centralLogFile = null;

	private String input;
	private String output;

	private FreehalImplOffline() {
		init();
	}

	private void init() {
		// file access
		FileUtils.set(new FileUtilsAndroid());

		// set the language and the base directory (if executed in "bin/", the
		// base directory is ".."). Freehal expects a "lang_xy" directory there
		// which contains the database files.
		FreehalConfig.set(new FreehalConfigAndroid());

		// how and where to print the log
		LogUtilsStandard log = new LogUtilsStandard();
		log.to(LogUtilsAndroid.AndroidLogStream.create());
		log.to(LogUtilsStandard.FileLogStream.create(centralLogFile = new File(FreehalConfig.getPath(),
				"stdout.txt")));
		LogUtils.set(log);

		ExecuteLater later = new ExecuteLater(0) {
			@Override
			public void run() {}

			@Override
			protected Void doInBackground(Void... params) {

				// Runtime.getRuntime().

				// unpack the zip file which contains the standard database
				Util.unpackZip(
						Util.getActivity().getResources().openRawResource(net.freehal.app.R.raw.database),
						FreehalConfig.getPath());

				final boolean isGerman = FreehalConfig.getLanguage().equals("de");

				// initialize the grammar
				// (also possible: EnglishGrammar, GermanGrammar, FakeGrammar)
				AbstractGrammar grammar = isGerman ? new GermanGrammar() : new EnglishGrammar();
				grammar.readGrammar(new File("grammar.txt"));
				FreehalConfig.setGrammar(grammar);

				// initialize the part of speech tagger
				// (also possible: EnglishTagger, GermanTagger, FakeTagger)
				// the parameter is either a TaggerCacheMemory (faster, higher
				// memory usage) or a TaggerCacheDisk (slower, less memory
				// usage)
				TaggerCache cache = new TaggerCacheDisk();
				AbstractTagger tagger = isGerman ? new GermanTagger(cache) : new EnglishTagger(cache);
				tagger.readTagsFrom(new File("guessed.pos"));
				tagger.readTagsFrom(new File("brain.pos"));
				tagger.readTagsFrom(new File("memory.pos"));
				tagger.readRegexFrom(new File("regex.pos"));
				tagger.readToggleWordsFrom(new File("toggle.csv"));
				FreehalConfig.setTagger(tagger);

				// how to phrase the output sentences
				// (also possible: EnglishPhrase, GermanPhrase, FakePhrase)
				AbstractPhrase phrase = isGerman ? new GermanPhrase() : new EnglishPhrase();
				FreehalConfig.setPhrase(phrase);

				// initialize the database
				// (also possible: DiskDatabase, FakeDatabase)
				DatabaseImpl database = new DiskDatabase();
				// set the maximum amount of facts to cache
				DiskDatabase.setMemoryLimit(500);
				
				// while updating the cache, a cache_xy/ directory will be
				// filled with information from the database files in lang_xy/
				database.updateCache();

				// Freehal has different ways to find an answer for an input
				AnswerProviders
						.getInstance()
						.add(isGerman ? new GermanPredefinedAnswerProvider()
								: new EnglishPredefinedAnswerProvider())
						.add(new DatabaseAnswerProvider(database))
						.add(isGerman ? new GermanRandomAnswerProvider() : null)
						.add(new FakeAnswerProvider());

				// fact filters are used to filter the best-matching fact in the
				// database
				FactFilters.getInstance().add(new FilterNot()).add(new FilterNoNames())
						.add(new FilterQuestionWho()).add(new FilterQuestionWhat())
						.add(new FilterQuestionExtra());

				return null;
			}
		};
		later.execute();
	}

	public static FreehalImpl getInstance() {
		if (instance == null)
			instance = new FreehalImplOffline();
		return instance;
	}

	@Override
	public void setInput(String input) {
		this.input = input;
	}

	@Override
	public void compute() {
		// also possible: EnglishParser, GermanParser, FakeParser
		AbstractParser p = FreehalConfig.getLanguage().equals("de") ? new GermanParser(input)
				: new EnglishParser(input);

		// parse the input and get a list of sentences
		final List<Sentence> inputParts = p.getSentences();

		List<String> outputParts = new ArrayList<String>();
		// for each sentence...
		for (Sentence s : inputParts) {
			// get the answer using the AnswerProvider API
			outputParts.add(AnswerProviders.getInstance().getAnswer(s));
		}
		// put all answers together
		output = StringUtils.join(" ", outputParts);
		LogUtils.i("Input: " + input);
		LogUtils.i("Output: " + output);
	}

	@Override
	public String getOutput() {
		return output;
	}

	@Override
	public String getLog() {
		return FileUtils.read(centralLogFile);
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
}

class FakeAnswerProvider implements AnswerProvider {

	@Override
	public String getAnswer(Sentence s) {
		return "Hello World!";
	}

}
