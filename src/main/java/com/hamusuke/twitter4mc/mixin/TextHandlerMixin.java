package com.hamusuke.twitter4mc.mixin;

import com.hamusuke.twitter4mc.font.TweetTextVisitFactory;
import com.hamusuke.twitter4mc.invoker.TextHandlerInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(TextHandler.class)
public class TextHandlerMixin implements TextHandlerInvoker {
    @Shadow
    @Final
    TextHandler.WidthRetriever widthRetriever;

    public float getWidthWithEmoji(Text text) {
        return this.getWidthWithEmoji(text.asString(), text.getStyle());
    }

    public float getWidthWithEmoji(@Nullable String text) {
        return this.getWidthWithEmoji(text, Style.EMPTY);
    }

    private float getWidthWithEmoji(@Nullable String text, Style style) {
        if (text == null) {
            return 0.0F;
        } else {
            MutableFloat mutableFloat = new MutableFloat();
            TweetTextVisitFactory.visitCharacterOrEmoji(text, style, (unused, style2, codePoint) -> {
                mutableFloat.add(this.widthRetriever.getWidth(codePoint, style2));
                return true;
            }, emoji -> {
                mutableFloat.add(emoji.getEmojiWidth());
                return true;
            });
            return mutableFloat.floatValue();
        }
    }
}
