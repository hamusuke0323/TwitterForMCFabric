package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.tweet.TweetSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.NarratorManager;

//TODO
@Environment(EnvType.CLIENT)
public class TwitterShowStatusScreen extends AbstractTwitterScreen {
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
