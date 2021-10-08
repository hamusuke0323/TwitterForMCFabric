package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.font.TweetTextVisitFactory;
import net.minecraft.text.*;
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

    public String asString() {
        return this.string;
    }

    public LiteralText copy() {
        return new LiteralText(this.string);
    }

    public OrderedText asOrderedText() {
        Language language = Language.getInstance();
        if (this.previousLanguage != language) {
            this.orderedText = language.reorder(this);
            this.previousLanguage = language;
        }

        return this.orderedText;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof TweetText tweetText)) {
            return false;
        } else {
            return this.string.equals(tweetText.asString()) && super.equals(object);
        }
    }

    public String toString() {
        return "TweetTextComponent{text='" + this.string + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
    }
}
