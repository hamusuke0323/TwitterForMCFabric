package com.hamusuke.twitter4mc.gui.toasts;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.invoker.TextRendererInvoker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TwitterNotificationToast extends InputStreamToast implements ClickableToast {
	private final String title;
	@Nullable
	private final String subtitle;

	public TwitterNotificationToast(@Nullable InputStream image, String title, @Nullable String subtitle) {
		super(image);
		this.title = title;
		this.subtitle = subtitle;
	}

	static TextRendererInvoker getInvoker(TextRenderer textRenderer) {
		return (TextRendererInvoker) textRenderer;
	}

	public void mouseClicked(int toastX, int toastY, double x, double y, int button) {
		LogManager.getLogger().info("x: {}, y: {}", toastX, toastY);
	}

	public Visibility draw(MatrixStack matrices, ToastManager toastGui, long delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		toastGui.drawTexture(matrices, 0, 0, 0, 0, 160, 32);

		if (this.subtitle == null) {
			getInvoker(toastGui.getGame().textRenderer).drawWithEmoji(matrices, this.title, 30.0F, 12.0F, -1);
		} else {
			List<StringVisitable> list = toastGui.getGame().textRenderer.getTextHandler().wrapLines(this.subtitle, 125, Style.EMPTY);
			if (list.size() == 1) {
				getInvoker(toastGui.getGame().textRenderer).drawWithEmoji(matrices, this.title, 30.0F, 7.0F, 16777215);
				getInvoker(toastGui.getGame().textRenderer).drawWithEmoji(matrices, this.subtitle, 30.0F, 18.0F, 16777215);
			} else {
				if (delta < 1500L) {
					int k = MathHelper.floor(MathHelper.clamp((float) (1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
					getInvoker(toastGui.getGame().textRenderer).drawWithEmoji(matrices, this.title, 30.0F, 7.0F, 16777215 | k);
					getInvoker(toastGui.getGame().textRenderer).drawWithEmoji(matrices, this.subtitle, 30.0F, 18.0F, 16777215 | k);
				} else {
					int i1 = MathHelper.floor(MathHelper.clamp((float) (delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
					int l = 16 - list.size() * 9 / 2;

					for (StringVisitable s : list) {
						getInvoker(toastGui.getGame().textRenderer).drawWithEmoji(matrices, s.getString(), 30.0F, (float) l, 16777215 | i1);
						l += 9;
					}
				}
			}
		}

		if (this.image != null) {
			matrices.push();
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			matrices.translate(8.0D, 8.0D, 0.0D);
			TwitterForMC.getTextureManager().bindTexture(this.image);
			DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
			matrices.pop();
		}

		return delta < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
	}
}
