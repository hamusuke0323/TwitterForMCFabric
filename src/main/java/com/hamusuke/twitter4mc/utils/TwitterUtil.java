package com.hamusuke.twitter4mc.utils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Environment(EnvType.CLIENT)
public class TwitterUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	public static String chunkedNumber(int number) {
		return chunkedNumber("" + number);
	}

	//123456 -> 123.4K
	public static String chunkedNumber(String number) {
		try {
			Integer.parseInt(number);
		} catch (NumberFormatException e) {
			LOGGER.warn("Input: {} is not integer", number);
			return number;
		}
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
	}

	public static String getDTime(Calendar created, Calendar now) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(now.getTimeInMillis() - created.getTimeInMillis()));
		int y = c.get(Calendar.YEAR);
		int mon = c.get(Calendar.MONTH) + 1;
		int d = c.get(Calendar.DAY_OF_MONTH);
		int h = c.get(Calendar.HOUR_OF_DAY) - 9;
		int m = c.get(Calendar.MINUTE);
		if (y == 1970 && mon == 1 && d == 1 && h == 0 && m == 0) {
			return I18n.translate("tw.seconds", c.get(Calendar.SECOND));
		} else if (y == 1970 && mon == 1 && d == 1 && h == 0) {
			return I18n.translate("tw.minutes", m);
		} else if (y == 1970 && mon == 1 && d == 1) {
			return I18n.translate("tw.hours", h);
		} else if (y == 1970) {
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

	public static List<Status> getNonDuplicateStatuses(List<Status> old, List<Status> homeTimeline) {
		List<Status> t = Lists.newArrayList(homeTimeline);
		for (Status o : old) {
			for (Status n : homeTimeline) {
				if (n.getId() == o.getId()) {
					t.remove(n);
				}
			}
		}
		return t;
	}

	@Nullable
	public static InputStream getInputStream(@Nullable String imageURL) {
		try {
			if (imageURL == null) {
				return null;
			}
			return new URL(imageURL).openStream();
		} catch (Exception e) {
			return null;
		}
	}

	@Nullable
	public static Integer[] getImageWidthHeight(@Nullable String imageURL) {
		try {
			if (imageURL == null) {
				return null;
			}
			BufferedImage bi = ImageIO.read(TwitterUtil.getInputStream(imageURL));
			return new Integer[]{bi.getWidth(), bi.getHeight()};
		} catch (Exception e) {
			return null;
		}
	}

	public static Dimension getScaledDimensionMaxRatio(Dimension imageSize, Dimension boundary) {
		double widthRatio = boundary.getWidth() / imageSize.getWidth();
		double heightRatio = boundary.getHeight() / imageSize.getHeight();
		double ratio = Math.max(widthRatio, heightRatio);

		return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
	}

	public static Dimension getScaledDimensionMinRatio(Dimension imageSize, Dimension boundary) {
		double widthRatio = boundary.getWidth() / imageSize.getWidth();
		double heightRatio = boundary.getHeight() / imageSize.getHeight();
		double ratio = Math.min(widthRatio, heightRatio);

		return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
	}

	@Environment(EnvType.CLIENT)
	public enum Unit {
		KILO('K', 1),
		MEGA('M', 2),
		GIGA('G', 3);

		public final char name;
		public final int split;

		Unit(char name, int split) {
			this.name = name;
			this.split = split;
		}

		public static char get(int split) {
			for (TwitterUtil.Unit u : TwitterUtil.Unit.values()) {
				if (u.split == split) {
					return u.name;
				}
			}

			return Character.MIN_VALUE;
		}
	}
}
