package com.hamusuke.twitter4mc.font;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.emoji.Emoji;
import com.hamusuke.twitter4mc.emoji.Fitzpatrick;
import com.hamusuke.twitter4mc.text.CharacterAndEmojiVisitor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Style;

@Environment(EnvType.CLIENT)
public class TweetTextVisitFactory {
    public static boolean visitForwardsCharacterOrEmoji(String text, Style style, CharacterAndEmojiVisitor characterAndEmojiVisitor) {
        int i = text.length();

        StringBuilder emoji = new StringBuilder();
        for (int j = 0; j < i; ++j) {
            char c = text.charAt(j);

            if (TwitterForMC.getEmojiManager().isEmoji(Integer.toHexString(c))) {
                characterAndEmojiVisitor.acceptEmoji(getEmoji(Integer.toHexString(c)));
            } else if (Character.isHighSurrogate(c) && j + 1 < text.length() && Character.isLowSurrogate(text.charAt(j + 1))) {
                char low = text.charAt(j + 1);
                emoji.append(Integer.toHexString(Character.toCodePoint(c, low)));
                j++;
                if (j + 1 < text.length() && (text.charAt(j + 1) == 0x200d || (j + 2 < text.length() && Fitzpatrick.isFitzpatrick(Integer.toHexString(Character.toCodePoint(text.charAt(j + 1), text.charAt(j + 2))))))) {
                    emoji.append("-");
                } else {
                    characterAndEmojiVisitor.acceptEmoji(getEmoji(emoji.toString()));
                    emoji = new StringBuilder();
                }
            } else if (c == 0x200d) {
                emoji.append(Integer.toHexString(c)).append("-");
            } else if (!emoji.toString().isEmpty()) {
                characterAndEmojiVisitor.acceptEmoji(getEmoji(emoji.substring(0, emoji.length() - 2)));
                emoji = new StringBuilder();
            } else if (!characterAndEmojiVisitor.accept(j, style, c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean visitBackwardsCharacterOrEmoji(String text, Style style, CharacterAndEmojiVisitor characterAndEmojiVisitor) {
        int i = text.length();

        StringBuilder emoji = new StringBuilder();
        for (int j = i - 1; j >= 0; --j) {
            char c = text.charAt(j);

            if (TwitterForMC.getEmojiManager().isEmoji(Integer.toHexString(c))) {
                characterAndEmojiVisitor.acceptEmoji(getEmoji(Integer.toHexString(c)));
            } else if (Character.isHighSurrogate(c) && j + 1 < text.length() && Character.isLowSurrogate(text.charAt(j + 1))) {
                char low = text.charAt(j + 1);
                emoji.append(Integer.toHexString(Character.toCodePoint(c, low)));
                j++;
                if (j + 1 < text.length() && (text.charAt(j + 1) == 0x200d || (j + 2 < text.length() && Fitzpatrick.isFitzpatrick(Integer.toHexString(Character.toCodePoint(text.charAt(j + 1), text.charAt(j + 2))))))) {
                    emoji.append("-");
                } else {
                    characterAndEmojiVisitor.acceptEmoji(getEmoji(emoji.toString()));
                    emoji = new StringBuilder();
                }
            } else if (c == 0x200d) {
                emoji.append(Integer.toHexString(c)).append("-");
            } else if (!emoji.toString().isEmpty()) {
                characterAndEmojiVisitor.acceptEmoji(getEmoji(emoji.substring(0, emoji.length() - 2)));
                emoji = new StringBuilder();
            } else if (!characterAndEmojiVisitor.accept(j, style, c)) {
                return false;
            }
        }

        return true;
    }

    private static Emoji getEmoji(String hex) {
        return TwitterForMC.getEmojiManager().getEmoji(hex);
    }
}
