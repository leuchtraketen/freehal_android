package net.freehal.app.offline;

import java.util.ArrayList;
import java.util.List;

import net.freehal.app.util.AndroidUtils;
import net.freehal.compat.android.AndroidCompatibility;
import net.freehal.compat.android.AndroidLogUtils;
import net.freehal.compat.android.SqliteFile;
import net.freehal.compat.sunjava.StandardFreehalFile;
import net.freehal.compat.sunjava.StandardHttpClient;
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
import net.freehal.core.pos.Tags;
import net.freehal.core.pos.storage.TagContainer;
import net.freehal.core.pos.storage.TagDatabase;
import net.freehal.core.storage.KeyValueDatabase;
import net.freehal.core.storage.Serializer;
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
import net.freehal.core.xml.XmlFact;
import net.freehal.plugin.berkeleydb.BerkeleyDb;
import net.freehal.plugin.berkeleydb.BerkeleyFile;
import net.freehal.plugin.wikipedia.GermanWikipedia;
import net.freehal.plugin.wikipedia.WikipediaClient;
import net.freehal.plugin.wikipedia.WikipediaPlugin;

public class OfflineImplementation {

	private static FreehalFile centralLogFile = null;
	private static boolean isInitialized = false;

	public OfflineImplementation() {}

	public static void register() {
		// file access: use the android sqlite API for all files with
		// "sqlite://" protocol, a Berkeley DB for the berkeley:// protocol and
		// a real file for all other protocols
		FreehalFiles.add(FreehalFiles.ALL_PROTOCOLS, StandardFreehalFile.newFactory());
		FreehalFiles.add("sqlite", SqliteFile.newFactory());
		FreehalFiles.add("berkeley", BerkeleyFile.newFactory());
		FreehalFiles.add("http", StandardHttpClient.newFactory());
		FreehalFiles.add("wikipedia", WikipediaClient.newFactory());

		// set the language and the base directory (if executed in "bin/", the
		// base directory is ".."). Freehal expects a "lang_xy" directory there
		// which contains the database files.
		Languages.setLanguage(AndroidCompatibility.getLanguage());
		Storages.setStorage(new StandardStorage(AndroidCompatibility.getPath()));

		// how and where to print the log
		StandardLogUtils log = new StandardLogUtils();
		log.to(AndroidLogUtils.AndroidLogStream.create());
		// .addFilter("xml", LogUtils.DEBUG).addFilter("filter", LogUtils.DEBUG)
		log.to(StandardLogUtils.FileLogStream.create((centralLogFile = Storages.inPath("stdout.txt"))
				.getFile()));
		LogUtils.set(log);
	}

	public static synchronized void initialize() {
		// now, as logging and file system stuff is set up, start the real
		// initialization process!
		LogUtils.startProgress("init");

		LogUtils.updateProgress("unpacking internal database files to sdcard...");

		final String thisVersion = "1";
		// check whether the standard database files have been unpacked
		FreehalFile versionFile = Storages.getPath().getChild(".version");
		final String currentVersion = versionFile.read();
		if (currentVersion == null || !thisVersion.equals(currentVersion)) {
			// unpack the zip file which contains the standard database
			AndroidUtils.unpackZip(net.freehal.app.R.raw.database, Storages.getPath());
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

		// this database is shared by several classes for storing metadata
		KeyValueDatabase<String> meta = new BerkeleyDb<String>(Storages.getCacheDirectory().getChild("meta"),
				new Serializer.StringSerializer());

		// initialize the part of speech tagger
		// (also possible: EnglishTagger, GermanTagger, FakeTagger)
		// the parameter is either a TaggerCacheMemory (faster, higher
		// memory usage) or a TaggerCacheDisk (slower, less memory
		// usage)
		KeyValueDatabase<Tags> tags = new BerkeleyDb<Tags>(Storages.getCacheDirectory().getChild("tagger"),
				new Tags.StringSerializer());
		Factory<TagContainer, String> cacheFactory = TagDatabase.newFactory(tags, meta);

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
		KeyValueDatabase<Iterable<XmlFact>> factsCache = new BerkeleyDb<Iterable<XmlFact>>(Storages
				.getCacheDirectory().getChild("database/facts"), new XmlFact.StringSerializer());
		FactIndex facts = new FactIndex(factsCache);
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
		// set the maximum amount of facts to hold in memory
		FactIndex.setMemoryLimit(1500);
		// update the cache of that database...
		// while updating the cache, a cache_xy/ directory will be
		// filled with
		// information from the database files in lang_xy/
		database.updateCache();

		tags.compress();
		factsCache.compress();
		meta.compress();

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

		LogUtils.stopProgress();

		isInitialized = true;
	}

	public static FreehalFile getLogFile() {
		return centralLogFile;
	}

	public static String compute(String input) {
		if (!isInitialized)
			initialize();
		android.os.Debug.startMethodTracing("answer");

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
		final String output = StringUtils.join(" ", outputParts);
		LogUtils.i("Input: " + input);
		LogUtils.i("Output: " + output);

		LogUtils.stopProgress();
		android.os.Debug.stopMethodTracing();

		return output;
	}

}

class FakeAnswerProvider implements AnswerProvider {

	@Override
	public String getAnswer(Sentence s) {
		return "Hello World!";
	}

}
