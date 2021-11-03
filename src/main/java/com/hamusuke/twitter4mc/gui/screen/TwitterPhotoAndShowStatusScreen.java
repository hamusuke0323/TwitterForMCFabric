package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TwitterPhotoAndShowStatusScreen extends ParentalScreen {
	private final TweetSummary summary;
	private final int index;

	public TwitterPhotoAndShowStatusScreen(Screen parent, TweetSummary summary, int index) {
		super(NarratorManager.EMPTY, parent);
		this.summary = summary;
		this.index = index;
	}

	protected void init() {
		super.init();
	}

	public void render(MatrixStack matrices, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.parent.render(matrices, -1, -1, p_render_3_);
		this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();

		List<TwitterPhotoMedia> p = this.summary.getPhotoMedias();
		TwitterPhotoMedia media = p.get(this.index);
		if (media.readyToRender()) {
			Dimension d = TwitterUtil.wrapImageSizeToMin(new Dimension(media.getWidth(), media.getHeight()), new Dimension(this.width, this.height));
			TwitterForMC.getTextureManager().bindTexture(media.getData());
			DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, d.width, d.height, d.width, d.height);
		}

		RenderSystem.disableBlend();

		super.render(matrices, p_render_1_, p_render_2_, p_render_3_);
	}
}
