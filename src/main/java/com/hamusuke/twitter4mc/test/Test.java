package com.hamusuke.twitter4mc.test;

import com.hamusuke.twitter4mc.emoji.Fitzpatrick;
import com.hamusuke.twitter4mc.tweet.ReplyTweet;
import com.hamusuke.twitter4mc.utils.ReplyObject;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Environment(EnvType.CLIENT)
final class Test {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) throws TwitterException {
		//oAuth2Authorization.setOAuth2Token(new OAuth2Token("", "AAAAAAAAAAAAAAAAAAAAAJu5IwEAAAAA%2FDD36S0uJpUQkw%2B3rlQ0%2BAJ%2BLC8%3DSUuGmOIv9hT37yvpAwSro8AEteeAyww4HSleFGG8yaXSNXUzV5"));
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer("lMaVpz8i1WP9A7xlt5BTzqWDr", "awYsyhxVXwoEQZpj7s8E40Z1kUrfQCl4KfiyqWEdpCnO7OBorK");
		twitter.setOAuthAccessToken(new AccessToken("981811509859794945-lhU6eHQdQ0fZgbNsOCe7wjO9ZyUfeVl", "Kypuf0Rp5jwWG5jBchMZZEmtCUSIvsuntdfVPwURHxJ5f"));

		ReplyObject replyObject = TwitterUtil.getReplies(twitter, 1410498680340172802L);
		if (replyObject != null) {
			LOGGER.info("result count: {}", replyObject.getResultCount());

			for (ReplyTweet replyTweet : replyObject.getReplyTweets()) {
				LOGGER.info("text: {}", replyTweet.getText());
			}
		}
	}

	private static String emojiToHex(String emoji) {
		char[] chars = emoji.toCharArray();
		StringBuilder res = new StringBuilder();
		for (char c : chars) {
			res.append(Integer.toHexString(c));
			res.append(chars[chars.length - 1] == c ? "" : ",");
		}
		return res.toString();
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
