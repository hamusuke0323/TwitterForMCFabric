package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.emoji.Emoji;
import net.minecraft.text.CharacterVisitor;

public interface CharacterAndEmojiVisitor extends CharacterVisitor {
    default boolean acceptEmoji(Emoji emoji) {
        return true;
    }
}
