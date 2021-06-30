package com.hamusuke.twitter4mc.gui.screen;

import java.awt.Dimension;
import java.io.InputStream;
import java.util.List;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

import com.mojang.blaze3d.systems.RenderSystem;

@Environment(EnvType.CLIENT)
public class TwitterPhotoAndShowStatusScreen extends ParentalScreen {
	private final TweetSummary summary;
	private final int indexOffset;

	public TwitterPhotoAndShowStatusScreen(Screen parent, TweetSummary summary, int index) {
		super(NarratorManager.EMPTY, parent);
		this.summary = summary;
		this.indexOffset = index;
	}

	protected void init() {
		super.init();
	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.parent.render(-1, -1, p_render_3_);
		this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();

		List<TwitterPhotoMedia> p = this.summary.getPhotoMedias();
		TwitterPhotoMedia media = p.get(this.indexOffset);
		InputStream data = media.getData();
		if (data != null) {
			Dimension d = TwitterUtil.getScaledDimensionMinRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(this.width, this.height));
			TwitterForMC.getTextureManager().bindTexture(data);
			DrawableHelper.blit(0, 0, 0.0F, 0.0F, d.width, d.height, d.width, d.height);
		}

		RenderSystem.disableBlend();

		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
}
