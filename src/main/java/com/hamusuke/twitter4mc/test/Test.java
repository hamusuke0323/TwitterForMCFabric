package com.hamusuke.twitter4mc.test;

import com.hamusuke.twitter4mc.emoji.util.Fitzpatrick;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Test {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
	}

	private static String emojiToHex(String emoji) {
		char[] chars = emoji.toCharArray();
		String res = "";
		for(char c : chars) {
			res += Integer.toHexString(c);
			res += chars[chars.length - 1] == c ? "" : ",";
		}
		return res;
	}

	private static String splitEmojiHex(String text) {
		char[] chars = text.toCharArray();
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i + 1 != chars.length && Character.isHighSurrogate(c)) {
				res.append(Integer.toHexString(Character.toCodePoint(c, chars[i + 1])));
				i++;
				if (i + 1 != chars.length && (chars[i + 1] == 0x200d || (i + 2 != chars.length && Fitzpatrick.isFitzpatrick(Integer.toHexString(Character.toCodePoint(chars[i + 1], chars[i + 2])))))) {
					res.append("-");
				} else {
					res.append(",");
				}
			} else if (c == 0x200d) {
				res.append(Integer.toHexString(c)).append("-");
			} else {
				res.append(Integer.toHexString(c)).append(",");
			}
		}
		return res.length() < 2 ? "" : res.deleteCharAt(res.length() - 1).toString();
	}

	private static int emojiLength(String emojiHex) {
		if (emojiHex.isEmpty()) {
			return 0;
		}
		String[] strings = emojiHex.split("-");
		int res = 0;
		for (String s : strings) {
			res += Character.charCount(Integer.decode("0x" + s));
		}
		return res;
	}
}
