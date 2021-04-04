package com.hamusuke.twitter4mc.utils;

public class TwitterThread extends Thread {
	public TwitterThread(Runnable run) {
		super(run, "Twitter Thread");
	}
}
