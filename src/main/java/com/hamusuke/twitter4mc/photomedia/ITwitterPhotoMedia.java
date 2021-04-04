package com.hamusuke.twitter4mc.photomedia;

import java.io.InputStream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import twitter4j.MediaEntity;

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
