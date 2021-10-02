package com.hamusuke.twitter4mc.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

//TODO
@Environment(EnvType.CLIENT)
public class HashTagWidget extends AbstractButtonWidget {
    public HashTagWidget(int x, int y, int width, int height, String message) {
        super(x, y, width, height, message);
    }
}
