package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.hamusuke.twitter4mc.tweet.UserSummary;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import twitter4j.User;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TwitterShowUserScreen extends AbstractTwitterScreen implements DisplayableMessage {
	@Nullable
	private final Screen parent;
	private final UserSummary user;

	public TwitterShowUserScreen(@Nullable Screen parent, User user) {
		super(new LiteralText(user.getName()).formatted(Formatting.BOLD));
		this.parent = parent;
		this.user = new UserSummary(user);
	}

	public void tick() {
		if (this.list != null) {
			this.list.tick();
		}

		super.tick();
	}

	protected void init() {
		if(this.parent != null) {
			this.parent.init(this.minecraft, this.width, this.height);
		}

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

	public void onClose() {
		this.minecraft.openScreen(this.parent);
	}

	@Environment(EnvType.CLIENT)
	private class TweetList extends AbstractTwitterScreen.TweetList {
		private TweetList(MinecraftClient mcIn, UserSummary userSummary) {
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

		@Environment(EnvType.CLIENT)
		private class UserProfile extends TweetEntry {
			private final UserSummary summary;
			private final List<String> desc;

			private UserProfile(UserSummary summary) {
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
			}

			protected boolean mayClickIcon(double x, double y) {
				return false;
			}
		}

		@Environment(EnvType.CLIENT)
		private class TweetEntry extends AbstractTwitterScreen.TweetList.TweetEntry {
			private TweetEntry(@Nullable TweetSummary tweet) {
				super(tweet);
			}

			public void tick() {
				if (this.isNotNull()) {
					super.tick();
				}
			}

			public void init() {
				if (this.isNotNull()) {
					super.init();
				}
			}

			public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {
				if (this.isNotNull()) {
					super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);
				}
			}

			public int renderPhotos(int rowLeft, int rowTop) {
				if (this.isNotNull()) {
					return super.renderPhotos(rowLeft, rowTop);
				}

				return 0;
			}

			public boolean mouseClicked(double x, double y, int button) {
				if (this.isNotNull()) {
					return super.mouseClicked(x, y, button);
				}

				return false;
			}

			protected boolean displayTwitterPhotoAndShowStatusScreen(int mouseButton, int index) {
				if (this.isNotNull()) {
					return super.displayTwitterPhotoAndShowStatusScreen(mouseButton, index);
				}

				return false;
			}

			protected boolean videoClicked(int mouseButton) {
				if (this.isNotNull()) {
					return super.videoClicked(mouseButton);
				}

				return false;
			}

			public void setY(int y) {
				this.y = y;

				if (this.isNotNull()) {
					this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
				}
			}

			public boolean equals(Object obj) {
				if (this.isNotNull()) {
					return super.equals(obj);
				}

				return false;
			}

			public int hashCode() {
				if (this.isNotNull()) {
					return super.hashCode();
				}

				return 0;
			}

			private boolean isNotNull() {
				return this.summary != null;
			}
		}
	}
}
