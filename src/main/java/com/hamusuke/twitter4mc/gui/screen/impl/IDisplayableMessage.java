package com.hamusuke.twitter4mc.gui.screen.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IDisplayableMessage {
    void accept(String msg);
}
