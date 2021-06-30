package com.hamusuke.twitter4mc.gui.screen;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.impl.IDisplayableMessage;
import com.hamusuke.twitter4mc.gui.screen.settings.TwitterSettingsScreen;
import com.hamusuke.twitter4mc.gui.widget.TwitterButton;
import com.hamusuke.twitter4mc.gui.widget.list.ExtendedTwitterTweetList;
import com.hamusuke.twitter4mc.tweet.photomedia.ITwitterPhotoMedia;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterThread;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.hamusuke.twitter4mc.utils.VersionChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.*;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

@Environment(EnvType.CLIENT)
public class TwitterScreen extends Screen implements IDisplayableMessage {
	private TwitterScreen.TweetList list;
	private ButtonWidget refreshTL;
	@Nullable
	private String message;
	private int fade;
	private boolean isFade;
	@Nullable
	private Screen parent;

	public TwitterScreen() {
		super(NarratorManager.EMPTY);

		if (TwitterForMC.twitterScreen != null) {
			throw new IllegalStateException("TwitterScreen can be created only one");
		}
	}

	public void setParentScreen(@Nullable Screen parent) {
		this.parent = parent;
	}

	public void tick() {
		this.list.tick();

		if (this.message != null) {
			if (this.fade <= 0) {
				this.message = null;
				this.fade = 0;
				this.isFade = false;
			} else if (this.fade <= 20) {
				this.isFade = true;
			}

			this.fade--;
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

			if (this.refreshTL != null) {
				this.refreshTL.x = j;
				this.refreshTL.y = this.height - 20;
				this.refreshTL.setWidth(i);
				this.refreshTL.setMessage(I18n.translate("twitter.refresh"));
				this.addButton(this.refreshTL);
			} else {
				this.refreshTL = this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("twitter.refresh"), (p) -> {
					p.active = false;
					new TwitterThread(() -> {
						List<Status> t = Lists.newArrayList();
						try {
							t.addAll(TwitterForMC.mctwitter.getHomeTimeline());
						} catch (TwitterException e) {
							this.accept(e.getErrorMessage());
						}
						Collections.reverse(t);
						for (Status s : t) {
							TwitterForMC.tweets.add(s);
							TwitterForMC.tweetSummaries.add(new TweetSummary(s));
							this.children.remove(this.list);
							this.list = new TwitterScreen.TweetList(this.minecraft);
							this.children.add(this.list);
						}
						p.active = true;
					}).start();
					this.minecraft.openScreen(this);
				}));
			}

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

		if (this.refreshTL != null && this.refreshTL.active) {
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

	public void render(int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		if (this.parent != null) {
			this.parent.render(-1, -1, p_230430_4_);
		}
		if (this.minecraft.currentScreen == this) {
			this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		} else {
			return;
		}
		super.render(p_230430_2_, p_230430_3_, p_230430_4_);
		this.list.render(p_230430_2_, p_230430_3_, p_230430_4_);

		for (AbstractButtonWidget b : this.buttons) {
			if (!b.getMessage().equals(I18n.translate("tw.settings")) && !b.getMessage().equals(I18n.translate("tweet")) && !b.getMessage().equals(I18n.translate("tw.save.timeline")) && !b.getMessage().equals(I18n.translate("tw.view.profile")) && !b.getMessage().equals(I18n.translate("tw.new.update.available"))) {
				b.render(p_230430_2_, p_230430_3_, p_230430_4_);
			}
		}
		if (this.message != null) {
			List<String> list = this.font.wrapStringToWidthAsList(this.message, this.width / 2);
			RenderSystem.pushMatrix();
			RenderSystem.enableAlphaTest();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.isFade ? (float) this.fade / 20 : 1.0F);
			this.renderTooltip(list, (this.width - this.font.getStringWidth(list.get(0))) / 2, this.height - list.size() * this.minecraft.textRenderer.fontHeight);
			RenderSystem.disableAlphaTest();
			RenderSystem.popMatrix();
		}
	}

