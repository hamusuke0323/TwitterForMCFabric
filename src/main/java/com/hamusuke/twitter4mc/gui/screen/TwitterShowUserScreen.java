package com.hamusuke.twitter4mc.gui.screen;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.tweet.UserSummary;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import twitter4j.User;

import java.io.InputStream;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TwitterShowUserScreen extends AbstractTwitterScreen {
	private final UserSummary user;
	private List<StringVisitable> name = Lists.newArrayList();

	public TwitterShowUserScreen(@Nullable Screen parent, User user) {
		super(new LiteralText(user.getName()).formatted(Formatting.BOLD), parent);
		this.user = new UserSummary(user);
	}

	protected void init() {
		List<StringVisitable> wrapped = this.textRenderer.getTextHandler().wrapLines(this.title, this.width / 2 - 20, Style.EMPTY);
		this.name = wrapped;
		int fontHeight = this.textRenderer.fontHeight;
		int top = fontHeight * wrapped.size() + fontHeight;

		if (!this.user.isGettingUserTimeline()) {
			if (!this.user.isAlreadyGotUserTimeline()) {
				this.user.startGettingUserTimeline(() -> {
					double scroll = 0.0D;
					if (this.list != null) {
						scroll = this.list.getScrollAmount();
						this.remove(this.list);
					}

					this.list = new TweetList(this.client, top, this.user);
					this.list.setScrollAmount(scroll);
					this.addDrawableChild(this.list);
				});
			} else {
				double scroll = this.list != null ? this.list.getScrollAmount() : 0.0D;
				this.list = new TweetList(this.client, top, this.user);
				this.list.setScrollAmount(scroll);
				this.addDrawableChild(this.list);
			}
		}

		super.init();
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

		if (this.list != null) {
			this.list.render(matrices, mouseX, mouseY, delta);
		}
		super.render(matrices, mouseX, mouseY, delta);
		float left = (float) (this.width / 2 - this.width / 4 + 2);
		float y = 0.0F;
		for (StringVisitable stringVisitable : this.name) {
			this.drawWithShadowAndEmoji(matrices, stringVisitable.getString(), left + 20, y, 16777215);
			y += this.textRenderer.fontHeight;
		}
		this.textRenderer.drawWithShadow(matrices, new TranslatableText("tw.statuses.count", this.user.getStatusesCount()).formatted(Formatting.GRAY), left + 20, y, 16777215);

		this.renderMessage(matrices);
	}

	@Environment(EnvType.CLIENT)
	private class TweetList extends AbstractTwitterScreen.TweetList {
		private TweetList(MinecraftClient mcIn, int top, UserSummary userSummary) {
			super(mcIn, TwitterShowUserScreen.this.width, TwitterShowUserScreen.this.height, top, TwitterShowUserScreen.this.height - 20);
			this.addEntry(new TwitterShowUserScreen.TweetList.UserProfile(userSummary));
			for (TweetSummary summary : userSummary.getUserTimeline()) {
				this.addEntry(new TwitterShowUserScreen.TweetList.TweetEntry(summary));
			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}
		}

		public void setSelected(@Nullable TweetEntry entry) {
			if (!(entry instanceof UserProfile)) {
				super.setSelected(entry);
			}
		}

		@Environment(EnvType.CLIENT)
		private class UserProfile extends TweetEntry {
			private final UserSummary summary;
			private final List<StringVisitable> name;
			private final List<StringVisitable> desc;

			private UserProfile(UserSummary summary) {
				super(null);
				this.summary = summary;
				boolean p = this.summary.isProtected();
				boolean v = this.summary.isVerified();
				int protectedVerifiedWidth = (p ? 10 : 0) + (v ? 10 : 0);
				this.name = TwitterShowUserScreen.this.textRenderer.getTextHandler().wrapLines(new LiteralText(this.summary.getName()).formatted(Formatting.BOLD), TweetList.this.getRowWidth() - 10 - protectedVerifiedWidth, Style.EMPTY);
				this.desc = TwitterShowUserScreen.this.textRenderer.getTextHandler().wrapLines(this.summary.getDescription(), TweetList.this.getRowWidth() - 20, Style.EMPTY);
				this.height = TwitterShowUserScreen.TweetList.this.getRowWidth() / 3 + 60 + this.desc.size() * TwitterShowUserScreen.this.textRenderer.fontHeight;
			}

			public void init() {
			}

			public void render(MatrixStack matrices, int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float delta) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableBlend();

				int i = rowWidth / 3;
				int j = rowWidth / 5;

				InputStream header = this.summary.getHeader();
				InputStream icon = this.summary.getIcon();

				if (header == null) {
					TwitterShowUserScreen.this.client.getTextureManager().bindTexture(MissingSprite.getMissingSpriteId());
				} else {
					TwitterForMC.getTextureManager().bindTexture(this.summary.getHeader());
				}
				drawTexture(matrices, rowLeft, rowTop, 0.0F, 0.0F, rowWidth, i, rowWidth, i);

				if (icon == null) {
					TwitterShowUserScreen.this.client.getTextureManager().bindTexture(MissingSprite.getMissingSpriteId());
				} else {
					TwitterForMC.getTextureManager().bindTexture(this.summary.getIcon());
				}
				drawTexture(matrices, rowLeft + 10, rowTop + (i - i / 3), 0.0F, 0.0F, j, j, j, j);

				int k = rowTop + (i - i / 3) + j;
				int x = 0;
				for (int index = 0; index < this.name.size(); index++) {
					int xx = TwitterShowUserScreen.this.drawWithShadowAndEmoji(matrices, this.name.get(index).getString(), rowLeft + 10, k, 16777215);
					k += TwitterShowUserScreen.this.textRenderer.fontHeight;
					x = index == this.name.size() - 1 ? xx : rowLeft + 10;
				}

				if (this.summary.isProtected()) {
					x += TwitterShowUserScreen.this.renderProtected(matrices, x, k);
				}
				if (this.summary.isVerified()) {
					TwitterShowUserScreen.this.renderVerified(matrices, x, k);
				}

				TwitterShowUserScreen.this.drawWithShadowAndEmoji(matrices, new LiteralText(this.summary.getScreenName()).formatted(Formatting.GRAY).asString(), rowLeft + 10, k, 0);

				for (int index = 0; index < this.desc.size(); index++) {
					TwitterShowUserScreen.this.drawWithShadowAndEmoji(matrices, this.desc.get(index).getString(), rowLeft + 10, k + TwitterShowUserScreen.this.textRenderer.fontHeight * 2 + index * TwitterShowUserScreen.this.textRenderer.fontHeight, 16777215);
				}
			}
		}
	}
}
