package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.emoji.Emoji;

public interface EmojiVisitor {
    boolean accept(Emoji emoji);
}
