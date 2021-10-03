package com.hamusuke.twitter4mc.invoker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface TextHandlerInvoker {
    float getWidthWithEmoji(@Nullable String text);
}
