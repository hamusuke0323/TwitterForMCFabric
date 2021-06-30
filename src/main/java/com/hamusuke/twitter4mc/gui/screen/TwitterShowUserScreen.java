package com.hamusuke.twitter4mc.gui.screen;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.widget.TwitterButton;
import com.hamusuke.twitter4mc.gui.widget.list.ExtendedTwitterTweetList;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.hamusuke.twitter4mc.tweet.UserSummary;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import twitter4j.TwitterException;
import twitter4j.User;

import java.awt.*;
import java.io.InputStream;
import java.util.List;

//TODO
@Environment(EnvType.CLIENT)
public class TwitterShowUserScreen extends ParentalScreen implements DisplayableMessage {
	private final UserSummary user;
	@Nullable
	private TwitterShowUserScreen.TweetList list;
	@Nullable
	private String message;
	private int fade;
	private boolean isFade;

	public TwitterShowUserScreen(Screen parent, User user) {
		super(new LiteralText(user.getName()).formatted(Formatting.BOLD), parent);
		this.user = new UserSummary(user);
	}

	public void tick() {
		if (this.list != null) {
			this.list.tick();
		}

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

	protected void init() {
		if (!this.user.isGettingUserTimeline()) {
			if (!this.user.isAlreadyGotUserTimeline()) {
				this.user.startGettingUserTimeline(() -> {
					double scroll = 0.0D;
					if (this.list != null) {
						scroll = this.list.getScrollAmount();
						this.children.remove(this.list);
					}

					this.list = new TweetList(this.minecraft, this.user);
					this.list.setScrollAmount(scroll);
					this.children.add(this.list);
				});
			} else {
				double scroll = this.list != null ? this.list.getScrollAmount() : 0.0D;
				this.list = new TweetList(this.minecraft, this.user);
				this.list.setScrollAmount(scroll);
				this.children.add(this.list);
			}
		}

		super.init();
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

		if (this.list != null) {
			this.list.render(mouseX, mouseY, delta);
		}
		super.render(mouseX, mouseY, delta);

		float left = (float) (this.width / 2 - this.width / 4 + 2);
		this.font.drawWithShadow(this.title.asFormattedString(), left + 20, 0.0F, 16777215);
		this.font.drawWithShadow(new TranslatableText("tw.statuses.count", this.user.getStatusesCount()).formatted(Formatting.GRAY).asFormattedString(), left + 20, 9.0F, 16777215);

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

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.list != null && this.list.hoveringEntry != null && this.list.hoveringEntry.mayClickIcon(mouseX, mouseY)) {
			this.minecraft.openScreen(new TwitterShowUserScreen(this, this.list.hoveringEntry.tweetSummary.getUser()));
			return true;
		} else if (this.list != null && !this.list.isHovering) {
			return super.mouseClicked(mouseX, mouseY, button);
		}

		return false;
	}

	public void accept(String errorMsg) {
		this.message = errorMsg;
		this.fade = 100;
	}

	@Environment(EnvType.CLIENT)
	public class TweetList extends ExtendedTwitterTweetList<TweetList.TweetEntry> {
		@Nullable
		private TwitterShowUserScreen.TweetList.TweetEntry hoveringEntry;
		private boolean isHovering;
		private int fade;

		public TweetList(MinecraftClient mcIn, UserSummary userSummary) {
			super(mcIn, TwitterShowUserScreen.this.width, TwitterShowUserScreen.this.height, 18, TwitterShowUserScreen.this.height - 20);
			this.addEntry(new TwitterShowUserScreen.TweetList.UserProfile(userSummary));
			for (TweetSummary summary : userSummary.getUserTimeline()) {
				this.addEntry(new TwitterShowUserScreen.TweetList.TweetEntry(summary));
			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}

			this.setY(0);
		}

		public void tick() {
			this.fade = this.isHovering ? 20 : this.fade - 1;
			this.children().forEach(TweetEntry::tick);
			super.tick();
		}

		protected int getScrollbarPosition() {
			return TwitterShowUserScreen.this.width - 5;
		}

		public int getRowWidth() {
			return TwitterShowUserScreen.this.width / 2;
		}

		protected void renderBackground() {
		}

		public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
			super.render(p_render_1_, p_render_2_, p_render_3_);

