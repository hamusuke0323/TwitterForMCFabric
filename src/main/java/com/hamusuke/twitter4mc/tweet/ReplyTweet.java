package com.hamusuke.twitter4mc.tweet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ReplyTweet {
    private final long conversationId;
    private final long tweetId;
    private final long inReplyToUserId;
    private final int retweetCount;
    private final int replyCount;
    private final int likeCount;
    private final int quoteCount;
    private final long repliedToTweetId;
    private final String text;

    public ReplyTweet(JSONObject jsonObject) {
        this.conversationId = jsonObject.optLong("conversation_id", -1L);
        this.tweetId = jsonObject.optLong("id", -1L);
        this.inReplyToUserId = jsonObject.optLong("in_reply_to_user_id", -1L);
        JSONObject publicMetrics = jsonObject.optJSONObject("public_metrics");
        this.retweetCount = publicMetrics != null ? publicMetrics.optInt("retweet_count", -1) : -1;
        this.replyCount = publicMetrics != null ? publicMetrics.optInt("reply_count", -1) : -1;
        this.likeCount = publicMetrics != null ? publicMetrics.optInt("like_count", -1) : -1;
        this.quoteCount = publicMetrics != null ? publicMetrics.optInt("quote_count", -1) : -1;
        JSONArray referencedTweets = jsonObject.optJSONArray("referenced_tweets");
        JSONObject referencedTweet = referencedTweets != null ? referencedTweets.optJSONObject(0) : null;
        this.repliedToTweetId = referencedTweet != null ? referencedTweet.optLong("id", -1L) : -1L;
        this.text = jsonObject.optString("text");
    }

    public long getConversationId() {
        return this.conversationId;
    }

    public long getTweetId() {
        return this.tweetId;
    }

    public long getInReplyToUserId() {
        return this.inReplyToUserId;
    }

    public int getRetweetCount() {
        return this.retweetCount;
    }

    public int getReplyCount() {
        return this.replyCount;
    }

    public int getLikeCount() {
        return this.likeCount;
    }

    public int getQuoteCount() {
        return this.quoteCount;
    }

    public long getRepliedToTweetId() {
        return this.repliedToTweetId;
    }

    public String getText() {
        return this.text;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ReplyTweet that = (ReplyTweet) obj;
        return this.tweetId == that.tweetId;
    }

    public int hashCode() {
        return Objects.hash(this.tweetId);
    }

    public String toString() {
        return "ReplyTweet{conversationId=" + this.conversationId + ", tweetId=" + this.tweetId + ", inReplyToUserId=" + this.inReplyToUserId + ", retweetCount=" + this.retweetCount + ", replyCount=" + this.replyCount + ", likeCount=" + this.likeCount + ", quoteCount=" + this.quoteCount + ", repliedToTweetId=" + this.repliedToTweetId + ", text='" + this.text + "'}";
    }
}
