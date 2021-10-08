package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.widget.TwitterTweetFieldWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.TwitterException;
import twitter4j.util.CharacterUtil;

@Environment(EnvType.CLIENT)
public class TwitterTweetScreen extends ParentalScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private TwitterTweetFieldWidget tweetText;

	public TwitterTweetScreen(Screen parent) {
		super(NarratorManager.EMPTY, parent);
	}

	public void tick() {
		if (this.tweetText != null) {
			this.tweetText.tick();
		}

		super.tick();
	}

	protected void init() {
		super.init();
		int i = this.width / 4;
		this.client.keyboard.setRepeatEvents(true);
		this.tweetText = new TwitterTweetFieldWidget(this.textRenderer, i, this.height / 4, i * 2, this.height / 2, NarratorManager.EMPTY);
		this.tweetText.setEditableColor(-1);
		this.tweetText.setMaxLength(CharacterUtil.MAX_TWEET_LENGTH);

		this.addDrawableChild(new ButtonWidget(i, (this.height / 4 + this.height / 2) + 10, i, 20, ScreenTexts.BACK, a -> this.onClose()));

		this.addDrawableChild(new ButtonWidget(i * 2, (this.height / 4 + this.height / 2) + 10, i, 20, new TranslatableText("tweet"), a -> {
			try {
				TwitterForMC.mcTwitter.updateStatus(this.tweetText.getText());
				this.accept(new TranslatableText("sent.tweet"));
			} catch (TwitterException e) {
				LOGGER.error("Error occurred while sending tweet", e);
				this.accept(new TranslatableText("failed.send.tweet", e.getErrorMessage()));
			}

			this.onClose();
		}));

		this.addDrawableChild(this.tweetText);
	}

	private void accept(Text msg) {
		if (this.parent instanceof DisplayableMessage) {
			((DisplayableMessage) this.parent).accept(msg);
		}
	}

	public void resize(MinecraftClient p_resize_1_, int p_resize_2_, int p_resize_3_) {
		String s = this.tweetText.getText();
		this.init(p_resize_1_, p_resize_2_, p_resize_3_);
		this.tweetText.setText(s);
	}

	public void removed() {
		super.removed();
		this.client.keyboard.setRepeatEvents(false);
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.onClose();
		}

		return this.tweetText.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || this.tweetText.isActive() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	public void render(MatrixStack matrices, int p_render_1_, int p_render_2_, float p_render_3_) {
		if (this.parent != null) {
			this.parent.render(matrices, -1, -1, p_render_3_);
		}
		this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
		RenderSystem.disableBlend();
		this.tweetText.render(matrices, p_render_1_, p_render_2_, p_render_3_);
		super.render(matrices, p_render_1_, p_render_2_, p_render_3_);
	}
}
