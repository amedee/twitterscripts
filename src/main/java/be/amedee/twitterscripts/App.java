package be.amedee.twitterscripts;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;

/**
 * Hello world!
 * 
 */
public class App {

	static final Object LOCK = new Object();

	public static void main(String[] args) throws InterruptedException {
		AsyncTwitter twitter = new AsyncTwitterFactory().getInstance();
		twitter.addListener(new TwitterAdapter() {

			public void gotHomeTimeline(ResponseList<Status> statuses) {
				System.out.println("Showing friends timeline.");
				for (Status status : statuses) {
					System.out.println(status.getUser().getName() + ":"
							+ status.getText());

				}
				synchronized (LOCK) {
					LOCK.notify();
				}
			}

		});

		twitter.getHomeTimeline();
		synchronized (LOCK) {
			LOCK.wait();
		}
	}
}
