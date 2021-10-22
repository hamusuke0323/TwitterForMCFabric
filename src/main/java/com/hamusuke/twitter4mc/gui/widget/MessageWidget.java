package com.hamusuke.twitter4mc.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MessageWidget extends ClickableWidget {
    private final Screen parent;

    public MessageWidget(Screen parent, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.parent = parent;
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<OrderedText> list = client.textRenderer.wrapLines(this.getMessage(), this.width);
        this.parent.renderOrderedTooltip(matrices, list, this.x, this.y);
    }

    public void onClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.parent.handleTextClick(client.textRenderer.getTextHandler().getStyleAt(this.getMessage(), MathHelper.ceil(mouseX) - this.x + 12));
    }

    public void setPosition(int x, int y, int width, int height) {
        if (x != this.x || y != this.y || width != this.width || height != this.height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    public boolean isNarratable() {
        return false;
    }
}
