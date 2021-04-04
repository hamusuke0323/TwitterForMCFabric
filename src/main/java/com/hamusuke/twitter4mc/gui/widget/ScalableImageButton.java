package com.hamusuke.twitter4mc.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ScalableImageButton extends ButtonWidget {
    protected final Identifier texture;
    protected final int u;
    protected final int v;
    protected final int hoveredVOffset;
    protected final int textureWidth;
    protected final int textureHeight;
    protected final float scale;
    protected final int renderWidth;
    protected final int renderHeight;

    public ScalableImageButton(int x, int y, int widgetwidth, int widgetheight, int renderWidth, int renderHeight, float scale, int u, int v, int hoveredVOffset, Identifier texture, ButtonWidget.PressAction pressAction) {
        this(x, y, widgetwidth, widgetheight, renderWidth, renderHeight, scale, u, v, hoveredVOffset, texture, 256, 256, pressAction);
    }

    public ScalableImageButton(int x, int y, int wwidth, int wheight, int renderWidth, int renderHeight, float scale, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction) {
        this(x, y, wwidth, wheight, renderWidth, renderHeight, scale, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, "");
    }

    public ScalableImageButton(int x, int y, int wwidth, int wheight, int renderWidth, int renderHeight, float scale, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction, String text) {
        super(x, y, wwidth, wheight, text, pressAction);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;
        this.hoveredVOffset = hoveredVOffset;
        this.texture = texture;
        this.scale = scale;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void renderButton(int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getTextureManager().bindTexture(this.texture);
        RenderSystem.pushMatrix();
        RenderSystem.disableDepthTest();
        int i = this.v;
        if (this.isHovered()) {
            i += this.hoveredVOffset;
        }

        RenderSystem.translatef(this.x, this.y, 0.0F);
        RenderSystem.scalef(this.scale, this.scale, this.scale);
        blit(0, 0, (float) this.u, (float) i, this.renderWidth, this.renderHeight, this.textureWidth, this.textureHeight);
        RenderSystem.enableDepthTest();
        RenderSystem.popMatrix();
    }
}
