package com.hamusuke.twitter4mc.utils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;
import twitter4j.*;

@Environment(EnvType.CLIENT)
public final class TwitterUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final Identifier PROTECTED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/protected.png");
	public static final Identifier VERIFIED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/verified.png");
	public static final Identifier REP = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/reply.png");
	public static final Identifier RET = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/retweet.png");
	public static final Identifier RETED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/retweeted.png");
	public static final Identifier RETUSR = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/retweetuser.png");
	public static final Identifier FAV = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/favorite.png");
	public static final Identifier FAVED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/favorited.png");
	public static final Identifier SHA = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/share.png");

	private TwitterUtil() {
		throw new IllegalStateException();
	}

	public static JSONObject getReplyCount(Twitter twitter, long tweetId) throws TwitterException {
		HttpResponse httpResponse = HttpClientFactory.getInstance().get("https://api.twitter.com/2/tweets", new HttpParameter[]{new HttpParameter("ids", tweetId), new HttpParameter("tweet.fields", "public_metrics,author_id,conversation_id,created_at,in_reply_to_user_id,referenced_tweets"), new HttpParameter("expansions", "author_id,in_reply_to_user_id,referenced_tweets.id"), new HttpParameter("user.fields", "name,username")}, twitter.getAuthorization(), null);

		if(httpResponse != null) {
			return httpResponse.asJSONObject();
		}

		return new JSONObject();
	}

	public static JSONObject getConversation(Twitter twitter, long conversationId) throws TwitterException {
		HttpResponse httpResponse = HttpClientFactory.getInstance().get("https://api.twitter.com/2/tweets/search/recent", new HttpParameter[]{new HttpParameter("query", "conversation_id:" + conversationId), new HttpParameter("tweet.fields", "public_metrics,author_id,conversation_id,created_at,in_reply_to_user_id,referenced_tweets"), new HttpParameter("user.fields", "name,username")}, twitter.getAuthorization(), null);

		if(httpResponse != null) {
			return httpResponse.asJSONObject();
		}

		return new JSONObject();
	}

	public static int renderRetweetedUser(MinecraftClient minecraft, @Nullable TweetSummary retweetedSummary, int iconX, int x, int y, int width) {
		if (retweetedSummary != null) {
			minecraft.getTextureManager().bindTexture(TwitterUtil.RETUSR);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(iconX, y, 0.0F);
			RenderSystem.scalef(0.625F, 0.625F, 0.625F);
			DrawableHelper.blit(0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
			RenderSystem.popMatrix();
			List<String> names = wrapUserNameToWidth(minecraft, retweetedSummary, width);
			for (int i = 0; i < names.size(); i++) {
				minecraft.textRenderer.drawWithShadow(names.get(i), x, y + i * minecraft.textRenderer.fontHeight, 11184810);
			}
			return y + names.size() * minecraft.textRenderer.fontHeight;
		}

		return y;
	}

	public static List<String> wrapUserNameToWidth(MinecraftClient minecraft, TweetSummary summary, int width) {
		return minecraft.textRenderer.wrapStringToWidthAsList(I18n.translate("tw.retweeted.user", summary.getUser().getName()), width);
	}

	public static void renderUserName(MinecraftClient minecraft, TweetSummary summary, int x, int y, int width) {
		boolean p = summary.getUser().isProtected();
		boolean v = summary.getUser().isVerified();

		String threeBold = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
		int threeBoldWidth = minecraft.textRenderer.getStringWidth(threeBold);
		String three = new LiteralText("...").formatted(Formatting.GRAY).asFormattedString();
		int threeWidth = minecraft.textRenderer.getStringWidth(three);
		String time = new LiteralText("・" + summary.getDifferenceTime()).formatted(Formatting.GRAY).asFormattedString();
		int timeWidth = minecraft.textRenderer.getStringWidth(time);
		String screenName = new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString();
		String name = new LiteralText(summary.getUser().getName()).formatted(Formatting.BOLD).asFormattedString();

		int pvw = (p ? 10 : 0) + (v ? 10 : 0);
		List<String> nameFormatted = minecraft.textRenderer.wrapStringToWidthAsList(name, width - pvw - timeWidth);
		boolean isOver = nameFormatted.size() > 1;
		List<String> nameFormatted2 = isOver ? minecraft.textRenderer.wrapStringToWidthAsList(name, width - pvw - timeWidth - threeBoldWidth) : nameFormatted;

		String formattedName = nameFormatted2.size() == 1 ? nameFormatted2.get(0) : nameFormatted2.get(0) + threeBold;
		int formattedNameWidth = minecraft.textRenderer.getStringWidth(formattedName);
		minecraft.textRenderer.drawWithShadow(formattedName, x, y, 16777215);
		x += formattedNameWidth;
		if (p) {
			x += renderProtected(minecraft, x, y);
		}
		if (v) {
			x += renderVerified(minecraft, x, y);
		}

		List<String> screenNameFormatted = minecraft.textRenderer.wrapStringToWidthAsList(screenName, width - formattedNameWidth - pvw - timeWidth - threeWidth);
		if (!isOver) {
			String s = screenNameFormatted.size() == 1 ? screenNameFormatted.get(0) : screenNameFormatted.get(0) + three;
			minecraft.textRenderer.drawWithShadow(s, x, y, 11184810);
			x += minecraft.textRenderer.getStringWidth(s);
		}
		minecraft.textRenderer.drawWithShadow(time, x, y, 11184810);
	}

	public static int renderProtected(MinecraftClient minecraft, int x, int y) {
		minecraft.getTextureManager().bindTexture(PROTECTED);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(x, y, 0.0F);
		RenderSystem.scalef(0.625F, 0.625F, 0.625F);
		DrawableHelper.blit(0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
		RenderSystem.popMatrix();
		return 10;
	}

	public static int renderVerified(MinecraftClient minecraft, int x, int y) {
		minecraft.getTextureManager().bindTexture(VERIFIED);
		RenderSystem.pushMatrix();
		RenderSystem.translatef(x, y, 0.0F);
		RenderSystem.scalef(0.625F, 0.625F, 0.625F);
		DrawableHelper.blit(0, 0, 0, 0, 16, 16, 16, 16);
		RenderSystem.popMatrix();
		return 10;
	}

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

	@Deprecated
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
			BufferedImage bi = ImageIO.read(new URL(imageURL));
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
