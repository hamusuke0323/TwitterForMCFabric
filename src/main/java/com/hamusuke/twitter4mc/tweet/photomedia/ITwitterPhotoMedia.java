package com.hamusuke.twitter4mc.tweet.photomedia;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import twitter4j.MediaEntity;

import java.io.InputStream;

@Environment(EnvType.CLIENT)
public interface ITwitterPhotoMedia {
	MediaEntity getMediaEntity();

	String getMediaURL();

	@Nullable
	InputStream getData();

	int getWidth();

	int getHeight();

	boolean canRendering();
}
