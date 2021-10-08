package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.font.TweetTextVisitFactory;
import net.minecraft.text.OrderedText;

public interface OrderedTweetText extends OrderedText {
    static OrderedText emoji() {
        return visitor -> {

        };
    }
}
