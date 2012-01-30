package be.amedee.twitterscripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

final class MyTwitterAdapter extends TwitterAdapter {
	
	private List<String> wordlist;

	public MyTwitterAdapter() {
		wordlist = initWordList();
	}
	
	@Override
	public void onException(TwitterException te, TwitterMethod method) {
		// Can't do anything useful here, so release the lock.
		CleanTimeline.LOCK.notify();
	}

	@Override
	public void gotUserTimeline(ResponseList<Status> statuses) {
		for (Status status : statuses) {
			destroyWorkhourTweets(status);
			destroyForbiddenWordsTweets(status, wordlist);
		}
		synchronized (CleanTimeline.LOCK) {
			CleanTimeline.LOCK.notify();
		}
	}

	private List<String> initWordList() {
		List<String> words = new ArrayList<String>();
		words.add("Arcelor");
		words.add("Zaventem");
		words.add("Zelzate");
		words.add("@slecluyse");
		words.add("@ldeneef");
		words.add("Anon");
		words.add("werkgever");
		words.add("vakbond");
		words.add("#30J");
		words.add("staken");
		words.add("staak");
		words.add("staking");
		words.add("ABVV");
		words.add("firma");
		words.add("Econocom");
		words.add("CSC");
		words.add("Cheops");
		words.add("DYMO");
		words.add("@");
		return words;
	}

	private void destroyForbiddenWordsTweets(Status status, List<String> wordlist) {
		for (String word : wordlist) {
			int index = status.getText().toLowerCase().indexOf(word.toLowerCase());
			if (index>-1) {
				destroyStatus(status);
			}
		}
		
	}

	/**
	 * @param status
	 */
	private void destroyWorkhourTweets(Status status) {
		Calendar baseCal = Calendar.getInstance();
		Date statusDate = status.getCreatedAt();
		baseCal.setTime(DateUtils.truncate(statusDate, Calendar.DATE));
		int dayOfWeek = baseCal.get(Calendar.DAY_OF_WEEK);
		boolean onWeekend = dayOfWeek == Calendar.SATURDAY
				|| dayOfWeek == Calendar.SUNDAY;
		if (!onWeekend) {
			baseCal.setTime(DateUtils.truncate(statusDate,
					Calendar.HOUR_OF_DAY));
			int hourOfDay = baseCal.get(Calendar.HOUR_OF_DAY);
			boolean workHours = (hourOfDay >= 9 && hourOfDay < 12)
					|| (hourOfDay >= 13 && hourOfDay < 17);
			if (workHours) {
				destroyStatus(status);
			}

		}
	}

	@Override
	public void destroyedStatus(Status status) {
		System.out.println("Tweet deleted: " + status.getCreatedAt() + " - " + status.getId() + ": " + status.getText());
	}

	/**
	 * @param status
	 */
	private void destroyStatus(Status status) {
		CleanTimeline.twitter.destroyStatus(status.getId());
	}
}