package com.hamusuke.twitter4mc.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ChangeableImageButton extends ButtonWidget {
    protected Identifier resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected int yDiffText;
    protected int textureSizeX;
    protected int textureSizeY;

    public ChangeableImageButton(int x, int y, int width, int height, int u, int v, int whenHovered, Identifier image, String msg, ButtonWidget.PressAction iPressable) {
        this(x, y, width, height, u, v, whenHovered, image, 256, 256, msg, iPressable);
    }

    public ChangeableImageButton(int x, int y, int width, int height, int u, int v, int whenHovered, Identifier image, int sizex, int sizey, ButtonWidget.PressAction iPressable) {
        this(x, y, width, height, u, v, whenHovered, image, sizex, sizey, "", iPressable);
    }

    public ChangeableImageButton(int x, int y, int width, int height, int u, int v, int whenHovered, Identifier image, int sizex, int sizey, String msg, ButtonWidget.PressAction iPressable) {
        super(x, y, width, height, msg, iPressable);
        this.textureSizeX = sizex;
        this.textureSizeY = sizey;
        this.xTexStart = u;
        this.yTexStart = v;
        this.yDiffText = whenHovered;
        this.resourceLocation = image;
    }

    public void setPosition(int xIn, int yIn) {
        this.x = xIn;
        this.y = yIn;
    }

    public void setSize(int x, int y) {
        this.textureSizeX = x;
        this.textureSizeY = y;
    }

    public void setImage(Identifier image) {
        this.resourceLocation = image;
    }

    public void setWhenHovered(int i) {
        this.yDiffText = i;
    }

    public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.getTextureManager().bindTexture(this.resourceLocation);
        RenderSystem.disableDepthTest();
        int i = this.yTexStart;

        if (this.isHovered()) {
            i += this.yDiffText;
        }

        blit(this.x, this.y, (float) this.xTexStart, (float) i, this.width, this.height, this.textureSizeX, this.textureSizeY);
        RenderSystem.enableDepthTest();
    }
}
