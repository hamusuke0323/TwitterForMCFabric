package com.hamusuke.twitter4mc.photomedia;

import java.io.InputStream;

import com.hamusuke.twitter4mc.utils.TwitterUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import twitter4j.MediaEntity;

public class TwitterPhotoMedia implements ITwitterPhotoMedia {
	private final MediaEntity entity;
	private final String url;
	@Nullable
	private final InputStream data;
	private final int width;
	private final int height;
	private final boolean canRendering;
	private static final Logger LOGGER = LogManager.getLogger();

	public TwitterPhotoMedia(MediaEntity entity) {
		this.entity = entity;
		this.url = this.entity.getMediaURLHttps();
		this.data = TwitterUtil.getInputStream(this.url);
		if(this.data == null) {
			LOGGER.warn("Failed to load photo data. return null");
		}
		int[] wh = TwitterUtil.getImageWidthHeight(this.url);
		int w = 0, h = 0;
		if(wh != null) {
			w = wh[0];
			h = wh[1];
		}

		this.width = w;
		this.height = h;

		this.canRendering = this.width != 0 && this.height != 0;
	}

	public MediaEntity getMediaEntity() {
		return this.entity;
	}

	public String getMediaURL() {
		return this.url;
	}

	@Nullable
	public InputStream getData() {
		return this.data;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public boolean canRendering() {
		return this.canRendering;
	}
}
