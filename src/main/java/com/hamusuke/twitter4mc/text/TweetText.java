package com.hamusuke.twitter4mc.text;

import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

public class TweetText extends BaseText {
    private final String string;
    private OrderedText orderedText;
    @Nullable
    private Language previousLanguage;

    public TweetText(String string) {
        this.string = string;
        this.orderedText = OrderedText.EMPTY;
    }

    @Override
    public String asString() {
        return this.string;
    }

    @Override
    public LiteralText copy() {
        return new LiteralText(this.string);
    }

    @Override
    public OrderedText asOrderedText() {
        Language language = Language.getInstance();
        if (this.previousLanguage != language) {
            this.orderedText = TweetTextUtil.reorderIgnoreStyleChar(this, language.isRightToLeft());
            this.previousLanguage = language;
        }

        return this.orderedText;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof TweetText tweetText)) {
            return false;
        } else {
            return this.string.equals(tweetText.asString()) && super.equals(object);
        }
    }

    @Override
    public String toString() {
        return "TweetTextComponent{text='" + this.string + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
    }
}
