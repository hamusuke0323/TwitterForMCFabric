package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.utils.TweetSummary;
import net.minecraft.client.util.NarratorManager;

//TODO
public class TwitterShowStatusScreen extends ParentalScreen {
	private final TweetSummary summary;

	public TwitterShowStatusScreen(TwitterScreen ts, TweetSummary summary) {
		super(NarratorManager.EMPTY, ts);
		this.summary = summary;
	}

	protected void init() {
		super.init();

	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
}
