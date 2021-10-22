package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public interface DisplayableMessage {
    void accept(Text text);

    void renderMessage(MatrixStack matrices, int mouseX, int mouseY, float delta);
}
