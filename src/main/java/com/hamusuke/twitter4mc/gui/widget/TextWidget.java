package com.hamusuke.twitter4mc.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TextWidget extends AbstractButtonWidget {
    public TextWidget(int x, int y, int width, int height, Text msg) {
        super(x, y, width, height, msg.asFormattedString());
    }

    public void renderButton(int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.drawCenteredString(client.textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 10) / 2, 16777215);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }
}