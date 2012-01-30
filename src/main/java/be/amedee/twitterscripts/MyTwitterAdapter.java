package be.amedee.twitterscripts;

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
	
	private WordList wordlist = new WordList();
	
	@Override
	public void onException(TwitterException te, TwitterMethod method) {
		// Can't do anything useful here, so release the lock.
		CleanTimeline.LOCK.notify();
	}

	@Override
	public void gotUserTimeline(ResponseList<Status> statuses) {
		for (Status status : statuses) {
			destroyWorkhourTweets(status);
			destroyForbiddenWordsTweets(status, wordlist.getWordlist());
		}
		synchronized (CleanTimeline.LOCK) {
			CleanTimeline.LOCK.notify();
		}
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