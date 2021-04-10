package com.hamusuke.twitter4mc.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TwitterButton extends ChangeableImageButton {
    private final int sizex;
    private final int sizey;

    public TwitterButton(int x, int y, int width, int height, int u, int v, int whenHovered, Identifier image, int sizex, int sizey, int renderSizex, int renderSizey, ButtonWidget.PressAction iPressable) {
        super(x, y, width, height, u, v, whenHovered, image, sizex, sizey, iPressable);
        this.sizex = renderSizex;
        this.sizey = renderSizey;
    }

    public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.getTextureManager().bindTexture(this.resourceLocation);
        RenderSystem.pushMatrix();
        RenderSystem.disableDepthTest();
        int i = this.yTexStart;

        if (this.isHovered()) {
            i += this.yDiffText;
        }

        RenderSystem.translatef(this.x, this.y, 0.0F);
        RenderSystem.scalef(0.625F, 0.625F, 0.625F);
        blit(0, 0, (float) this.xTexStart, (float) i, this.sizex, this.sizey, this.textureSizeX, this.textureSizeY);
        RenderSystem.enableDepthTest();
        RenderSystem.popMatrix();
    }
}
