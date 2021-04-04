package com.hamusuke.twitter4mc.gui.toasts;

import java.io.InputStream;
import java.util.List;

import com.hamusuke.twitter4mc.TwitterForMC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TwitterNotificationToast extends InputStreamToast implements IClickableToast {
	private final String title;
	@Nullable
	private final String subtitle;

	public TwitterNotificationToast(@Nullable InputStream image, String title, @Nullable String subtitle) {
		super(image);
		this.title = title;
		this.subtitle = subtitle;
	}

	public Visibility draw(ToastManager toastGui, long delta) {
		toastGui.getGame().getTextureManager().bindTexture(TOASTS_TEX);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		toastGui.blit(0, 0, 0, 0, 160, 32);

		if (this.subtitle == null) {
			toastGui.getGame().textRenderer.draw(this.title, 30.0F, 12.0F, -1);
		} else {
			List<String> list = toastGui.getGame().textRenderer.wrapStringToWidthAsList(this.subtitle, 125);
			if (list.size() == 1) {
				toastGui.getGame().textRenderer.draw(this.title, 30.0F, 7.0F, 16777215);
				toastGui.getGame().textRenderer.draw(this.subtitle, 30.0F, 18.0F, 16777215);
			} else {
				int j = 1500;
				float f = 300.0F;
				if (delta < 1500L) {
					int k = MathHelper.floor(MathHelper.clamp((float) (1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
					toastGui.getGame().textRenderer.draw(this.title, 30.0F, 7.0F, 16777215 | k);
					toastGui.getGame().textRenderer.draw(this.subtitle, 30.0F, 18.0F, 16777215 | k);
				} else {
					int i1 = MathHelper.floor(MathHelper.clamp((float) (delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
					int l = 16 - list.size() * 9 / 2;

					for (String s : list) {
						toastGui.getGame().textRenderer.draw(s, 30.0F, (float) l, 16777215 | i1);
						l += 9;
					}
				}
			}
		}

		if (this.image != null) {
			try {
				RenderSystem.pushMatrix();
				RenderSystem.enableBlend();
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.translatef(8.0F, 8.0F, 0.0F);
				TwitterForMC.getTextureManager().bindTexture(this.image);
				DrawableHelper.blit(0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
				RenderSystem.popMatrix();
			} catch (Throwable t) {
				return Toast.Visibility.HIDE;
			}
		}

		return delta < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
		//return IToast.Visibility.SHOW;
	}

	public void mouseClicked(int toastX, int toastY, double x, double y, int button) {
		LogManager.getLogger().info("x: {}, y: {}", toastX, toastY);
	}
}
