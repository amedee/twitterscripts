package be.amedee.twitterscripts;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class WordList {
	private static List<String> wordlist;

	public WordList() {
		initWordList();
	}

	@SuppressWarnings("unchecked")
	private List<String> initWordList() {
		Configuration config = null;
		try {
			config = new PropertiesConfiguration("CleanTimeline.properties");
		} catch (ConfigurationException ce) {
			ce.printStackTrace();
		}
		List<String> words = config.getList("forbiddenwords");
		return words;
	}

	public List<String> getWordlist() {
		return wordlist;
	}
}