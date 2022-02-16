package com.hamusuke.twitter4mc.text;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.emoji.Emoji;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.BidiRun;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TweetTextUtil {
    public static float getWidthWithEmoji(OrderedText text, TextHandler.WidthRetriever widthRetriever) {
        MutableFloat mutableFloat = new MutableFloat();
        text.accept(new CharacterAndEmojiVisitor() {
            @Override
            public boolean accept(int index, Style style, int codePoint) {
                mutableFloat.add(widthRetriever.getWidth(codePoint, style));
                return true;
            }

            @Override
            public boolean acceptEmoji(Emoji emoji) {
                mutableFloat.add(emoji.getEmojiWidth());
                return true;
            }
        });
        return mutableFloat.floatValue();
    }

    public static OrderedText reorderIgnoreStyleChar(StringVisitable text, boolean rightToLeft) {
        TweetTextReorderingProcessor tweetTextReorderingProcessor = TweetTextReorderingProcessor.create(text, UCharacter::getMirror, TweetTextUtil::shapeArabic);
        Bidi bidi = new Bidi(tweetTextReorderingProcessor.getString(), rightToLeft ? Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT : Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        bidi.setReorderingMode(0);
        List<OrderedText> list = Lists.newArrayList();
        int i = bidi.countRuns();

        for (int j = 0; j < i; ++j) {
            BidiRun bidiRun = bidi.getVisualRun(j);
            list.addAll(tweetTextReorderingProcessor.process(bidiRun.getStart(), bidiRun.getLength(), bidiRun.isOddRun()));
        }

        return OrderedText.concat(list);
    }

    private static String shapeArabic(String string) {
        try {
            return new ArabicShaping(8).shape(string);
        } catch (Exception e) {
            return string;
        }
    }
}
