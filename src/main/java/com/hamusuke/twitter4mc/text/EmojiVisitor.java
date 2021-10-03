package com.hamusuke.twitter4mc.text;

import com.hamusuke.twitter4mc.emoji.Emoji;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface EmojiVisitor {
    boolean accept(Emoji emoji);
}
