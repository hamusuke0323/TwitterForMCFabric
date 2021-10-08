package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.font.TweetTextVisitFactory;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public interface OrderedTweetText extends OrderedText {
    static OrderedText styledForwardsVisitedString(String string, Style style) {
        return string.isEmpty() ? EMPTY : (visitor) -> {
            if (visitor instanceof CharacterAndEmojiVisitor characterAndEmojiVisitor) {
                return TweetTextVisitFactory.visitForwardsCharacterOrEmoji(string, style, characterAndEmojiVisitor);
            } else {
                return TextVisitFactory.visitForwards(string, style, visitor);
            }
        };
    }

    static OrderedText styledBackwardsVisitedString(String string, Style style, Int2IntFunction codePointMapper) {
        return string.isEmpty() ? EMPTY : (visitor) -> {
            if (visitor instanceof CharacterAndEmojiVisitor characterAndEmojiVisitor) {
                return TweetTextVisitFactory.visitBackwardsCharacterOrEmoji(string, style, characterAndEmojiVisitor);
            } else {
                return TextVisitFactory.visitBackwards(string, style, OrderedText.map(visitor, codePointMapper));
            }
        };
    }
}
