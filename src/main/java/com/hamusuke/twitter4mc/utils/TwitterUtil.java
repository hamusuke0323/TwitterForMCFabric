package com.hamusuke.twitter4mc.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.Token;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import twitter4j.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class TwitterUtil {
	private static final Logger LOGGER = LogManager.getLogger();

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

	public static int getArgb(int alpha, int red, int green, int blue) {
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	public static ImmutableList<Status> getHomeTimeline(Twitter twitter, int count) throws TwitterException {
		HttpResponse httpResponse = HttpClientFactory.getInstance().get("https://api.twitter.com/1.1/statuses/home_timeline.json", new HttpParameter[]{new HttpParameter("count", count)}, twitter.getAuthorization(), null);
		List<Status> statuses = Lists.newArrayList();
		if (httpResponse != null) {
			try {
				JSONArray jsonArray = httpResponse.asJSONArray();
				for (int i = 0; i < jsonArray.length(); i++) {
					statuses.add(TwitterObjectFactory.createStatus(jsonArray.getJSONObject(i).toString()));
				}
			} catch (Exception e) {
				throw new TwitterException(e);
			}
		}

		return ImmutableList.copyOf(statuses);
	}

	@Nullable
	public static ReplyObject getReplies(Twitter twitter, long tweetId) throws TwitterException {
		return getReplies(twitter, tweetId, 10);
	}

	@Nullable
	public static ReplyObject getReplies(Twitter twitter, long tweetId, int maxResult) throws TwitterException {
		HttpResponse httpResponse = HttpClientFactory.getInstance().get("https://api.twitter.com/2/tweets/search/recent", new HttpParameter[]{new HttpParameter("expansions", "attachments.poll_ids,attachments.media_keys,author_id,entities.mentions.username,geo.place_id,in_reply_to_user_id,referenced_tweets.id,referenced_tweets.id.author_id"), new HttpParameter("query", "conversation_id:" + tweetId), new HttpParameter("tweet.fields", "author_id,conversation_id,created_at,entities,id,in_reply_to_user_id,public_metrics,referenced_tweets,reply_settings,text"), new HttpParameter("user.fields", "id,name,pinned_tweet_id,profile_image_url,protected,username,verified"), new HttpParameter("max_results", maxResult)}, twitter.getAuthorization(), null);

		if (httpResponse != null) {
			return new ReplyObject(httpResponse.asJSONObject());
		}

		return null;
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
	public static List<String> getVideoURL(Twitter twitter, String url) {
		if (url.contains("status/")) {
			try {
				return TwitterUtil.getVideoURL(twitter, Long.parseLong(url.substring(url.indexOf("status/")).replace("status/", "").trim()));
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public static List<String> getVideoURL(Twitter twitter, long id) throws TwitterException {
		return TwitterUtil.getVideoURL(twitter.showStatus(id).getMediaEntities());
	}

	public static List<String> getVideoURL(MediaEntity[] medias) {
		List<String> urls = Lists.newArrayList();
		if (medias.length > 0) {
			MediaEntity media = medias[0];
			if (media.getType().equals("video")) {
				for (MediaEntity.Variant v : media.getVideoVariants()) {
					String url = v.getUrl();
					if (!url.contains("m3u8")) {
						urls.add(url);
					}
				}
			}
		}
		return urls;
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

	@Nullable
	public static InputStream getInputStream(@Nullable String imageURL) {
		return getInputStream(imageURL, e -> {
		});
	}

	@Nullable
	public static InputStream getInputStream(@Nullable String imageURL, Consumer<Exception> onException) {
		try {
			if (imageURL == null) {
				throw new IOException("imageURL == null");
			}

			return new URL(imageURL).openStream();
		} catch (Exception e) {
			onException.accept(e);
			return null;
		}
	}

	@Nullable
	public static Integer[] getImageWidthHeight(@Nullable String imageURL) {
		if (imageURL == null) {
			return null;
		}

		try {
			BufferedImage bi = ImageIO.read(new URL(imageURL));
			if (bi == null) {
				return null;
			}
			return new Integer[]{bi.getWidth(), bi.getHeight()};
		} catch (Exception e) {
			return null;
		}
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
