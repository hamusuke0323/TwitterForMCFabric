package com.hamusuke.twitter4mc.tweet;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.utils.ImageDataDeliverer;
import com.hamusuke.twitter4mc.utils.ReplyObject;
import com.hamusuke.twitter4mc.utils.TwitterThread;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twitter4j.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class TweetSummary implements Comparable<TweetSummary> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Status status;
	@Nullable
	private final Status retweetedStatus;
	@Nullable
	private final TweetSummary retweetedSummary;
	@Nullable
	private final Status quotedTweet;
	@Nullable
	private final TweetSummary quotedTweetSummary;
	private final MutableInt replyCount = new MutableInt();
	private final List<Status> replyStatuses = Lists.newArrayList();
	private final List<TweetSummary> replyTweetSummaries = Lists.newArrayList();
	private final MutableBoolean isGettingReplies = new MutableBoolean();
	private final MutableBoolean isAlreadyGotReplies = new MutableBoolean();
	private final User user;
	private final ImageDataDeliverer userIconData;
	private final ImageFormat userIconFormat;
	private final Date createdAt;
	private final Calendar createdAtC;
	private final MutableInt favoriteCount = new MutableInt();
	private final MutableBoolean isFavorited = new MutableBoolean();
	private final List<HashtagEntity> hashtagList;
	private final long id;
	private final String lang;
	private final List<MediaEntity> mediaList;
	private final List<TwitterPhotoMedia> photoList;
	@Nullable
	private final String videoURL;
	private final boolean isIncludeImages;
	private final boolean isIncludeVideo;
	private final boolean isEmptyMedia;
	private final int photoMediaLength;
	private final Place place;
	private final MutableInt retweetCount = new MutableInt();
	private final MutableBoolean isRetweeted = new MutableBoolean();
	private String retweetCountF;
	private final String tweet;
	private final List<URLEntity> urlList;
	private final boolean isRetweet;
	private final boolean isRetweetedByMe;
	private String favoriteCountF;

	public TweetSummary(Status status) {
		this.status = status;
		this.retweetedStatus = status.getRetweetedStatus();
		this.retweetedSummary = this.retweetedStatus != null ? new TweetSummary(this.retweetedStatus) : null;
		this.quotedTweet = status.getQuotedStatus();
		this.quotedTweetSummary = this.quotedTweet != null ? new TweetSummary(this.quotedTweet) : null;
		this.user = status.getUser();
		String url = this.user.get400x400ProfileImageURLHttps();
		this.userIconData = new ImageDataDeliverer(url).prepareAsync(e -> LOGGER.warn("Failed to get user icon data, return null.", e), ignored -> {
		});
		this.userIconFormat = url.contains(".png") ? ImageFormat.PNG : ImageFormat.JPEG;
		this.createdAt = status.getCreatedAt();
		this.createdAtC = Calendar.getInstance(Locale.ROOT);
		this.createdAtC.setTime(this.createdAt);
		this.favoriteCount.setValue(status.getFavoriteCount());
		this.favoriteCountF = TwitterUtil.getChunkedNumber(this.favoriteCount.getValue());
		this.hashtagList = Lists.newArrayList(status.getHashtagEntities());
		this.id = status.getId();
		this.lang = status.getLang();
		this.mediaList = Lists.newArrayList(status.getMediaEntities());
		this.isEmptyMedia = this.mediaList.size() == 0;
		this.isIncludeImages = !this.isEmptyMedia && this.mediaList.get(0).getType().equals("photo");
		this.isIncludeVideo = !this.isEmptyMedia && this.mediaList.get(0).getType().equals("video");
		this.videoURL = this.isIncludeVideo ? TwitterUtil.getHiBitrateVideoURL(this.mediaList.get(0)) : null;
		this.photoMediaLength = this.isIncludeImages ? this.mediaList.size() : 0;
		this.photoList = this.mediaList.stream().filter(mediaEntity -> mediaEntity.getType().equals("photo")).map(TwitterPhotoMedia::new).toList();
		this.place = status.getPlace();
		this.retweetCount.setValue(status.getRetweetCount());
		this.retweetCountF = TwitterUtil.getChunkedNumber(this.retweetCount.getValue());
		this.tweet = status.getText();
		this.urlList = Lists.newArrayList(status.getURLEntities());
		this.isFavorited.setValue(status.isFavorited());
		this.isRetweet = status.isRetweet();
		this.isRetweeted.setValue(status.isRetweeted());
		this.isRetweetedByMe = status.isRetweetedByMe();
	}

	public void startGettingRepliesAsync(Runnable onAdd) {
		if (TwitterForMC.mcTwitter != null && !this.isGettingReplies()) {
			this.isGettingReplies.setTrue();
			new TwitterThread(() -> {
				try {
					ReplyObject replyObject = TwitterUtil.getReplies(TwitterForMC.mcTwitter, this.id);
					if (replyObject != null) {
						replyObject.removeOtherReplies(this.id);
						List<ReplyTweet> replyTweets = replyObject.getReplyTweets();
						this.replyCount.setValue(replyTweets.size());
						for (ReplyTweet replyTweet : replyTweets) {
							Status status = TwitterForMC.mcTwitter.showStatus(replyTweet.getTweetId());
							this.replyStatuses.add(status);
							this.replyTweetSummaries.add(new TweetSummary(status));
							onAdd.run();
						}
					}
					this.isGettingReplies.setFalse();
					this.isAlreadyGotReplies.setTrue();
				} catch (Throwable e) {
					LOGGER.error("Error occurred while getting replies", e);
					this.isGettingReplies.setFalse();
					this.isAlreadyGotReplies.setFalse();
				}

				if (this.replyCount.getValue() <= 0) {
					onAdd.run();
				}
			}).start();
		}
	}

	public boolean isGettingReplies() {
		return this.isGettingReplies.getValue();
	}

	public boolean isAlreadyGotReplies() {
		return this.isAlreadyGotReplies.getValue();
	}

	public List<Status> getReplyStatuses() {
		return this.replyStatuses;
	}

	public List<TweetSummary> getReplyTweetSummaries() {
		return this.replyTweetSummaries;
	}

	public Status getStatus() {
		return this.status;
	}

	@Nullable
	public Status getRetweetedStatus() {
		return this.retweetedStatus;
	}

	@Nullable
	public TweetSummary getRetweetedSummary() {
		return this.retweetedSummary;
	}

	@Nullable
	public Status getQuotedStatus() {
		return this.quotedTweet;
	}

	@Nullable
	public TweetSummary getQuotedTweetSummary() {
		return this.quotedTweetSummary;
	}

	public User getUser() {
		return this.user;
	}

	public String getScreenName() {
		return "@" + this.user.getScreenName();
	}

	public ImageDataDeliverer getUserIconData() {
		return this.userIconData;
	}

	public ImageFormat getUserIconFormat() {
		return this.userIconFormat;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public String getDifferenceTime() {
		return TwitterUtil.getDifferenceTime(this.createdAtC, Calendar.getInstance());
	}

	public String getTime() {
		return TwitterUtil.getTime(this.createdAtC);
	}

	public Calendar getCalendar() {
		return this.createdAtC;
	}

	public int getFavoriteCount() {
		return this.favoriteCount.getValue();
	}

	public String getFavoriteCountF() {
		return this.favoriteCountF;
	}

	public List<HashtagEntity> getHashtagList() {
		return this.hashtagList;
	}

	public long getId() {
		return this.id;
	}

	public String getTweetURL() {
		return "https://twitter.com/%s/status/%s".formatted(this.getScreenName(), this.getId());
	}

	public String getLang() {
		return this.lang;
	}

	public List<MediaEntity> getMediaList() {
		return this.mediaList;
	}

	public int getPhotoMediaLength() {
		return this.photoMediaLength;
	}

	public boolean isEmptyMedia() {
		return this.isEmptyMedia;
	}

	public boolean isIncludeImages() {
		return this.isIncludeImages;
	}

	public List<TwitterPhotoMedia> getPhotoMedias() {
		return this.photoList;
	}

	@Nullable
	public String getVideoURL() {
		return this.videoURL;
	}

	public boolean isIncludeVideo() {
		return this.isIncludeVideo;
	}

	public boolean isVideoURLNull() {
		return this.videoURL == null;
	}

	public Place getPlace() {
		return this.place;
	}

	public int getRetweetCount() {
		return this.retweetCount.getValue();
	}

	public String getRetweetCountF() {
		return this.retweetCountF;
	}

	public String getText() {
		return this.tweet;
	}

	public List<URLEntity> getURLList() {
		return this.urlList;
	}

	public boolean isFavorited() {
		return this.isFavorited.booleanValue();
	}

	public void favorite(boolean flag) {
		if (flag) {
			this.isFavorited.setTrue();
			this.favoriteCount.increment();
		} else {
			this.isFavorited.setFalse();
			this.favoriteCount.decrement();
		}

		this.updateFavoriteCountFormat();
	}

	public void retweet(boolean flag) {
		if (flag) {
			this.isRetweeted.setTrue();
			this.retweetCount.increment();
		} else {
			this.isRetweeted.setFalse();
			this.retweetCount.decrement();
		}

		this.updateRetweetCountFormat();
	}

	public void updateFavoriteCountFormat() {
		this.favoriteCountF = TwitterUtil.getChunkedNumber(this.favoriteCount.getValue());
	}

	public void updateRetweetCountFormat() {
		this.retweetCountF = TwitterUtil.getChunkedNumber(this.retweetCount.getValue());
	}

	public boolean isRetweet() {
		return this.isRetweet;
	}

	public boolean isRetweeted() {
		return this.isRetweeted.booleanValue();
	}

	public boolean isRetweetedByMe() {
		return this.isRetweetedByMe;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TweetSummary that = (TweetSummary) o;
		return this.status.equals(that.status);
	}

	public int hashCode() {
		return this.status.hashCode();
	}

	public int compareTo(@NotNull TweetSummary that) {
		return this.status.compareTo(that.status);
	}

	@Environment(EnvType.CLIENT)
	public enum ImageFormat {
		PNG,
		JPEG,
		GIF
	}
}
