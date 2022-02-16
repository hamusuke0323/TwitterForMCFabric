package com.hamusuke.twitter4mc.mixin;

import com.hamusuke.twitter4mc.invoker.TextHandlerInvoker;
import com.hamusuke.twitter4mc.text.TweetTextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(TextHandler.class)
public class TextHandlerMixin implements TextHandlerInvoker {
    @Shadow
    @Final
    TextHandler.WidthRetriever widthRetriever;

    @Override
    public float getWidthWithEmoji(OrderedText text) {
        return TweetTextUtil.getWidthWithEmoji(text, this.widthRetriever);
    }
}
