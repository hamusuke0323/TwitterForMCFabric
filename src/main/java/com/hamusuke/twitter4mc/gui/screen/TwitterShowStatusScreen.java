package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.tweet.TweetSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

//TODO
@Environment(EnvType.CLIENT)
public class TwitterShowStatusScreen extends AbstractTwitterScreen {
	private final TweetSummary summary;

	public TwitterShowStatusScreen(Screen parent, TweetSummary summary) {
		super(new TranslatableText("tweeting"), parent);
		this.summary = summary;
	}

	protected void init() {
		super.init();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
	}

	@Environment(EnvType.CLIENT)
	private class TweetList extends AbstractTwitterScreen.TweetList {
		private TweetList(MinecraftClient mcIn, int width, int height, int top, int bottom) {
			super(mcIn, TwitterShowStatusScreen.this.width, TwitterShowStatusScreen.this.height, 20, TwitterShowStatusScreen.this.height - 20);
		}

		@Environment(EnvType.CLIENT)
		private class MainEntry extends TweetEntry {
			private MainEntry(@Nullable TweetSummary tweet) {
				super(tweet);
			}
		}

		@Environment(EnvType.CLIENT)
		private class ReplyEntry extends TweetEntry {
			private ReplyEntry(@Nullable TweetSummary tweet) {
				super(tweet);
			}
		}
	}
}
