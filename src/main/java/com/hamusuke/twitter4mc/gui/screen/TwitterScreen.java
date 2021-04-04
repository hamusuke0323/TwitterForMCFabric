package com.hamusuke.twitter4mc.gui.screen;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.hamusuke.twitter4mc.TwitterForMinecraft;
import com.hamusuke.twitter4mc.gui.widget.TwitterButton;
import com.hamusuke.twitter4mc.gui.widget.list.ExtendedTwitterTweetList;
import com.hamusuke.twitter4mc.photomedia.ITwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterThread;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
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
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

@Environment(EnvType.CLIENT)
public class TwitterScreen extends Screen {
	private static final Identifier PROTECTED = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/protected.png");
	private static final Identifier VERIFIED = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/verified.png");
	private static final Identifier REP = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/reply.png");
	private static final Identifier RET = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/retweet.png");
	private static final Identifier RETED = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/retweeted.png");
	private static final Identifier RETUSR = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/retweetuser.png");
	private static final Identifier FAV = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/favorite.png");
	private static final Identifier FAVED = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/favorited.png");
	private static final Identifier SHA = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/share.png");
	private TwitterScreen.TweetList list;
	private static final Logger LOGGER = LogManager.getLogger();
	private ButtonWidget refreshTL;
	@Nullable
	private String message;
	private int fade;
	private boolean isfade;
	@Nullable
	private Screen parent;

	@Nullable
	private TwitterScreen.TweetList.TweetEntry hoveredEntry;
	private boolean iconHovered;
	private int suFade;
	private boolean isRendering;
	private boolean isHovering;

	public TwitterScreen() {
		super(NarratorManager.EMPTY);
	}

	public void setParentScreen(@Nullable Screen parent) {
		this.parent = parent;
	}

	public void tick() {
		for (TweetList.TweetEntry te : this.list.children()) {
			te.tick();
		}
		if (!this.isRendering && this.iconHovered) {
			this.suFade++;
			if (this.suFade >= 20) {
				this.isRendering = true;
				this.suFade = 10;
			}
		} else if (this.isRendering && !this.iconHovered && !this.isHovering) {
			this.suFade--;
			if (this.suFade <= 0) {
				this.isRendering = false;
				this.hoveredEntry = null;
				this.suFade = 0;
			}
		}
		if (this.message != null) {
			if (this.fade <= 0) {
				this.message = null;
				this.fade = 0;
				this.isfade = false;
			} else if (this.fade <= 20) {
				this.isfade = true;
			}

			this.fade--;
		}
		super.tick();
	}

	public void init() {
		int i = this.width / 2;
		int j = this.width / 3;
		int k = this.width / 4;

		if (TwitterForMinecraft.mctwitter == null) {
			this.list = new TwitterScreen.TweetList(this.minecraft);

			this.addButton(new ButtonWidget(0, this.height - 20, i, 20, I18n.translate("twitter.login"), (l) -> {
				this.minecraft.openScreen(new TwitterLoginScreen(this));
			}));

			this.addButton(new ButtonWidget(i, this.height - 20, i, 20, I18n.translate("gui.back"), (p_213034_1_) -> {
				this.onClose();
			}));
		} else {
			this.addButton(new ButtonWidget(0, this.height - 80, k - 10, 20, I18n.translate("tweet"), (press) -> {
				this.minecraft.openScreen(new TwitterTweetScreen(this));
			}));

			if (this.refreshTL != null) {
				this.refreshTL.y = this.height - 20;
				this.refreshTL.setWidth(i);
				this.refreshTL.setMessage(I18n.translate("twitter.refresh"));
				this.addButton(this.refreshTL);
			} else {
				this.refreshTL = this.addButton(new ButtonWidget(0, this.height - 20, i, 20, I18n.translate("twitter.refresh"), (p) -> {
					p.active = false;
					new TwitterThread(() -> {
						List<Status> t = Lists.newArrayList();
						try {
							t = TwitterUtil.getNonDuplicateStatuses(TwitterForMinecraft.tweets, TwitterForMinecraft.mctwitter.getHomeTimeline());
						} catch (TwitterException e) {
							this.accept(e.getErrorMessage());
						}
						Collections.reverse(t);
						for (Status s : t) {
							TwitterForMinecraft.tweets.add(0, s);
							TwitterForMinecraft.tweetSummaries.add(0, new TweetSummary(s));
							this.children.remove(this.list);
							this.list = new TwitterScreen.TweetList(this.minecraft);
							this.children.add(this.list);
						}
						p.active = true;
					}).start();
					this.minecraft.openScreen(this);
				}));
			}

			this.addButton(new ButtonWidget(0, this.height - 50, k - 10, 20, I18n.translate("tw.save.timeline"), (b) -> {
				b.active = false;
				try {
					TwitterForMinecraft.saveTimeline();
				} catch (IOException e) {
					this.accept(e.getLocalizedMessage());
				}
				b.active = true;
			}));

			this.addButton(new ButtonWidget(i, this.height - 20, i, 20, I18n.translate("gui.back"), (p_213034_1_) -> {
				this.onClose();
			}));
		}

		this.addButton(new ButtonWidget(0, this.height - 110, k - 10, 20, I18n.translate("tw.settings"), (b) -> {
			this.minecraft.openScreen(new TwitterSettingsScreen(this));
		}));

		if (this.refreshTL != null && this.refreshTL.active) {
			double scroll = this.list != null ? this.list.getScrollAmount() : 0.0D;
			this.list = new TwitterScreen.TweetList(this.minecraft);
			this.list.setScrollAmount(scroll);
			this.children.add(this.list);
		}

		super.init();
	}

