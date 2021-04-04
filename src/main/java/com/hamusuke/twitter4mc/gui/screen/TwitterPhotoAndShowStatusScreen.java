package com.hamusuke.twitter4mc.gui.screen;

import java.awt.Dimension;
import java.io.InputStream;
import java.util.List;

import com.hamusuke.twitter4mc.TwitterForMinecraft;
import com.hamusuke.twitter4mc.photomedia.ITwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.NarratorManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

public class TwitterPhotoAndShowStatusScreen extends ParentalScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final TweetSummary summary;
	private final int indexoffset;

	public TwitterPhotoAndShowStatusScreen(TwitterScreen ts, TweetSummary summary, int index) {
		super(NarratorManager.EMPTY, ts);
		this.summary = summary;
		this.indexoffset = index;
	}

	protected void init() {
		super.init();
	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.parent.render(-1, -1, p_render_3_);
		this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();

		try {
			List<ITwitterPhotoMedia> p = this.summary.getPhotoMedias();
			ITwitterPhotoMedia imedia = p.get(this.indexoffset);
			InputStream data = imedia.getData();
			if (data != null) {
				Dimension d = TwitterUtil.getScaledDimensionMinRatio(new Dimension(imedia.getWidth(), imedia.getHeight()), new Dimension(this.width, this.height));
				TwitterForMinecraft.getTextureManager().bindTexture(data);
				DrawableHelper.blit(0, 0, 0.0F, 0.0F, d.width, d.height, d.width, d.height);
			}
		} catch (Throwable t) {
			LOGGER.error("Can't rendering: {}", t.getLocalizedMessage());
		}

		RenderSystem.disableBlend();

		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
}