			TweetEntry tweetEntry = this.getEntryAtPosition(p_render_1_, p_render_2_);
			TwitterShowUserScreen.TweetList.TweetEntry e = (tweetEntry instanceof UserProfile) ? null : tweetEntry;
			if (this.hoveringEntry != null) {
				this.isHovering = TwitterForMC.twitterScreen.renderTwitterUser(this.hoveringEntry.tweetSummary, this.getRowLeft() - 60, this.hoveringEntry.getY() + this.hoveringEntry.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
				if (!this.isHovering && this.fade < 0) {
					this.hoveringEntry = null;
					this.fade = 0;
				}
			} else if (e != null && this.getRowLeft() <= p_render_1_ && this.getRowLeft() + 16 >= p_render_1_ && e.getY() + e.retweetedUserNameHeight + 2 <= p_render_2_ && e.getY() + e.retweetedUserNameHeight + 2 + 16 >= p_render_2_) {
				this.hoveringEntry = e;
				this.isHovering = TwitterForMC.twitterScreen.renderTwitterUser(e.tweetSummary, this.getRowLeft() - 60, e.getY() + e.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
				this.fade = 20;
			}
		}

		protected boolean isFocused() {
			return TwitterShowUserScreen.this.getFocused() == this;
		}

		@Environment(EnvType.CLIENT)
		public class UserProfile extends TweetEntry {
			private final UserSummary summary;
			private final List<String> desc;

			public UserProfile(UserSummary summary) {
				super(null);
				this.summary = summary;
				this.desc = TwitterShowUserScreen.this.font.wrapStringToWidthAsList(this.summary.getDescription(), TweetList.this.getRowWidth() - 20);
				this.height = TwitterShowUserScreen.TweetList.this.getRowWidth() / 3 + 60 + this.desc.size() * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight;
			}

			public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableBlend();

				int i = rowWidth / 3;
				int j = rowWidth / 5;
				TwitterForMC.getTextureManager().bindTexture(this.summary.getHeader());
				blit(rowLeft, rowTop, 0.0F, 0.0F, rowWidth, i, rowWidth, i);

				TwitterForMC.getTextureManager().bindTexture(this.summary.getIcon());
				blit(rowLeft + 10, rowTop + (i - i / 3), 0.0F, 0.0F, j, j, j, j);

				int k = rowTop + (i - i / 3) + j;
				int x = TwitterShowUserScreen.this.font.drawWithShadow(new LiteralText(this.summary.getName()).formatted(Formatting.BOLD).asFormattedString(), rowLeft + 10, k, 16777215);
				if (this.summary.isProtected()) {
					x += TwitterUtil.renderProtected(TwitterShowUserScreen.this.minecraft, x, k);
				}
				if (this.summary.isVerified()) {
					TwitterUtil.renderVerified(TwitterShowUserScreen.this.minecraft, x, k);
				}

				TwitterShowUserScreen.this.font.drawWithShadow(new LiteralText(this.summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString(), rowLeft + 10, k + 9, 0);

				for (int index = 0; index < this.desc.size(); index++) {
					TwitterShowUserScreen.this.font.drawWithShadow(this.desc.get(index), rowLeft + 10, k + 27 + index * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight, 16777215);
				}

				super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);
			}
		}

		@Environment(EnvType.CLIENT)
		public class TweetEntry extends ExtendedTwitterTweetList.AbstractTwitterListEntry<TwitterShowUserScreen.TweetList.TweetEntry> {
			@Nullable
			private final TweetSummary tweetSummary;
			@Nullable
			private final TweetSummary retweetedSummary;
			@Nullable
			private final TweetSummary quoteSourceSummary;
			private final List<String> strings;
			private final List<String> quotedTweetStrings;
			private final int retweetedUserNameHeight;
			protected int height;
			protected int y;
			private TwitterButton rep;
			private TwitterButton ret;
			private TwitterButton fav;
			private TwitterButton sha;
			private final int fourBtnHeightOffset;

