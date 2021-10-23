package com.hamusuke.twitter4mc.gui.widget;

import com.hamusuke.twitter4mc.gui.screen.AbstractTwitterScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MessageWidget extends ClickableWidget {
    private final MinecraftClient client;
    private final Screen parent;
    @Nullable
    private List<OrderedText> messageLines;
    private int fade;

    public MessageWidget(Screen parent, MinecraftClient client, int x, int y, int width, int height, Text message) {
        super(x, y, width + 12, height + 12, message);
        this.parent = parent;
        this.client = client;
        this.fade = 100;
    }

    public void tick() {
        if (this.fade <= 0) {
            AbstractTwitterScreen.messageWidget = null;
        }

        this.fade--;
    }

    public void init(int width, int height) {
        this.messageLines = this.client.textRenderer.wrapLines(this.getMessage(), width / 2);
        int messageWidth = AbstractTwitterScreen.getMaxWidth(this.client.textRenderer, this.messageLines);
        this.setPosition((width - messageWidth) / 2, height - 20 - this.messageLines.size() * 9, messageWidth, this.messageLines.size() * 9);
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.messageLines != null) {
            this.parent.renderOrderedTooltip(matrices, this.messageLines, this.x - 12, this.y + 12);
        }
    }

    protected boolean clicked(double mouseX, double mouseY) {
        Style style = this.getStyleAt(mouseX, mouseY);
        return super.clicked(mouseX, mouseY) && style != null && style.getClickEvent() != null;
    }

    public void onClick(double mouseX, double mouseY) {
        this.parent.handleTextClick(this.getStyleAt(mouseX, mouseY));
    }

    @Nullable
    private Style getStyleAt(double mouseX, double mouseY) {
        if (this.messageLines != null) {
            int i = MathHelper.floor(mouseX) - this.x;
            int j = (MathHelper.floor(mouseY) - this.y) / 9;
            if (i >= 0 && j >= 0 && j < this.messageLines.size()) {
                return this.client.textRenderer.getTextHandler().getStyleAt(this.messageLines.get(j), i);
            }
        }

        return null;
    }

    public void setPosition(int x, int y, int width, int height) {
        width += 12;
        height += 12;

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
