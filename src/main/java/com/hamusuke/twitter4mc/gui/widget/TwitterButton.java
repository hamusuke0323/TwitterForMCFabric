package com.hamusuke.twitter4mc.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TwitterButton extends ChangeableImageButton {
    private final int sizeX;
    private final int sizeY;

    public TwitterButton(int x, int y, int width, int height, int u, int v, int whenHovered, Identifier image, int sizeX, int sizeY, int renderSizeX, int renderSizeY, ButtonWidget.PressAction iPressable) {
        super(x, y, width, height, u, v, whenHovered, image, sizeX, sizeY, iPressable);
        this.sizeX = renderSizeX;
        this.sizeY = renderSizeY;
    }

    public void renderButton(MatrixStack matrices, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.getTextureManager().bindTexture(this.resourceLocation);
        matrices.push();
        RenderSystem.disableDepthTest();
        int i = this.yTexStart;

        if (this.isHovered()) {
            i += this.yDiffText;
        }

        matrices.translate(this.x, this.y, 0.0F);
        matrices.scale(0.625F, 0.625F, 0.625F);
        drawTexture(matrices, 0, 0, (float) this.xTexStart, (float) i, this.sizeX, this.sizeY, this.textureSizeX, this.textureSizeY);
        RenderSystem.enableDepthTest();
        matrices.pop();
    }
}
