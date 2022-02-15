package com.hamusuke.twitter4mc.gui.screen.twitter;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.login.TwitterLoginScreen;
import com.hamusuke.twitter4mc.gui.screen.settings.TwitterSettingsScreen;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TweetSummaryProcessor;
import com.hamusuke.twitter4mc.utils.VersionChecker;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import twitter4j.Paging;
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
	@Nullable
	private IntegerFieldWidget count;

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

		if (TwitterForMC.mcTwitter == null) {
			this.list = new TwitterScreen.TweetList(this.client);

			this.addRenderLaterButton(new ButtonWidget(j.getValue(), this.height - 20, i, 20, new TranslatableText("twitter.login"), (l) -> {
				this.client.setScreen(new TwitterLoginScreen(this));
			}));

			j.add(i);
		} else {
			this.addDrawableChild(new ButtonWidget(0, this.height - 110, k - 10, 20, new TranslatableText("tweet"), (press) -> {
				this.client.setScreen(new TwitterTweetScreen(this));
			}));

			this.addDrawableChild(new ButtonWidget(0, this.height - 50, k - 10, 20, new TranslatableText("tw.view.profile"), (press) -> {
				press.active = false;
				try {
					this.displayTwitterUser(this, TwitterForMC.mcTwitter.showUser(TwitterForMC.mcTwitter.getId()));
				} catch (TwitterException e) {
					this.accept(Text.of(e.getErrorMessage()));
					press.active = true;
				}
			}));

			int l = i / 4;
			boolean bl = this.count == null;
			this.count = this.addSelectableChild(new IntegerFieldWidget(j.getValue() + i - l, this.height - 19, l, 18, this.count, new TranslatableText("tw.refresh.desc"), 1, 200));
			if (bl && this.count.getText().isEmpty()) {
				this.count.setText("20");
			}

			this.addRenderLaterButton(new ButtonWidget(j.getValue(), this.height - 20, i - l, 20, new TranslatableText("twitter.refresh"), (p) -> {
				p.active = false;
				this.refreshingTL.set(true);
				List<Status> t = Lists.newArrayList();
				try {
					t.addAll(TwitterForMC.mcTwitter.getHomeTimeline(new Paging().count(this.count == null ? 20 : this.count.getValue())));
				} catch (TwitterException e) {
					this.accept(Text.of(e.getErrorMessage()));
				} catch (CommandSyntaxException e) {
					this.accept(Text.of(e.getMessage()));
				}

				Collections.reverse(t);

				new TweetSummaryProcessor(t, tweetSummary -> {
					TwitterForMC.tweets.add(tweetSummary.getStatus());
					TwitterForMC.tweetSummaries.add(tweetSummary);
					this.remove(this.list);
					this.list = new TweetList(this.client);
					this.addSelectableChild(this.list);
				}, () -> {
					p.active = true;
					this.refreshingTL.set(false);
					this.init(this.client, this.width, this.height);
				}).process();
			})).active = !this.refreshingTL.get();

			j.add(i);

			this.addDrawableChild(new ButtonWidget(0, this.height - 80, k - 10, 20, new TranslatableText("tw.export.timeline"), (b) -> {
				b.active = false;
				try {
					TwitterForMC.exportTimeline();
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

		getMessageWidget().ifPresent(messageWidget -> messageWidget.init(this.width, this.height));
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
		}

		super.render(matrices, mouseX, mouseY, delta);

		if (this.list != null) {
			this.list.render(matrices, mouseX, mouseY, delta);
		}

		this.renderButtonLater(matrices, mouseX, mouseY, delta);

		if (this.count != null) {
			this.count.render(matrices, mouseX, mouseY, delta);
		}

		renderMessage(matrices, mouseX, mouseY, delta);
	}

	public void onClose() {
		this.client.setScreen(this.parent);
	}

	@Environment(EnvType.CLIENT)
	private class IntegerFieldWidget extends TextFieldWidget {
		private final IntegerArgumentType integerArgumentType;
		@Nullable
		private Text message;

		public IntegerFieldWidget(int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text, int min, int max) {
			super(TwitterScreen.this.textRenderer, x, y, width, height, copyFrom, text);
			this.integerArgumentType = IntegerArgumentType.integer(min, max);
			this.setTextPredicate(s -> {
				try {
					this.integerArgumentType.parse(new StringReader(s));
					this.message = null;
				} catch (CommandSyntaxException e) {
					this.message = Text.of(e.getMessage());
				}

				return true;
			});
		}

		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			super.renderButton(matrices, mouseX, mouseY, delta);
			Optional.ofNullable(this.message).ifPresent(s -> {
				List<OrderedText> orderedTexts = TwitterScreen.this.textRenderer.wrapLines(this.message, TwitterScreen.this.width);
				int max = AbstractTwitterScreen.getMaxWidth(TwitterScreen.this.textRenderer, orderedTexts);
				TwitterScreen.this.renderOrderedTooltip(matrices, orderedTexts, MathHelper.clamp(this.x + this.width / 2 - max / 2, 0, TwitterScreen.this.width - max), this.y - orderedTexts.size() * 9);
			});

			if (this.hovered) {
				this.renderTooltip(matrices, mouseX, mouseY);
			}
		}

		public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			List<OrderedText> orderedTexts = TwitterScreen.this.textRenderer.wrapLines(this.getMessage(), TwitterScreen.this.width / 2);
			TwitterScreen.this.renderOrderedTooltip(matrices, orderedTexts, mouseX, mouseY);
		}

		public int getValue() throws CommandSyntaxException {
			return this.integerArgumentType.parse(new StringReader(this.getText()));
		}
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