			public TweetEntry(@Nullable TweetSummary tweet) {
				if (tweet != null) {
					boolean flag = tweet.getRetweetedSummary() != null;
					this.tweetSummary = flag ? tweet.getRetweetedSummary() : tweet;
					this.retweetedSummary = flag ? tweet : null;
					this.quoteSourceSummary = this.tweetSummary.getQuotedTweetSummary();
					this.strings = TwitterShowUserScreen.this.font.wrapStringToWidthAsList(this.tweetSummary.getText(), TwitterShowUserScreen.TweetList.this.getRowWidth() - 25);
					this.quotedTweetStrings = this.quoteSourceSummary != null ? TwitterShowUserScreen.this.font.wrapStringToWidthAsList(this.quoteSourceSummary.getText(), TwitterShowUserScreen.TweetList.this.getRowWidth() - 40) : Lists.newArrayList();
					this.height = ((this.strings.size() - 1) * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight) + 10 + 30;
					this.height += this.tweetSummary.isIncludeImages() || this.tweetSummary.isIncludeVideo() ? 120 : 0;
					this.retweetedUserNameHeight = flag ? TwitterUtil.wrapUserNameToWidth(TwitterShowUserScreen.this.minecraft, this.retweetedSummary, TwitterShowUserScreen.TweetList.this.getRowWidth() - 24).size() * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight : 0;
					this.height += this.retweetedUserNameHeight;
					this.height += this.quoteSourceSummary != null ? 20 + this.quotedTweetStrings.size() * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight : 0;
					this.fourBtnHeightOffset = this.height - 14;
				} else {
					this.tweetSummary = null;
					this.retweetedSummary = null;
					this.quoteSourceSummary = null;
					this.strings = Lists.newArrayList();
					this.quotedTweetStrings = Lists.newArrayList();
					this.height = 0;
					this.retweetedUserNameHeight = 0;
					this.fourBtnHeightOffset = 0;
				}
			}

			public void tick() {
				if (this.isNotNull()) {
					this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
				}
			}

			public void init() {
				if (this.isNotNull()) {
					int i = TwitterShowUserScreen.TweetList.this.getRowLeft() + 24;

					this.rep = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, TwitterUtil.REP, 16, 32, 16, 16, (p) -> {

					}));

					this.ret = this.addButton(new TwitterButton(i + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, TwitterUtil.RET, 16, 32, 16, 16, (p) -> {

					}));

					this.fav = this.addButton(new TwitterButton(i + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, this.tweetSummary.isFavorited() ? 0 : 16, this.tweetSummary.isFavorited() ? TwitterUtil.FAVED : TwitterUtil.FAV, 16, this.tweetSummary.isFavorited() ? 16 : 32, 16, 16, (b) -> {
						try {
							if (this.tweetSummary.isFavorited()) {
								TwitterForMC.mctwitter.destroyFavorite(this.tweetSummary.getId());
								this.tweetSummary.favorite(false);
								this.fav.setImage(TwitterUtil.FAV);
								this.fav.setWhenHovered(16);
								this.fav.setSize(16, 32);
							} else {
								TwitterForMC.mctwitter.createFavorite(this.tweetSummary.getId());
								this.tweetSummary.favorite(true);
								this.fav.setImage(TwitterUtil.FAVED);
								this.fav.setWhenHovered(0);
								this.fav.setSize(16, 16);
							}
						} catch (TwitterException e) {
							TwitterShowUserScreen.this.accept(I18n.translate("tw.failed.like", e.getErrorMessage()));
						}
					}));

					this.sha = this.addButton(new TwitterButton(i + 60 + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, TwitterUtil.SHA, 16, 32, 16, 16, (p) -> {

					}));
				}
			}

