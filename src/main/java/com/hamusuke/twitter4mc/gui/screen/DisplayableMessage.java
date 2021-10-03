package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public interface DisplayableMessage {
    void accept(String msg);

    void renderMessage(MatrixStack matrices);
}
