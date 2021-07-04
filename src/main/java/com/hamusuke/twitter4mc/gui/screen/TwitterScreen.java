package com.hamusuke.twitter4mc.gui.screen;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.settings.TwitterSettingsScreen;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TweetSummaryCreator;
import com.hamusuke.twitter4mc.utils.TwitterThread;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.hamusuke.twitter4mc.utils.VersionChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Util;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;
import twitter4j.TwitterException;

@Environment(EnvType.CLIENT)
public class TwitterScreen extends AbstractTwitterScreen implements DisplayableMessage {
	@Nullable
	private Screen parent;
	private final AtomicBoolean refreshingTL = new AtomicBoolean();

	public TwitterScreen() {
		super(NarratorManager.EMPTY, null);

		if (TwitterForMC.twitterScreen != null) {
			throw new IllegalStateException("TwitterScreen can be created only one");
		}
	}

	public void setParentScreen(@Nullable Screen parent) {
		this.parent = parent;
	}

	public void tick() {
		if (this.list != null) {
			this.list.tick();
		}

		super.tick();
	}

	public void init() {
		boolean updateAvailable = VersionChecker.isUpdateAvailable();
		int i = this.width / (updateAvailable ? 3 : 2);
		int j = 0;
		int k = this.width / 4;

		if (updateAvailable) {
			this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("tw.new.update.available"), (b) -> {
				this.minecraft.openScreen(new ConfirmChatLinkScreen((bl) -> {
					if (bl) {
						Util.getOperatingSystem().open(VersionChecker.getUrl());
					}

					this.minecraft.openScreen(this);
				}, VersionChecker.getUrl(), true));
			}));

			j += i;
		}

		if (TwitterForMC.mctwitter == null) {
			this.list = new TwitterScreen.TweetList(this.minecraft);

			this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("twitter.login"), (l) -> {
				this.minecraft.openScreen(new TwitterLoginScreen(this));
			}));

			j += i;
		} else {
			this.addButton(new ButtonWidget(0, this.height - 110, k - 10, 20, I18n.translate("tweet"), (press) -> {
				this.minecraft.openScreen(new TwitterTweetScreen(this));
			}));

			this.addButton(new ButtonWidget(0, this.height - 50, k - 10, 20, I18n.translate("tw.view.profile"), (press) -> {
				press.active = false;
				try {
					this.minecraft.openScreen(new TwitterShowUserScreen(this, TwitterForMC.mctwitter.showUser(TwitterForMC.mctwitter.getId())));
				} catch (TwitterException e) {
					this.accept(e.getErrorMessage());
					press.active = true;
				}
			}));

			this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("twitter.refresh"), (p) -> {
				p.active = false;
				this.refreshingTL.set(true);
				List<Status> t = Lists.newArrayList();
				try {
					t.addAll(TwitterForMC.mctwitter.getHomeTimeline());
				} catch (TwitterException e) {
					this.accept(e.getErrorMessage());
				}
				Collections.reverse(t);
				new TweetSummaryCreator(t, (tweetSummary) -> {
					TwitterForMC.tweets.add(tweetSummary.getStatus());
					TwitterForMC.tweetSummaries.add(tweetSummary);
					this.children.remove(this.list);
					this.list = new TweetList(this.minecraft);
					this.children.add(this.list);
				}, () -> {
					p.active = true;
					this.refreshingTL.set(false);
				}).createAll();
			})).active = !this.refreshingTL.get();

			j += i;

			this.addButton(new ButtonWidget(0, this.height - 80, k - 10, 20, I18n.translate("tw.save.timeline"), (b) -> {
				b.active = false;
				try {
					TwitterForMC.saveTimeline();
				} catch (IOException e) {
					this.accept(e.getLocalizedMessage());
				}
				b.active = true;
			}));
		}

		this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("gui.back"), (p_213034_1_) -> this.onClose()));

		this.addButton(new ButtonWidget(0, this.height - 140, k - 10, 20, I18n.translate("tw.settings"), (b) -> {
			this.minecraft.openScreen(new TwitterSettingsScreen(this));
		}));

		if (!this.refreshingTL.get()) {
			double scroll = this.list != null ? this.list.getScrollAmount() : 0.0D;
			this.list = new TwitterScreen.TweetList(this.minecraft);
			this.list.setScrollAmount(scroll);
			this.children.add(this.list);
		}

		if (this.parent != null) {
			this.parent.resize(this.minecraft, this.width, this.height);
		}

		super.init();
	}

	public boolean isInitialized() {
		return this.minecraft != null;
	}

	public void render(int mouseX, int mouseY, float delta) {
		if (this.parent != null) {
			this.parent.render(-1, -1, delta);
		}

		if (this.minecraft.currentScreen == this) {
			this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		} else {
			return;
		}

		super.render(mouseX, mouseY, delta);

		if (this.list != null) {
			this.list.render(mouseX, mouseY, delta);
		}

		for (AbstractButtonWidget b : this.buttons) {
			if (!b.getMessage().equals(I18n.translate("tw.settings")) && !b.getMessage().equals(I18n.translate("tweet")) && !b.getMessage().equals(I18n.translate("tw.save.timeline")) && !b.getMessage().equals(I18n.translate("tw.view.profile")) && !b.getMessage().equals(I18n.translate("tw.new.update.available"))) {
				b.render(mouseX, mouseY, delta);
			}
		}
		this.renderMessage();
	}

	public void onClose() {
		this.minecraft.openScreen(this.parent);
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

			this.setY(0);
		}
	}
}
