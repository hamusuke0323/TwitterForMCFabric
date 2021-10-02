package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface DisplayableMessage {
    void accept(String msg);

    void renderMessage();
}
