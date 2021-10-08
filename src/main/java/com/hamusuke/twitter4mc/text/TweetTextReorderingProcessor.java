package com.hamusuke.twitter4mc.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.font.TweetTextVisitFactory;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Environment(EnvType.CLIENT)
public class TweetTextReorderingProcessor {
    private final String string;
    private final List<Style> styles;
    private final Int2IntFunction reverser;

    private TweetTextReorderingProcessor(String string, List<Style> styles, Int2IntFunction reverser) {
        this.string = string;
        this.styles = ImmutableList.copyOf(styles);
        this.reverser = reverser;
    }

    public String getString() {
        return this.string;
    }

    public List<OrderedText> process(int start, int length, boolean reverse) {
        if (length == 0) {
            return ImmutableList.of();
        } else {
            List<OrderedText> list = Lists.newArrayList();
            Style style = (Style) this.styles.get(start);
            int i = start;

            for (int j = 1; j < length; ++j) {
                int k = start + j;
                Style style2 = (Style) this.styles.get(k);
                if (!style2.equals(style)) {
                    String string = this.string.substring(i, k);
                    list.add(reverse ? OrderedText.styledBackwardsVisitedString(string, style, this.reverser) : OrderedText.styledForwardsVisitedString(string, style));
                    style = style2;
                    i = k;
                }
            }

            if (i < start + length) {
                String string2 = this.string.substring(i, start + length);
                list.add(reverse ? OrderedText.styledBackwardsVisitedString(string2, style, this.reverser) : OrderedText.styledForwardsVisitedString(string2, style));
            }

            return reverse ? Lists.reverse(list) : list;
        }
    }

    public static TweetTextReorderingProcessor create(StringVisitable visitable, Int2IntFunction reverser, UnaryOperator<String> shaper) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Style> list = Lists.newArrayList();
        visitable.visit((style, text) -> {
            TweetTextVisitFactory.visitCharacterOrEmoji(text, style, (charIndex, stylex, codePoint) -> {
                stringBuilder.appendCodePoint(codePoint);
                int i = Character.charCount(codePoint);

                for (int j = 0; j < i; ++j) {
                    list.add(stylex);
                }

                return true;
            }, emoji -> {

            });
            return Optional.empty();
        });
        return new TweetTextReorderingProcessor(shaper.apply(stringBuilder.toString()), list, reverser);
    }
}
