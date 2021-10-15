package com.hamusuke.twitter4mc.gui.screen;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.settings.TwitterSettingsScreen;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TweetSummaryCreator;
import com.hamusuke.twitter4mc.utils.VersionChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class TwitterScreen extends AbstractTwitterScreen {
	@Nullable
	private Screen parent;
	private final AtomicBoolean refreshingTL = new AtomicBoolean();

	public TwitterScreen() {
		super(NarratorManager.EMPTY, null);

		if (TwitterForMC.twitterScreen != null) {
			throw new IllegalStateException("TwitterScreen object can be created only one");
		}
	}

	public void setParentScreen(@Nullable Screen parent) {
		this.parent = parent;
	}

	public void init() {
		this.getPreviousScreen().ifPresent(screen -> this.client.setScreen(screen));

		Optional<VersionChecker.UpdateInfo> updateInfo = VersionChecker.getUpdateInfo();
		int i = this.width / (updateInfo.isPresent() ? 3 : 2);
		MutableInt j = new MutableInt();
		int k = this.width / 4;

		updateInfo.ifPresent(info -> {
			this.addRenderLaterButton(new ButtonWidget(j.getValue(), this.height - 20, i, 20, new TranslatableText("tw.new.update.available"), (b) -> {
				this.client.setScreen(new ConfirmChatLinkScreen((bl) -> {
					if (bl) {
						Util.getOperatingSystem().open(info.url());
					}

					this.client.setScreen(this);
				}, info.url(), true));
			}));

			j.add(i);
		});

		//TODO for debug
		this.addDrawableChild(new ButtonWidget(0, this.height - 110, k - 10, 20, new TranslatableText("tweet"), (press) -> {
			this.client.setScreen(new TwitterTweetScreen(this));
		}));

		if (TwitterForMC.mcTwitter == null) {
			this.list = new TwitterScreen.TweetList(this.client);

			this.addRenderLaterButton(new ButtonWidget(j.getValue(), this.height - 20, i, 20, new TranslatableText("twitter.login"), (l) -> {
				this.client.setScreen(new TwitterLoginScreen(this));
			}));

			j.add(i);
		} else {
			/*
			this.addButton(new ButtonWidget(0, this.height - 110, k - 10, 20, I18n.translate("tweet"), (press) -> {
				this.minecraft.openScreen(new TwitterTweetScreen(this));
			}));
			*/

			this.addDrawableChild(new ButtonWidget(0, this.height - 50, k - 10, 20, new TranslatableText("tw.view.profile"), (press) -> {
				press.active = false;
				try {
					this.client.setScreen(new TwitterShowUserScreen(this, TwitterForMC.mcTwitter.showUser(TwitterForMC.mcTwitter.getId())));
				} catch (TwitterException e) {
					this.accept(Text.of(e.getErrorMessage()));
					press.active = true;
				}
			}));

			this.addRenderLaterButton(new ButtonWidget(j.getValue(), this.height - 20, i, 20, new TranslatableText("twitter.refresh"), (p) -> {
				p.active = false;
				this.refreshingTL.set(true);
				List<Status> t = Lists.newArrayList();
				try {
					t.addAll(TwitterForMC.mcTwitter.getHomeTimeline());
				} catch (TwitterException e) {
					this.accept(Text.of(e.getErrorMessage()));
				}
				Collections.reverse(t);
				new TweetSummaryCreator(t, (tweetSummary) -> {
					TwitterForMC.tweets.add(tweetSummary.getStatus());
					TwitterForMC.tweetSummaries.add(tweetSummary);
					this.remove(this.list);
					this.list = new TweetList(this.client);
					this.addSelectableChild(this.list);
				}, () -> {
					p.active = true;
					this.refreshingTL.set(false);
					this.init(this.client, this.width, this.height);
				}).createAll();
			})).active = !this.refreshingTL.get();

			j.add(i);

			this.addDrawableChild(new ButtonWidget(0, this.height - 80, k - 10, 20, new TranslatableText("tw.save.timeline"), (b) -> {
				b.active = false;
				try {
					TwitterForMC.saveTimeline();
				} catch (IOException e) {
					this.accept(Text.of(e.getLocalizedMessage()));
				}
				b.active = true;
			}));
		}

		this.addRenderLaterButton(new ButtonWidget(j.getValue(), this.height - 20, i, 20, ScreenTexts.BACK, (p_213034_1_) -> this.onClose()));

		this.addDrawableChild(new ButtonWidget(0, this.height - 140, k - 10, 20, new TranslatableText("tw.settings"), (b) -> {
			this.client.setScreen(new TwitterSettingsScreen(this));
		}));

		if (!this.refreshingTL.get()) {
			double scroll = this.list != null ? this.list.getScrollAmount() : 0.0D;
			this.list = new TwitterScreen.TweetList(this.client);
			this.list.setScrollAmount(scroll);
			this.addSelectableChild(this.list);
		}

		if (this.parent != null) {
			this.parent.resize(this.client, this.width, this.height);
		}
	}

	public boolean isInitialized() {
		return this.client != null;
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.parent != null) {
			this.parent.render(matrices, -1, -1, delta);
		}

		if (this.client.currentScreen == this) {
			this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
		} else {
			return;
		}

		super.render(matrices, mouseX, mouseY, delta);

		if (this.list != null) {
			this.list.render(matrices, mouseX, mouseY, delta);
		}

		this.renderButtonLater(matrices, mouseX, mouseY, delta);
		this.renderMessage(matrices);
	}

	public void onClose() {
		this.client.setScreen(this.parent);
	}

	@Environment(EnvType.CLIENT)
	private class TweetList extends AbstractTwitterScreen.TweetList {
		private TweetList(MinecraftClient mcIn) {
			super(mcIn, TwitterScreen.this.width, TwitterScreen.this.height, 0, TwitterScreen.this.height - 20);
			for (TweetSummary tweetSummary : TwitterForMC.tweetSummaries) {
				this.addEntry(new TweetEntry(tweetSummary));
			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}
		}
	}
}
