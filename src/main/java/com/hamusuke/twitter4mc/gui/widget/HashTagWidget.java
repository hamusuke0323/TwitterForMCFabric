package com.hamusuke.twitter4mc.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

//TODO
@Environment(EnvType.CLIENT)
public class HashTagWidget extends ClickableWidget {
    public HashTagWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
