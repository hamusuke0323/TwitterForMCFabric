package com.hamusuke.twitter4mc.invoker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.OrderedText;

@Environment(EnvType.CLIENT)
public interface TextHandlerInvoker {
    float getWidthWithEmoji(OrderedText text);
}
