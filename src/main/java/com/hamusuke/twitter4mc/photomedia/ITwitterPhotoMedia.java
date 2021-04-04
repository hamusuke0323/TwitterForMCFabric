package com.hamusuke.twitter4mc.photomedia;

import java.io.InputStream;

import org.jetbrains.annotations.Nullable;
import twitter4j.MediaEntity;

public interface ITwitterPhotoMedia {
	MediaEntity getMediaEntity();
	String getMediaURL();
	@Nullable
	InputStream getData();
	int getWidth();
	int getHeight();
	boolean canRendering();
}
