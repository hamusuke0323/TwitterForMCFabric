package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.emoji.Emoji;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.CharacterVisitor;

@Environment(EnvType.CLIENT)
public interface CharacterAndEmojiVisitor extends CharacterVisitor {
    default boolean acceptEmoji(Emoji emoji) {
        return true;
    }
}
