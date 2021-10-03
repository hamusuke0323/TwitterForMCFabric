package com.hamusuke.twitter4mc.invoker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface TextHandlerInvoker {
    float getWidthWithEmoji(Text text);

    float getWidthWithEmoji(@Nullable String text);
}
