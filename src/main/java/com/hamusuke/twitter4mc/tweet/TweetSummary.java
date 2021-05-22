package com.hamusuke.twitter4mc.tweet;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.hamusuke.twitter4mc.tweet.photomedia.ITwitterPhotoMedia;
import com.hamusuke.twitter4mc.tweet.photomedia.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.hamusuke.twitter4mc.tweet.videomedia.TwitterVideoPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

@Environment(EnvType.CLIENT)
public class TweetSummary {
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
	private final User user;
	@Nullable
	private final InputStream userIconData;
	private final String userIconFormat;
	private final Date createdAt;
	private final Calendar createdAtC;
	private final int favoriteCount;
	private final String favoriteCountF;
	private final HashtagEntity[] hashtags;
	private final List<HashtagEntity> hashtagList;
	private final long ID;
	private final String lang;
	private final MediaEntity[] medias;
	private final List<MediaEntity> mediaList;
	private final List<ITwitterPhotoMedia> photoList = Lists.newArrayList();
	@Nullable
	private final String videoURL;
	@Nullable
	private final TwitterVideoPlayer player;
	private final boolean isIncludeImages;
	private final boolean isIncludeVideo;
	private final boolean isEmptyMedia;
	private final int photoMediaLength;
	private final Place place;
	private final int retweetCount;
	private final String retweetCountF;
	private final String tweet;
	//private final String formattedTweet;
	private final URLEntity[] urls;
	private final List<URLEntity> urlList;
	private boolean isFavorited;
	private final boolean isRetweet;
	private final boolean isRetweeted;
	private final boolean isRetweetedByMe;

	public TweetSummary(Status status) {
		this.status = status;
		this.retweetedStatus = this.status.getRetweetedStatus();
		this.retweetedSummary = this.retweetedStatus != null ? new TweetSummary(this.retweetedStatus) : null;
		this.quotedTweet = this.status.getQuotedStatus();
		this.quotedTweetSummary = this.quotedTweet != null ? new TweetSummary(this.quotedTweet) : null;
		this.user = status.getUser();
		String url = this.user.get400x400ProfileImageURLHttps();
		this.userIconData = TwitterUtil.getInputStream(url);
		this.userIconFormat = url.contains(".png") ? "PNG" : "JPEG";
		if (this.userIconData == null) {
			LOGGER.warn("Failed load user icon. return null");
		}
		this.createdAt = status.getCreatedAt();
		this.createdAtC = Calendar.getInstance(Locale.ROOT);
		this.createdAtC.setTime(this.createdAt);
		this.favoriteCount = status.getFavoriteCount();
		this.favoriteCountF = TwitterUtil.chunkedNumber(this.favoriteCount);
		this.hashtags = status.getHashtagEntities();
		this.hashtagList = Arrays.asList(this.hashtags);
		this.ID = status.getId();
		this.lang = status.getLang();
		this.medias = status.getMediaEntities();
		this.mediaList = Arrays.asList(this.medias);
		this.isEmptyMedia = this.medias.length == 0;
		this.isIncludeImages = !this.isEmptyMedia && this.medias[0].getType().equals("photo");
		this.isIncludeVideo = !this.isEmptyMedia && this.medias[0].getType().equals("video");
		this.videoURL = this.isIncludeVideo ? TwitterUtil.getHiBitrateVideoURL(this.medias[0]) : null;
		this.player = this.videoURL == null ? null : new TwitterVideoPlayer(this.videoURL);
		this.photoMediaLength = this.isIncludeImages ? this.medias.length : 0;

		for (int i = 0; i < this.photoMediaLength; i++) {
			this.photoList.add(new TwitterPhotoMedia(this.medias[i]));
		}

		this.place = status.getPlace();
		this.retweetCount = status.getRetweetCount();
		this.retweetCountF = TwitterUtil.chunkedNumber(this.retweetCount);
		this.tweet = status.getText();
		this.urls = status.getURLEntities();
		this.urlList = Arrays.asList(this.urls);
		this.isFavorited = status.isFavorited();
		this.isRetweet = status.isRetweet();
		this.isRetweeted = status.isRetweeted();
		this.isRetweetedByMe = status.isRetweetedByMe();
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

	@Nullable
	public InputStream getUserIconData() {
		return this.userIconData;
	}

	public String getUserIconFormat() {
		return this.userIconFormat;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public String getDifferenceTime() {
		return TwitterUtil.getDTime(this.createdAtC, Calendar.getInstance());
	}

	public String getTime() {
		return TwitterUtil.getTime(this.createdAtC);
	}

	public Calendar getCalendar() {
		return this.createdAtC;
	}

	public int getFavoriteCount() {
		return this.favoriteCount;
	}

	public String getFavoriteCountF() {
		return this.favoriteCountF;
	}

	public HashtagEntity[] getHashtagEntities() {
		return this.hashtags;
	}

	public List<HashtagEntity> getHashtagList() {
		return this.hashtagList;
	}

	public long getId() {
		return this.ID;
	}

	public String getLang() {
		return this.lang;
	}

	public MediaEntity[] getMediaEntities() {
		return this.medias;
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

	public List<ITwitterPhotoMedia> getPhotoMedias() {
		return this.photoList;
	}

	@Nullable
	public String getVideoURL() {
		return this.videoURL;
	}

	@Nullable
	public TwitterVideoPlayer getPlayer() {
		return this.player;
	}

	public boolean isIncludeVideo() {
		return this.isIncludeVideo;
	}

	public boolean isVideoNull() {
		return this.videoURL == null || this.player == null;
	}

	public Place getPlace() {
		return this.place;
	}

	public int getRetweetCount() {
		return this.retweetCount;
	}

	public String getRetweetCountF() {
		return this.retweetCountF;
	}

	public String getText() {
		return this.tweet;
	}

	public URLEntity[] getURLEntities() {
		return this.urls;
	}

	public List<URLEntity> getURLList() {
		return this.urlList;
	}

	public boolean isFavorited() {
		return this.isFavorited;
	}

	public void favorite(boolean flag) {
		this.isFavorited = flag;
	}

	public boolean isRetweet() {
		return this.isRetweet;
	}

	public boolean isRetweeted() {
		return this.isRetweeted;
	}

	public boolean isRetweetedByMe() {
		return this.isRetweetedByMe;
	}

	public boolean equals(Object var1) {
		if (var1 instanceof TweetSummary) {
			return this.getId() == ((TweetSummary) var1).getId();
		} else {
			return false;
		}
	}
}