	public void onClose() {
		this.minecraft.openScreen(this.parent);
	}

	public void accept(String errorMsg) {
		this.message = errorMsg;
		this.fade = 100;
	}

	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (this.list.hoveringEntry != null && this.list.hoveringEntry.mayClickIcon(p_mouseClicked_1_, p_mouseClicked_3_)) {
			this.minecraft.openScreen(new TwitterShowUserScreen(this, this.list.hoveringEntry.summary.getUser()));
			return true;
		} else if (!this.list.isHovering) {
			return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
		}

		return false;
	}

	public boolean renderTwitterUser(TweetSummary summary, int x, int y, int mouseX, int mouseY) {
		User user = summary.getUser();
		InputStream icon = summary.getUserIconData();
		List<String> desc = this.font.wrapStringToWidthAsList(user.getDescription(), Math.min(this.width / 2, 150));
		String f1 = new LiteralText(user.getFriendsCount() + "").formatted(Formatting.BOLD).asFormattedString();
		String follow = f1 + " " + new TranslatableText("tw.follow").formatted(Formatting.GRAY).asFormattedString();
		String f2 = new LiteralText(user.getFollowersCount() + "").formatted(Formatting.BOLD).asFormattedString();
		String follower = f2 + " " + new TranslatableText("tw.follower").formatted(Formatting.GRAY).asFormattedString();
		List<String> ff = this.font.wrapStringToWidthAsList(follow + "  " + follower, 150);

		RenderSystem.disableRescaleNormal();
		RenderSystem.disableDepthTest();
		int i = 0;

		for (String s : desc) {
			int j = this.font.getStringWidth(s);

			if (j > i) {
				i = j;
			}
		}

		for (String s1 : ff) {
			int j2 = this.font.getStringWidth(s1);
			if (j2 > i) {
				i = j2;
			}
		}

		int i2 = y;
		int k = 0;
		k += icon != null ? 22 : 0;
		k += user.getName().isEmpty() ? 0 : 10;
		k += user.getScreenName().isEmpty() ? 0 : 10;
		k += 4 + (desc.size() * (this.minecraft.textRenderer.fontHeight + 1)) + 4;
		k += ff.size() == 1 ? 10 : 20 + 2;

		if (i2 + k + 6 > this.height - 20) {
			i2 = this.height - 20 - k - 6;
		}

		this.fillGradient(x - 3, i2 - 4, x + i + 3, i2 - 3, -267386864, -267386864);
		this.fillGradient(x - 3, i2 + k + 3, x + i + 3, i2 + k + 4, -267386864, -267386864);
		this.fillGradient(x - 3, i2 - 3, x + i + 3, i2 + k + 3, -267386864, -267386864);
		this.fillGradient(x - 4, i2 - 3, x - 3, i2 + k + 3, -267386864, -267386864);
		this.fillGradient(x + i + 3, i2 - 3, x + i + 4, i2 + k + 3, -267386864, -267386864);
		this.fillGradient(x - 3, i2 - 3 + 1, x - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
		this.fillGradient(x + i + 2, i2 - 3 + 1, x + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
		this.fillGradient(x - 3, i2 - 3, x + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
		this.fillGradient(x - 3, i2 + k + 2, x + i + 3, i2 + k + 3, 1344798847, 1344798847);
		MatrixStack matrixstack = new MatrixStack();
		VertexConsumerProvider.Immediate vertexConsumerProvider$immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		Matrix4f matrix4f = matrixstack.peek().getModel();

		int yy = i2;
		if (icon != null) {
			TwitterForMC.getTextureManager().bindTexture(icon);
			blit(x, i2, 0.0F, 0.0F, 20, 20, 20, 20);
			i2 += 20;
		}

		int yyy = i2;
		boolean p = user.isProtected();
		boolean v = user.isVerified();
		int m = (p ? 10 : 0) + (v ? 10 : 0);
		String name = new LiteralText(user.getName()).formatted(Formatting.BOLD).asFormattedString();
		String three = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
		List<String> nameFormatted = this.font.wrapStringToWidthAsList(name, i - this.font.getStringWidth(three) - m);
		int n = this.font.draw(nameFormatted.size() == 1 ? nameFormatted.get(0) : nameFormatted.get(0) + three, (float) x, (float) i2 + 2, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
		this.font.draw(new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString(), (float) x, (float) i2 + 12, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);

		for (String s1 : desc) {
			if (s1 != null) {
				this.font.draw(s1, (float) x, (float) i2 + 26, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
			}

			i2 += 10;
		}

		if (ff.size() == 1) {
			this.font.draw(ff.get(0), (float) x, (float) i2 + 30, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
		} else {
			this.font.draw(follow, (float) x, (float) i2 + 30, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
			this.font.draw(follower, (float) x, (float) i2 + 30 + 10, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
		}

		vertexConsumerProvider$immediate.draw();

		if (p) {
			n += TwitterUtil.renderProtected(this.minecraft, n, yyy + 2);
		}
		if (v) {
			TwitterUtil.renderVerified(this.minecraft, n, yyy + 2);
		}

		RenderSystem.enableDepthTest();
		RenderSystem.enableRescaleNormal();

		return x - 4 < mouseX && x + i + 4 > mouseX && yy - 4 < mouseY && yy + k + 4 > mouseY;
	}

	public TwitterScreen.TweetList getList() {
		return this.list;
	}

	@Environment(EnvType.CLIENT)
	public class TweetList extends ExtendedTwitterTweetList<TweetList.TweetEntry> {
		@Nullable
		private TwitterScreen.TweetList.TweetEntry hoveringEntry;
		private boolean isHovering;
		private int fade;

		public TweetList(MinecraftClient mcIn) {
			super(mcIn, TwitterScreen.this.width, TwitterScreen.this.height, 0, TwitterScreen.this.height - 20);
			for (TweetSummary tweetSummary : TwitterForMC.tweetSummaries) {
				this.addEntry(new TweetEntry(tweetSummary));
			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}

			this.setY(0);
		}

		public void tick() {
			this.fade = this.isHovering ? 10 : this.fade - 1;
			this.children().forEach(TweetEntry::tick);
			super.tick();
		}

		protected int getScrollbarPosition() {
			return TwitterScreen.this.width - 5;
		}

		public int getRowWidth() {
			return TwitterScreen.this.width / 2;
		}

		protected void renderBackground() {
		}

		public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
			super.render(p_render_1_, p_render_2_, p_render_3_);

			TweetEntry e = this.getEntryAtPosition(p_render_1_, p_render_2_);
			if (this.hoveringEntry != null) {
				this.isHovering = TwitterScreen.this.renderTwitterUser(this.hoveringEntry.summary, this.getRowLeft() - 60, this.hoveringEntry.getY() + this.hoveringEntry.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
				if (!this.isHovering && this.fade < 0) {
					this.hoveringEntry = null;
					this.fade = 0;
				}
			} else if (e != null && e.mayClickIcon(p_render_1_, p_render_2_)) {
				this.hoveringEntry = e;
				this.isHovering = TwitterScreen.this.renderTwitterUser(e.summary, this.getRowLeft() - 60, e.getY() + e.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
				this.fade = 10;
			}
		}

		protected boolean isFocused() {
			return TwitterScreen.this.getFocused() == this;
		}

		@Environment(EnvType.CLIENT)
		public class TweetEntry extends ExtendedTwitterTweetList.AbstractTwitterListEntry<TweetEntry> {
			private final TweetSummary summary;
			@Nullable
			private final TweetSummary retweetedSummary;
			@Nullable
			private final TweetSummary quoteSourceSummary;
			private final List<String> strings;
			private final List<String> quotedTweetStrings;
			private final int retweetedUserNameHeight;
			private int height;
			private int y;

			private TwitterButton rep;
			private TwitterButton ret;
			private TwitterButton fav;
			private TwitterButton sha;
			private final int fourBtnHeightOffset;

			public TweetEntry(TweetSummary tweet) {
				boolean flag = tweet.getRetweetedSummary() != null;
				this.summary = flag ? tweet.getRetweetedSummary() : tweet;
				this.retweetedSummary = flag ? tweet : null;
				this.quoteSourceSummary = this.summary.getQuotedTweetSummary();
				this.strings = TwitterScreen.this.font.wrapStringToWidthAsList(this.summary.getText(), TweetList.this.getRowWidth() - 25);
				this.quotedTweetStrings = this.quoteSourceSummary != null ? TwitterScreen.this.font.wrapStringToWidthAsList(this.quoteSourceSummary.getText(), TweetList.this.getRowWidth() - 40) : Lists.newArrayList();
				this.height = ((this.strings.size() - 1) * TwitterScreen.this.minecraft.textRenderer.fontHeight) + 10 + 30;
				this.height += this.summary.isIncludeImages() || this.summary.isIncludeVideo() ? 120 : 0;
				this.retweetedUserNameHeight = flag ? TwitterUtil.wrapUserNameToWidth(TwitterScreen.this.minecraft, this.retweetedSummary, TweetList.this.getRowWidth() - 24).size() * TwitterScreen.this.minecraft.textRenderer.fontHeight : 0;
				this.height += this.retweetedUserNameHeight;
				this.height += this.quoteSourceSummary != null ? 20 + this.quotedTweetStrings.size() * TwitterScreen.this.minecraft.textRenderer.fontHeight : 0;
				this.fourBtnHeightOffset = this.height - 14;
			}

			public void tick() {
				this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
			}

			public void init() {
				int i = TwitterScreen.TweetList.this.getRowLeft() + 24;

				this.rep = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, TwitterUtil.REP, 16, 32, 16, 16, (p) -> {

				}));

				this.ret = this.addButton(new TwitterButton(i + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, TwitterUtil.RET, 16, 32, 16, 16, (p) -> {

				}));

				this.fav = this.addButton(new TwitterButton(i + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, this.summary.isFavorited() ? 0 : 16, this.summary.isFavorited() ? TwitterUtil.FAVED : TwitterUtil.FAV, 16, this.summary.isFavorited() ? 16 : 32, 16, 16, (b) -> {
					try {
						if (this.summary.isFavorited()) {
							TwitterForMC.mctwitter.destroyFavorite(this.summary.getId());
							this.summary.favorite(false);
							this.fav.setImage(TwitterUtil.FAV);
							this.fav.setWhenHovered(16);
							this.fav.setSize(16, 32);
						} else {
							TwitterForMC.mctwitter.createFavorite(this.summary.getId());
							this.summary.favorite(true);
							this.fav.setImage(TwitterUtil.FAVED);
							this.fav.setWhenHovered(0);
							this.fav.setSize(16, 16);
						}
					} catch (TwitterException e) {
						TwitterScreen.this.accept(I18n.translate("tw.failed.like", e.getErrorMessage()));
					}
				}));

				this.sha = this.addButton(new TwitterButton(i + 60 + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, TwitterUtil.SHA, 16, 32, 16, 16, (p) -> {

				}));
			}

			public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {
				InputStream icon = this.summary.getUserIconData();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableBlend();

				int nowY = rowTop;
				nowY = TwitterUtil.renderRetweetedUser(TwitterScreen.this.minecraft, this.retweetedSummary, rowLeft + 6, rowLeft + 24, nowY, rowWidth - 24);

				if (icon != null) {
					TwitterForMC.getTextureManager().bindTexture(icon);
					DrawableHelper.blit(rowLeft, nowY, 0.0F, 0.0F, 16, 16, 16, 16);
				}

				RenderSystem.disableBlend();

				TwitterUtil.renderUserName(TwitterScreen.this.minecraft, this.summary, rowLeft + 24, nowY, rowWidth - 24);

				for (int i = 0; i < this.strings.size(); i++) {
					TwitterScreen.this.font.drawWithShadow(this.strings.get(i), (float) (rowLeft + 24), (float) (nowY + 10 + i * TwitterScreen.this.minecraft.textRenderer.fontHeight), 16777215);
				}
				nowY += 10 + this.strings.size() * TwitterScreen.this.minecraft.textRenderer.fontHeight;

				if (this.summary.isIncludeVideo()) {
					TwitterScreen.this.fillGradient(rowLeft + 24, nowY, rowLeft + 24 + 208, nowY + 117, -1072689136, -804253680);
					if (mouseX >= rowLeft + 24 && mouseX <= rowLeft + 24 + 208 && mouseY >= nowY && mouseY <= nowY + 117) {
						TwitterScreen.this.renderTooltip(I18n.translate("tw.play.video"), mouseX, mouseY);
					}
					nowY += 117;
				}

				nowY += this.renderPhotos(rowLeft, nowY);

				if (this.quoteSourceSummary != null) {
					nowY += 10;
					InputStream qsIco = this.quoteSourceSummary.getUserIconData();
					if (qsIco != null) {
						TwitterForMC.getTextureManager().bindTexture(qsIco);
						blit(rowLeft + 24 + 5, nowY, 0.0F, 0.0F, 10, 10, 10, 10);
					}
					TwitterUtil.renderUserName(TwitterScreen.this.minecraft, this.quoteSourceSummary, rowLeft + 24 + 5 + 10 + 4, nowY, TweetList.this.getRowWidth() - 24 - 5 - 10 - 4 - 10);
					for (int i = 0; i < this.quotedTweetStrings.size(); i++) {
						TwitterScreen.this.font.drawWithShadow(this.quotedTweetStrings.get(i), rowLeft + 24 + 5, nowY + 10 + i * TwitterScreen.this.minecraft.textRenderer.fontHeight, Formatting.WHITE.getColorValue());
					}
					nowY += 10 + this.quotedTweetStrings.size() * TwitterScreen.this.minecraft.textRenderer.fontHeight;
				}

				super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);

				if (this.summary.getRetweetCount() != 0) {
					TwitterScreen.this.font.drawWithShadow("" + this.summary.getRetweetCountF(), this.ret.x + 16.0F, this.ret.y, Formatting.GRAY.getColorValue());
				}
				if (this.summary.getFavoriteCount() != 0) {
					TwitterScreen.this.font.drawWithShadow("" + this.summary.getFavoriteCountF(), this.fav.x + 16.0F, this.fav.y, Formatting.GRAY.getColorValue());
				}
			}

			public int renderPhotos(int rowLeft, int rowTop) {
				List<ITwitterPhotoMedia> p = this.summary.getPhotoMedias();
				if (p.size() == 1) {
					ITwitterPhotoMedia media = p.get(0);
					InputStream data = media.getData();
					if (data != null) {
						Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(208, 117));
						TwitterForMC.getTextureManager().bindTexture(data);
						DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, 0.0F, 208, 117, d.width, d.height);
					}
				} else if (p.size() == 2) {
					for (int i = 0; i < 2; i++) {
						ITwitterPhotoMedia media = p.get(i);
						InputStream data = media.getData();
						if (data != null) {
							Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 117));
							TwitterForMC.getTextureManager().bindTexture(data);
							DrawableHelper.blit(rowLeft + 24 + i * 105, rowTop, 0.0F, 0.0F, 104, 117, d.width, d.height);
						}
					}
				} else if (p.size() == 3) {
					for (int i = 0; i < 3; i++) {
						ITwitterPhotoMedia media = p.get(i);
						InputStream data = media.getData();
						if (data != null) {
							Dimension d;
							TwitterForMC.getTextureManager().bindTexture(data);
							if (i == 0) {
								d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 117));
								DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, 0.0F, 104, 117, d.width, d.height);
							} else {
								d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 58));
								DrawableHelper.blit(rowLeft + 24 + 105, rowTop + ((i - 1) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
							}
						}
					}
				} else if (p.size() == 4) {
					for (int i = 0; i < 4; i++) {
						ITwitterPhotoMedia media = p.get(i);
						InputStream data = media.getData();
						if (data != null) {
							Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 58));
							TwitterForMC.getTextureManager().bindTexture(data);
							if (i % 2 == 0) {
								DrawableHelper.blit(rowLeft + 24, rowTop + ((i / 2) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
							} else {
								DrawableHelper.blit(rowLeft + 24 + 105, rowTop + ((i / 3) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
							}
						}
					}
				}

				return p.size() == 0 ? 0 : 117;
			}

			public boolean mouseClicked(double x, double y, int button) {
				int i = TweetList.this.getRowLeft() + 24;
				int j = this.y + this.retweetedUserNameHeight + 11 + this.strings.size() * TwitterScreen.this.minecraft.textRenderer.fontHeight;
				int k = this.summary.getPhotoMediaLength();
				boolean xMore = x >= i;
				boolean yMore = y >= j;
				boolean b = xMore && x <= i + 208 && yMore && y <= j + 117;
				boolean b1 = xMore && x <= i + 104 && yMore && y <= j + 117;
				boolean b2 = x >= i + 105 && x <= i + 208 && yMore && y <= j + 58;

				if (k == 1) {
					if (b) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
					}
				} else if (k == 2) {
					if (b1) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
					} else if (x >= i + 105 && x <= i + 208 && yMore && y <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
					}
				} else if (k == 3) {
					if (b1) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
					} else if (b2) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
					} else if (xMore && x <= i + 208 && y >= j + 59 && y <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 2);
					}
				} else if (k == 4) {
					if (xMore && x <= i + 104 && yMore && y <= j + 58) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
					} else if (b2) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
					} else if (xMore && x <= i + 104 && y >= j + 59 && y <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 2);
					} else if (x >= i + 105 && x <= i + 208 && y >= j + 59 && y <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(button, 3);
					}
				}

				if (this.summary.isIncludeVideo()) {
					if (b) {
						return this.videoClicked(button);
					}
				}

				for (AbstractButtonWidget w : this.buttons) {
					if (w.mouseClicked(x, y, button)) {
						return true;
					}
				}

				if (button == 0) {
					TwitterScreen.TweetList.this.setSelected(this);
					//TODO show showStatusScreen action
					return true;
				} else {
					return false;
				}
			}

			private boolean mayClickIcon(double x, double y) {
				int i = TwitterScreen.TweetList.this.getRowLeft();
				int j = this.y + this.retweetedUserNameHeight;
				return x > i && x < i + 16 && y > j && y < j + 16;
			}

			private boolean displayTwitterPhotoAndShowStatusScreen(int mouseButton, int index) {
				TwitterScreen.this.minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				if (mouseButton == 0) {
					TwitterScreen.this.minecraft.openScreen(new TwitterPhotoAndShowStatusScreen(TwitterScreen.this, this.summary, index));
				} else if (mouseButton == 1) {
					//TODO save picture action;
				}

				return true;
			}

			private boolean videoClicked(int mouseButton) {
				if (!this.summary.isVideoNull()) {
					if (mouseButton == 0) {
						this.summary.getPlayer().play(TwitterScreen.this.minecraft.getWindow().getX(), TwitterScreen.this.minecraft.getWindow().getY(), TwitterScreen.this.minecraft.getWindow().getWidth() / 2, TwitterScreen.this.minecraft.getWindow().getHeight() / 2);
					} else if (mouseButton == 1) {
						//TODO save video action;
					}
				}

				return false;
			}

			public int getHeight() {
				return this.height;
			}

			public int getY() {
				return this.y;
			}

			public void setY(int y) {
				this.y = y;
				this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
			}

			public boolean equals(Object obj) {
				return this.summary.equals(obj);
			}

			public int hashCode() {
				return this.summary.hashCode();
			}
		}
	}
}
