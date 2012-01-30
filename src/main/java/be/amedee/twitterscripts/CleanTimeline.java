package be.amedee.twitterscripts;

import java.io.Serializable;


import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Paging;

/**
 * Remove all tweets that were done during working hours.
 * 
 */
public class CleanTimeline implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2713543043133463656L;
	static final Object LOCK = new Object();
	static AsyncTwitter twitter;

	public static void main(String[] args) throws InterruptedException {
		twitter = new AsyncTwitterFactory().getInstance();
		twitter.addListener(new MyTwitterAdapter());
		for (int page = 1; page <= 100; page++) {
			Paging paging = new Paging(page, 200);
			twitter.getUserTimeline(paging);
			synchronized (LOCK) {
				LOCK.wait();
			}
		}
	}
}
