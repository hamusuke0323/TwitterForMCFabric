package com.hamusuke.twitter4mc.utils;

import com.hamusuke.twitter4mc.Token;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import twitter4j.*;

import java.awt.*;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

@Environment(EnvType.CLIENT)
public class TwitterUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	public static StatusUpdate createReplyTweet(String tweet, Status replyTo) {
		return new StatusUpdate("@" + replyTo.getUser().getScreenName() + " " + tweet).inReplyToStatusId(replyTo.getId());
	}

	public static StatusUpdate createQuoteTweet(String comment, TweetSummary target) {
		return new StatusUpdate(comment).attachmentUrl(target.getTweetURL());
	}

	public static void saveToken(NewToken newToken, File tokenFile) throws Exception {
		Class_124611_a_.func_082122_a_(newToken, tokenFile);
	}

	public static NewToken readToken(File tokenFile) throws Exception {
		return Class_124611_a_.func_082113_b_(tokenFile);
	}

	public static NewToken oldTokenToNewTokenAndSave(Token old, File tokenFile) throws Exception {
		return Class_124611_a_.func_013341_f_(old, tokenFile);
	}

	public static JSONObject getReplyCount(Twitter twitter, long tweetId) throws TwitterException {
		HttpResponse httpResponse = HttpClientFactory.getInstance().get("https://api.twitter.com/2/tweets", new HttpParameter[]{new HttpParameter("ids", tweetId), new HttpParameter("tweet.fields", "public_metrics,author_id,conversation_id,created_at,in_reply_to_user_id,referenced_tweets"), new HttpParameter("expansions", "author_id,in_reply_to_user_id,referenced_tweets.id"), new HttpParameter("user.fields", "name,username")}, twitter.getAuthorization(), null);

		if (httpResponse != null) {
			return httpResponse.asJSONObject();
		}

		return new JSONObject();
	}

	@Nullable
	public static ReplyObject getReplies(Twitter twitter, long tweetId) throws TwitterException {
		return getReplies(twitter, tweetId, 10);
	}

	@Nullable
	public static ReplyObject getReplies(Twitter twitter, long tweetId, int maxResult) throws TwitterException {
		HttpResponse httpResponse = HttpClientFactory.getInstance().get("https://api.twitter.com/2/tweets/search/recent", new HttpParameter[]{new HttpParameter("expansions", "attachments.poll_ids,attachments.media_keys,author_id,entities.mentions.username,geo.place_id,in_reply_to_user_id,referenced_tweets.id,referenced_tweets.id.author_id"), new HttpParameter("query", "conversation_id:" + tweetId), new HttpParameter("tweet.fields", "author_id,conversation_id,created_at,entities,id,in_reply_to_user_id,public_metrics,referenced_tweets,reply_settings,text"), new HttpParameter("user.fields", "id,name,pinned_tweet_id,profile_image_url,protected,username,verified"), new HttpParameter("max_results", maxResult)}, twitter.getAuthorization(), null);
		return httpResponse != null ? new ReplyObject(httpResponse.asJSONObject()) : null;
	}

	public static String getChunkedNumber(int number) {
		return getChunkedNumber("" + number);
	}

	//123456 -> 123.4K
	public static String getChunkedNumber(String number) {
		try {
			Integer.parseInt(number);
			StringBuilder builder = new StringBuilder();
			int index = 0;
			int counter = 0;
			for (int i = number.length() - 1; i >= 0; i--) {
				builder.append(number.charAt(i));
				index++;
				if (index % 3 == 0 && index != number.length()) {
					builder.append('.');
					counter++;
					builder.delete(0, counter == 1 ? 2 : 4);
				}
			}
			builder.reverse();
			if (builder.length() >= 2 && builder.charAt(builder.length() - 2) == '.' && builder.charAt(builder.length() - 1) == '0') {
				builder.delete(builder.length() - 2, builder.length());
			}

			if (counter != 0) {
				builder.append(TwitterUtil.Unit.get(counter));
			}
			return builder.toString();
		} catch (RuntimeException e) {
			LOGGER.warn("Input: {} is not a number", number);
			return number;
		}
	}

	public static String getDifferenceTime(Calendar created, Calendar now) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(now.getTimeInMillis() - created.getTimeInMillis()));
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY) - 9;
		int minute = calendar.get(Calendar.MINUTE);
		if (year == 1970 && month == 1 && day == 1 && hour == 0 && minute == 0) {
			return I18n.translate("tw.seconds", calendar.get(Calendar.SECOND));
		} else if (year == 1970 && month == 1 && day == 1 && hour == 0) {
			return I18n.translate("tw.minutes", minute);
		} else if (year == 1970 && month == 1 && day == 1) {
			return I18n.translate("tw.hours", hour);
		} else if (year == 1970) {
			return I18n.translate("tw.month.day." + (created.get(Calendar.MONTH) + 1), created.get(Calendar.DAY_OF_MONTH));
		} else {
			return I18n.translate("tw.year.month.day." + (created.get(Calendar.MONTH) + 1), created.get(Calendar.YEAR), created.get(Calendar.DAY_OF_MONTH));
		}
	}

	public static String getTime(Calendar c) {
		int y = c.get(Calendar.YEAR);
		int mon = c.get(Calendar.MONTH);
		int d = c.get(Calendar.DAY_OF_MONTH);
		int h = c.get(Calendar.HOUR_OF_DAY);
		int m = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);
		return y + "/" + (mon < 10 ? "0" : "") + mon + "/" + (d < 10 ? "0" : "") + d + " " + (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
	}

	@Nullable
	public static String getHiBitrateVideoURL(MediaEntity media) {
		int i = 0, b = 0;
		if (media.getType().equals("video")) {
			MediaEntity.Variant[] variants = media.getVideoVariants();
			for (int j = 0; j < variants.length; j++) {
				MediaEntity.Variant v = variants[j];
				String url = v.getUrl();
				if (!url.contains("m3u8")) {
					int k = v.getBitrate();
					if (b < k) {
						b = k;
						i = j;
					}
				}
			}
			return variants[i].getUrl();
		}
		return null;
	}

	public static Dimension wrapImageSizeToMax(Dimension imageSize, Dimension boundary) {
		double ratio = Math.max(boundary.getWidth() / imageSize.getWidth(), boundary.getHeight() / imageSize.getHeight());
		return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
	}

	public static Dimension wrapImageSizeToMin(Dimension imageSize, Dimension boundary) {
		double ratio = Math.min(boundary.getWidth() / imageSize.getWidth(), boundary.getHeight() / imageSize.getHeight());
		return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
	}

	@Environment(EnvType.CLIENT)
	enum Unit {
		KILO('K', 1),
		MEGA('M', 2),
		GIGA('G', 3);

		private final char name;
		private final int split;

		Unit(char name, int split) {
			this.name = name;
			this.split = split;
		}

		static char get(int split) {
			for (TwitterUtil.Unit u : TwitterUtil.Unit.values()) {
				if (u.split == split) {
					return u.name;
				}
			}

			return Character.MIN_VALUE;
		}
	}
}