	public void render(int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		if (this.parent != null) {
			this.parent.render(-1, -1, p_230430_4_);
		}
		if (this.minecraft.currentScreen == this) {
			this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		}
		super.render(p_230430_2_, p_230430_3_, p_230430_4_);
		this.list.render(p_230430_2_, p_230430_3_, p_230430_4_);
		for (AbstractButtonWidget b : this.buttons) {
			if (!b.getMessage().equals(I18n.translate("tweet")) && !b.getMessage().equals(I18n.translate("tw.save.timeline"))) {
				b.render(p_230430_2_, p_230430_3_, p_230430_4_);
			}
		}
		if (this.message != null) {
			List<String> list = this.font.wrapStringToWidthAsList(this.message, this.width / 2);
			RenderSystem.pushMatrix();
			RenderSystem.enableAlphaTest();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.isfade ? this.fade / 20 : 1.0F);
			this.renderTooltip(list, (this.width - this.font.getStringWidth(list.get(0))) / 2, this.height - list.size() * 10);
			RenderSystem.disableAlphaTest();
			RenderSystem.popMatrix();
		}
	}

	public void onClose() {
		this.minecraft.openScreen(this.parent);
	}

	public void accept(String errormsg) {
		this.message = errormsg;
		this.fade = 100;
	}

	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (!this.isHovering) {
			return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
		}

		return false;
	}

	public boolean renderTwitterUser(TweetSummary summary, int x, int y, int mouseX, int mouseY, float alpha) {
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

		int l1 = x;
		int i2 = y;
		int k = 0;
		k += icon != null ? 22 : 0;
		k += user.getName().isEmpty() ? 0 : 10;
		k += user.getScreenName().isEmpty() ? 0 : 10;
		k += 4 + (desc.size() * 10) + 4;
		k += ff.size() == 1 ? 10 : 20 + 2;

		if (i2 + k + 6 > this.height - 20) {
			i2 = this.height - 20 - k - 6;
		}

		int l = -267386864;
		this.fillGradient(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864, -267386864);
		this.fillGradient(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864, -267386864);
		this.fillGradient(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864, -267386864);
		this.fillGradient(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864, -267386864);
		this.fillGradient(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864, -267386864);
		int i1 = 1347420415;
		int j1 = 1344798847;
		this.fillGradient(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
		this.fillGradient(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
		this.fillGradient(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
		this.fillGradient(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847, 1344798847);
		MatrixStack matrixstack = new MatrixStack();
		VertexConsumerProvider.Immediate irendertypebuffer$impl = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		Matrix4f matrix4f = matrixstack.peek().getModel();

		int yy = i2;
		if (icon != null) {
			try {
				TwitterForMinecraft.getTextureManager().bindTexture(icon);
				DrawableHelper.blit(l1, i2, 0.0F, 0.0F, 20, 20, 20, 20);
				i2 += 20;
			} catch (Throwable e) {
				LOGGER.error("Throwable was thrown: {}", e.getLocalizedMessage());
			}
		}

		String name = new LiteralText(user.getName()).formatted(Formatting.BOLD).asFormattedString();
		String three = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
		List<String> namef = this.font.wrapStringToWidthAsList(name, i - this.font.getStringWidth(three));
		this.font.draw(namef.size() == 1 ? namef.get(0) : namef.get(0) + three, (float) l1, (float) i2 + 2, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
		this.font.draw(new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString(), (float) l1, (float) i2 + 12, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);

		for (int k1 = 0; k1 < desc.size(); ++k1) {
			String s1 = desc.get(k1);

			if (s1 != null) {
				this.font.draw(s1, (float) l1, (float) i2 + 26, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
			}

			i2 += 10;
		}

		if (ff.size() == 1) {
			this.font.draw(ff.get(0), (float) l1, (float) i2 + 30, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
		} else {
			this.font.draw(follow, (float) l1, (float) i2 + 30, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
			this.font.draw(follower, (float) l1, (float) i2 + 30 + 10, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
		}

		irendertypebuffer$impl.draw();
		RenderSystem.enableDepthTest();
		RenderSystem.enableRescaleNormal();

		if (l1 - 4 < mouseX && l1 + i + 4 > mouseX && yy - 4 < mouseY && yy + k + 4 > mouseY) {
			return true;
		}

		return false;
	}

	public void renderUserName(TweetSummary summary, int x, int y, int width) {
		boolean p = summary.getUser().isProtected();
		boolean v = summary.getUser().isVerified();

		String threeb = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
		int threebw = this.font.getStringWidth(threeb);
		String three = new LiteralText("...").formatted(Formatting.GRAY).asFormattedString();
		int threew = this.font.getStringWidth(three);
		String time = new LiteralText("・" + summary.getDifferenceTime()).formatted(Formatting.GRAY).asFormattedString();
		int timew = this.font.getStringWidth(time);
		String sname = new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString();
		String name = new LiteralText(summary.getUser().getName()).formatted(Formatting.BOLD).asFormattedString();

		int pvw = (p ? 10 : 0) + (v ? 10 : 0);
		List<String> namef = this.font.wrapStringToWidthAsList(name, width - pvw - timew);
		boolean isnar = namef.size() > 1;
		List<String> namef2 = isnar ? this.font.wrapStringToWidthAsList(name, width - pvw - timew - threebw) : namef;

		String formattedName = namef2.size() == 1 ? namef2.get(0) : namef2.get(0) + threeb;
		int fnamew = this.font.getStringWidth(formattedName);
		this.font.drawWithShadow(formattedName, x, y, Formatting.WHITE.getColorValue());
		x += fnamew;
		if (p) {
			this.minecraft.getTextureManager().bindTexture(PROTECTED);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(x, y, 0.0F);
			RenderSystem.scalef(0.035F, 0.035F, 0.035F);
			blit(0, 0, 0, 0, 246, 246, 246, 246);
			RenderSystem.popMatrix();
			x += 10;
		}
		if (v) {
			this.minecraft.getTextureManager().bindTexture(VERIFIED);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(x, y, 0.0F);
			RenderSystem.scalef(0.035F, 0.035F, 0.035F);
			blit(0, 0, 0, 0, 246, 246, 246, 246);
			RenderSystem.popMatrix();
			x += 10;
		}

		List<String> snamef = this.font.wrapStringToWidthAsList(sname, width - fnamew - pvw - timew - threew);
		if (!isnar) {
			String s = snamef.size() == 1 ? snamef.get(0) : snamef.get(0) + three;
			this.font.drawWithShadow(s, x, y, Formatting.GRAY.getColorValue());
			x += this.font.getStringWidth(s);
		}
		this.font.drawWithShadow(time, x, y, Formatting.GRAY.getColorValue());
	}

	public int renderRetweetedUser(@Nullable TweetSummary retweetedSummary, int icox, int x, int y) {
		if (retweetedSummary != null) {
			this.minecraft.getTextureManager().bindTexture(RETUSR);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(icox, y, 0.0F);
			RenderSystem.scalef(0.04F, 0.04F, 0.04F);
			blit(0, 0, 0, 0, 246, 186, 246, 186);
			RenderSystem.popMatrix();
			this.font.drawWithShadow(I18n.translate("tw.retweeted.user", retweetedSummary.getUser().getName()), x, y, Formatting.GRAY.getColorValue());
			return y + 10;
		}

		return y;
	}

	public TwitterScreen.TweetList getList() {
		return this.list;
	}

	@Environment(EnvType.CLIENT)
	public class TweetList extends ExtendedTwitterTweetList<TweetList.TweetEntry> {
		public TweetList(MinecraftClient mcIn) {
			super(mcIn, TwitterScreen.this.width, TwitterScreen.this.height, 0, TwitterScreen.this.height - 20);
			for (int i = 0; i < TwitterForMinecraft.tweetSummaries.size(); i++) {
				this.addEntry(new TweetEntry(TwitterForMinecraft.tweetSummaries.get(i)));
			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}

			this.setY(0);
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
			if (TwitterScreen.this.hoveredEntry != null && TwitterScreen.this.isRendering) {
				TweetEntry entry = TwitterScreen.this.hoveredEntry;
				TwitterScreen.this.isHovering = TwitterScreen.this.renderTwitterUser(entry.summary, this.getRowLeft() - 60, entry.getY() + (entry.retweetedSummary != null ? 10 : 0) + 2 + 22, p_render_1_, p_render_2_, TwitterScreen.this.suFade / 20);
			}
			TweetEntry e = this.getEntryAtPosition(p_render_1_, p_render_2_);
			if (e != null && this.getRowLeft() <= p_render_1_ && this.getRowLeft() + 16 >= p_render_1_ && e.getY() + (e.retweetedSummary != null ? 10 : 0) + 2 <= p_render_2_ && e.getY() + (e.retweetedSummary != null ? 10 : 0) + 2 + 16 >= p_render_2_) {
				if (!TwitterScreen.this.iconHovered) {
					TwitterScreen.this.iconHovered = true;
					TwitterScreen.this.hoveredEntry = e;
				} else if (TwitterScreen.this.hoveredEntry != e) {
					TwitterScreen.this.iconHovered = false;
				}
			} else if (TwitterScreen.this.iconHovered) {
				TwitterScreen.this.iconHovered = false;
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
			private final List<String> strs;
			private final List<String> qsStrs;
			private int height;
			private int y;

			private TwitterButton rep;
			private TwitterButton ret;
			private TwitterButton fav;
			private TwitterButton sha;
			private int fourBtnHeightOffset;

			public TweetEntry(TweetSummary tweet) {
				boolean flag = tweet.getRetweetedSummary() != null;
				this.summary = flag ? tweet.getRetweetedSummary() : tweet;
				this.retweetedSummary = flag ? tweet : null;
				this.quoteSourceSummary = this.summary.getQuotedTweetSummary();
				this.strs = TwitterScreen.this.font.wrapStringToWidthAsList(this.summary.getText(), TweetList.this.getRowWidth() - 25);
				this.qsStrs = this.quoteSourceSummary != null ? TwitterScreen.this.font.wrapStringToWidthAsList(this.quoteSourceSummary.getText(), TweetList.this.getRowWidth() - 40) : Lists.newArrayList();
				this.height = ((this.strs.size() - 1) * 10) + 10 + 30;
				this.height += this.summary.isIncludeImages() || this.summary.isIncludeVideo() ? 120 : 0;
				this.height += flag ? 10 : 0;
				this.height += this.quoteSourceSummary != null ? 20 + this.qsStrs.size() * 10 : 0;
				this.fourBtnHeightOffset = this.height - 14;
			}

			public void tick() {
				this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
			}

			public void init() {
				int i = TwitterScreen.TweetList.this.getRowLeft() + 24;

				this.rep = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 246, REP, 246, 492, 246, 246, (p) -> {

				}));

				this.ret = this.addButton(new TwitterButton(i + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 177, RET, 246, 354, 246, 177, (p) -> {

				}));

				this.fav = this.addButton(new TwitterButton(i + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, this.summary.isFavorited() ? 0 : 233, this.summary.isFavorited() ? FAVED : FAV, 246, this.summary.isFavorited() ? 233 : 466, 246, 233, (b) -> {
					try {
						if (this.summary.isFavorited()) {
							TwitterForMinecraft.mctwitter.destroyFavorite(this.summary.getId());
							this.summary.favorite(false);
							this.fav.setImage(FAV);
							this.fav.setWhenHovered(233);
							this.fav.setSize(246, 466);
						} else {
							TwitterForMinecraft.mctwitter.createFavorite(this.summary.getId());
							this.summary.favorite(true);
							this.fav.setImage(FAVED);
							this.fav.setWhenHovered(0);
							this.fav.setSize(246, 233);
						}
					} catch (TwitterException e) {
						TwitterScreen.this.accept(I18n.translate("tw.failed.like", e.getErrorMessage()));
					}
				}));

				this.sha = this.addButton(new TwitterButton(i + 60 + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 246, SHA, 246, 492, 246, 246, (p) -> {

				}));
			}

			public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {
				InputStream icon = this.summary.getUserIconData();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableBlend();

				int nowy = rowTop;
				nowy = TwitterScreen.this.renderRetweetedUser(this.retweetedSummary, rowLeft + 6, rowLeft + 24, nowy);

				try {
					if (icon != null) {
						TwitterForMinecraft.getTextureManager().bindTexture(icon);
						DrawableHelper.blit(rowLeft, nowy, 0.0F, 0.0F, 16, 16, 16, 16);
					}
					this.renderPhotos(rowLeft, nowy);
				} catch (Throwable t) {
					LOGGER.error("Can't rendering: {}", t.getLocalizedMessage());
				}
				RenderSystem.disableBlend();

				TwitterScreen.this.renderUserName(this.summary, rowLeft + 24, nowy, rowWidth - 24);

				for (int i = 0; i < this.strs.size(); i++) {
					TwitterScreen.this.font.drawWithShadow(this.strs.get(i), (float) (rowLeft + 24), (float) (nowy + 10 + i * 10), 16777215);
				}
				nowy += 10 + this.strs.size() * 10;

				if (this.summary.isIncludeVideo()) {
					TwitterScreen.this.fillGradient(rowLeft + 24, nowy, rowLeft + 24 + 208, nowy + 117, -1072689136, -804253680);
					if (mouseX >= rowLeft + 24 && mouseX <= rowLeft + 24 + 208 && mouseY >= nowy && mouseY <= nowy + 117) {
						TwitterScreen.this.renderTooltip(I18n.translate("tw.play.video"), mouseX, mouseY);
					}
					nowy += 117;
				}

				if (this.quoteSourceSummary != null) {
					nowy += 10;
					InputStream qsIco = this.quoteSourceSummary.getUserIconData();
					if (qsIco != null) {
						try {
							TwitterForMinecraft.getTextureManager().bindTexture(qsIco);
							blit(rowLeft + 24 + 5, nowy, 0.0F, 0.0F, 10, 10, 10, 10);
						} catch (Throwable throwable) {
							LOGGER.error("Can't rendering", throwable);
						}
					}
					TwitterScreen.this.renderUserName(this.quoteSourceSummary, rowLeft + 24 + 5 + 10 + 4, nowy, TweetList.this.getRowWidth() - 24 - 5 - 10 - 4 - 10);
					for (int i = 0; i < this.qsStrs.size(); i++) {
						TwitterScreen.this.font.drawWithShadow(this.qsStrs.get(i), rowLeft + 24 + 5, nowy + 10 + i * 10, Formatting.WHITE.getColorValue());
					}
					nowy += 10 + this.qsStrs.size() * 10;
				}

				super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);

				if (this.summary.getRetweetCount() != 0) {
					TwitterScreen.this.font.drawWithShadow("" + this.summary.getRetweetCountF(), this.ret.x + 16.0F, this.ret.y, Formatting.GRAY.getColorValue());
				}
				if (this.summary.getFavoriteCount() != 0) {
					TwitterScreen.this.font.drawWithShadow("" + this.summary.getFavoriteCountF(), this.fav.x + 16.0F, this.fav.y, Formatting.GRAY.getColorValue());
				}
			}

			public void renderPhotos(int rowLeft, int rowTop) throws Throwable {
				List<ITwitterPhotoMedia> p = this.summary.getPhotoMedias();
				if (p.size() == 0) {
					return;
				} else if (p.size() == 1) {
					ITwitterPhotoMedia imedia = p.get(0);
					InputStream data = imedia.getData();
					if (data != null) {
						Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(208, 117));
						TwitterForMinecraft.getTextureManager().bindTexture(data);
						DrawableHelper.blit(rowLeft + 24, rowTop + 11 + this.strs.size() * 10, 0.0F, 0.0F, 208, 117, d.width, d.height);
					}
					return;
				} else if (p.size() == 2) {
					for (int i = 0; i < 2; i++) {
						ITwitterPhotoMedia imedia = p.get(i);
						InputStream data = imedia.getData();
						if (data != null) {
							Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(104, 117));
							TwitterForMinecraft.getTextureManager().bindTexture(data);
							DrawableHelper.blit(rowLeft + 24 + i * 105, rowTop + 11 + this.strs.size() * 10, 0.0F, 0.0F, 104, 117, d.width, d.height);
						}
					}
					return;
				} else if (p.size() == 3) {
					for (int i = 0; i < 3; i++) {
						ITwitterPhotoMedia imedia = p.get(i);
						InputStream data = imedia.getData();
						if (data != null) {
							if (i == 0) {
								Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(104, 117));
								TwitterForMinecraft.getTextureManager().bindTexture(data);
								DrawableHelper.blit(rowLeft + 24, rowTop + 11 + this.strs.size() * 10, 0.0F, 0.0F, 104, 117, d.width, d.height);
							} else {
								Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(104, 58));
								TwitterForMinecraft.getTextureManager().bindTexture(data);
								DrawableHelper.blit(rowLeft + 24 + 105, (rowTop + 11 + this.strs.size() * 10) + ((i - 1) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
							}
						}
					}
					return;
				} else if (p.size() == 4) {
					for (int i = 0; i < 4; i++) {
						ITwitterPhotoMedia imedia = p.get(i);
						InputStream data = imedia.getData();
						if (data != null) {
							if (i == 0 || i == 2) {
								Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(104, 58));
								TwitterForMinecraft.getTextureManager().bindTexture(data);
								DrawableHelper.blit(rowLeft + 24, (rowTop + 11 + this.strs.size() * 10) + ((i / 2) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
							} else {
								Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(104, 58));
								TwitterForMinecraft.getTextureManager().bindTexture(data);
								DrawableHelper.blit(rowLeft + 24 + 105, (rowTop + 11 + this.strs.size() * 10) + ((int) (i / 3) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
							}
						}
					}
					return;
				}
			}

			public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
				int i = TweetList.this.getRowLeft() + 24;
				int j = this.y + (this.retweetedSummary != null ? 10 : 0) + 11 + this.strs.size() * 10;
				int k = this.summary.getPhotoMediaLength();

				if (k == 1) {
					if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 0);
					}
				} else if (k == 2) {
					if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 104 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 0);
					} else if (p_mouseClicked_1_ >= i + 105 && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 1);
					}
				} else if (k == 3) {
					if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 104 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 0);
					} else if (p_mouseClicked_1_ >= i + 105 && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 58) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 1);
					} else if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j + 59 && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 2);
					}
				} else if (k == 4) {
					if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 104 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 58) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 0);
					} else if (p_mouseClicked_1_ >= i + 105 && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 58) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 1);
					} else if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 104 && p_mouseClicked_3_ >= j + 59 && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 2);
					} else if (p_mouseClicked_1_ >= i + 105 && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j + 59 && p_mouseClicked_3_ <= j + 117) {
						return this.displayTwitterPhotoAndShowStatusScreen(p_mouseClicked_5_, 3);
					}
				}

				if (this.summary.isIncludeVideo()) {
					if (p_mouseClicked_1_ >= i && p_mouseClicked_1_ <= i + 208 && p_mouseClicked_3_ >= j && p_mouseClicked_3_ <= j + 117) {
						return this.videoClicked(p_mouseClicked_5_);
					}
				}

				for (AbstractButtonWidget w : this.buttons) {
					if (w.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
						return true;
					}
				}

				if (p_mouseClicked_5_ == 0) {
					TwitterScreen.TweetList.this.setSelected(this);
					//TODO show showStatusScreen action
					return true;
				} else {
					return false;
				}
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
		}
	}
}