			public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {
				if (this.isNotNull()) {
					InputStream icon = this.tweetSummary.getUserIconData();
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.enableBlend();

					int nowY = rowTop;
					nowY = TwitterUtil.renderRetweetedUser(TwitterShowUserScreen.this.minecraft, this.retweetedSummary, rowLeft + 6, rowLeft + 24, nowY, rowWidth - 24);

					if (icon != null) {
						TwitterForMC.getTextureManager().bindTexture(icon);
						DrawableHelper.blit(rowLeft, nowY, 0.0F, 0.0F, 16, 16, 16, 16);
					}

					RenderSystem.disableBlend();

					TwitterUtil.renderUserName(TwitterShowUserScreen.this.minecraft, this.tweetSummary, rowLeft + 24, nowY, rowWidth - 24);

					for (int i = 0; i < this.strings.size(); i++) {
						TwitterShowUserScreen.this.font.drawWithShadow(this.strings.get(i), (float) (rowLeft + 24), (float) (nowY + 10 + i * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight), 16777215);
					}
					nowY += 10 + this.strings.size() * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight;

					if (this.tweetSummary.isIncludeVideo()) {
						TwitterShowUserScreen.this.fillGradient(rowLeft + 24, nowY, rowLeft + 24 + 208, nowY + 117, -1072689136, -804253680);
						if (mouseX >= rowLeft + 24 && mouseX <= rowLeft + 24 + 208 && mouseY >= nowY && mouseY <= nowY + 117) {
							TwitterShowUserScreen.this.renderTooltip(I18n.translate("tw.play.video"), mouseX, mouseY);
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
						TwitterUtil.renderUserName(TwitterShowUserScreen.this.minecraft, this.quoteSourceSummary, rowLeft + 24 + 5 + 10 + 4, nowY, TwitterShowUserScreen.TweetList.this.getRowWidth() - 24 - 5 - 10 - 4 - 10);
						for (int i = 0; i < this.quotedTweetStrings.size(); i++) {
							TwitterShowUserScreen.this.font.drawWithShadow(this.quotedTweetStrings.get(i), rowLeft + 24 + 5, nowY + 10 + i * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight, 16777215);
						}
						nowY += 10 + this.quotedTweetStrings.size() * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight;
					}

					super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);

					if (this.tweetSummary.getRetweetCount() != 0) {
						TwitterShowUserScreen.this.font.drawWithShadow("" + this.tweetSummary.getRetweetCountF(), this.ret.x + 16.0F, this.ret.y, 11184810);
					}
					if (this.tweetSummary.getFavoriteCount() != 0) {
						TwitterShowUserScreen.this.font.drawWithShadow("" + this.tweetSummary.getFavoriteCountF(), this.fav.x + 16.0F, this.fav.y, 11184810);
					}
				} else {
					super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);
				}
			}

			public int renderPhotos(int rowLeft, int rowTop) {
				if (this.isNotNull()) {
					List<TwitterPhotoMedia> p = this.tweetSummary.getPhotoMedias();
					if (p.size() == 1) {
						TwitterPhotoMedia media = p.get(0);
						InputStream data = media.getData();
						if (data != null) {
							Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(208, 117));
							TwitterForMC.getTextureManager().bindTexture(data);
							DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, 0.0F, 208, 117, d.width, d.height);
						}
					} else if (p.size() == 2) {
						for (int i = 0; i < 2; i++) {
							TwitterPhotoMedia media = p.get(i);
							InputStream data = media.getData();
							if (data != null) {
								Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 117));
								TwitterForMC.getTextureManager().bindTexture(data);
								DrawableHelper.blit(rowLeft + 24 + i * 105, rowTop, 0.0F, 0.0F, 104, 117, d.width, d.height);
							}
						}
					} else if (p.size() == 3) {
						for (int i = 0; i < 3; i++) {
							TwitterPhotoMedia media = p.get(i);
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
							TwitterPhotoMedia media = p.get(i);
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
				} else {
					return 0;
				}
			}

			public boolean mouseClicked(double x, double y, int button) {
				if (this.isNotNull()) {
					int i = TwitterShowUserScreen.TweetList.this.getRowLeft() + 24;
					int j = this.y + this.retweetedUserNameHeight + 11 + this.strings.size() * TwitterShowUserScreen.this.minecraft.textRenderer.fontHeight;
					int k = this.tweetSummary.getPhotoMediaLength();
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

					if (this.tweetSummary.isIncludeVideo()) {
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
						TwitterShowUserScreen.TweetList.this.setSelected(this);
						//TODO show showStatusScreen action
						return true;
					} else {
						return false;
					}
				} else {
					return super.mouseClicked(x, y, button);
				}
			}

			private boolean mayClickIcon(double x, double y) {
				int i = TwitterShowUserScreen.TweetList.this.getRowLeft();
				int j = this.y + this.retweetedUserNameHeight;
				return x > i && x < i + 16 && y > j && y < j + 16;
			}

			private boolean displayTwitterPhotoAndShowStatusScreen(int mouseButton, int index) {
				if (this.isNotNull()) {
					TwitterShowUserScreen.this.minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
					if (mouseButton == 0) {
						TwitterShowUserScreen.this.minecraft.openScreen(new TwitterPhotoAndShowStatusScreen(TwitterShowUserScreen.this, this.tweetSummary, index));
					} else if (mouseButton == 1) {
						//TODO save picture action;
					}
				}

				return true;
			}

			private boolean videoClicked(int mouseButton) {
				if (this.isNotNull() && !this.tweetSummary.isVideoNull()) {
					if (mouseButton == 0) {
						this.tweetSummary.getPlayer().play(TwitterShowUserScreen.this.minecraft.getWindow().getX(), TwitterShowUserScreen.this.minecraft.getWindow().getY(), TwitterShowUserScreen.this.minecraft.getWindow().getWidth() / 2, TwitterShowUserScreen.this.minecraft.getWindow().getHeight() / 2);
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

				if (this.isNotNull()) {
					this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
				}
			}

			private boolean isNotNull() {
				return this.tweetSummary != null;
			}
		}
	}
}
