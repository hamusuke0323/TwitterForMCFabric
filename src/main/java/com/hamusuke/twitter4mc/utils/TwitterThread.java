package com.hamusuke.twitter4mc.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TwitterThread extends Thread {
	public TwitterThread(Runnable run) {
		super(run, "Twitter Thread");
	}
}
