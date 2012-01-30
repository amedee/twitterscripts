package be.amedee.twitterscripts;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

final class MyTwitterAdapter extends TwitterAdapter {
	
	@Override
	public void onException(TwitterException te, TwitterMethod method) {
		CleanTimeline.LOCK.notify();
//		if (method == UPDATE_STATUS) {
//			te.printStackTrace();
//			synchronized (RemoveWorkhoursTweets.LOCK) {
//				RemoveWorkhoursTweets.LOCK.notify();
//			}
//		} else {
//			synchronized (RemoveWorkhoursTweets.LOCK) {
//				RemoveWorkhoursTweets.LOCK.notify();
//			}
//			throw new AssertionError("Should not happen");
//		}
	}

	@Override
	public void gotUserTimeline(ResponseList<Status> statuses) {
		for (Status status : statuses) {
			destroyWorkhourTweets(status);
		}
		synchronized (CleanTimeline.LOCK) {
			CleanTimeline.LOCK.notify();
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