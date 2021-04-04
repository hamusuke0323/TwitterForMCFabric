package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.TwitterForMC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.TextFieldWidget;
import twitter4j.TwitterException;
import twitter4j.util.CharacterUtil;

//TODO Allow line breaks
@Environment(EnvType.CLIENT)
public class TwitterTweetScreen extends ParentalScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private TextFieldWidget tweettext;

	protected TwitterTweetScreen(TwitterScreen ts) {
		super(NarratorManager.EMPTY, ts);
	}

	protected void init() {
		super.init();
		int i = this.width / 4;
		this.minecraft.keyboard.enableRepeatEvents(true);
		this.tweettext = new TextFieldWidget(this.font, i, this.height / 4, i * 2, this.height / 2, "");
		this.tweettext.setEditableColor(-1);
		this.tweettext.setMaxLength(CharacterUtil.MAX_TWEET_LENGTH);

		this.addButton(new ButtonWidget(i, (this.height / 4 + this.height / 2) + 10, i, 20, I18n.translate("gui.back"), (a) -> {
			this.onClose();
		}));

		this.addButton(new ButtonWidget(i * 2, (this.height / 4 + this.height / 2) + 10, i, 20, I18n.translate("tweet"), (a) -> {
			try {
				TwitterForMC.mctwitter.updateStatus(this.tweettext.getText());
				((TwitterScreen) this.parent).accept(I18n.translate("sent.tweet"));
			} catch (TwitterException e) {
				LOGGER.error("Error occurred while sending tweet: {}", e);
				((TwitterScreen) this.parent).accept(I18n.translate("failed.send.tweet") + e.getErrorMessage());
			}

			this.onClose();
		}));

		this.children.add(this.tweettext);
	}

	public void resize(MinecraftClient p_resize_1_, int p_resize_2_, int p_resize_3_) {
		String s = this.tweettext.getText();
		this.init(p_resize_1_, p_resize_2_, p_resize_3_);
		this.tweettext.setText(s);
	}

	public void removed() {
		super.removed();
		this.minecraft.keyboard.enableRepeatEvents(false);
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.onClose();
		}

		return this.tweettext.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || this.tweettext.isActive() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.parent.render(-1, -1, p_render_3_);
		this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		RenderSystem.disableBlend();
		this.tweettext.render(p_render_1_, p_render_2_, p_render_3_);
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
}
