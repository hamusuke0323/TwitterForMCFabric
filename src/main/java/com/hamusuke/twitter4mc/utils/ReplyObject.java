package com.hamusuke.twitter4mc.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.tweet.ReplyTweet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ReplyObject {
    private final JSONObject jsonObject;
    private final long newestTweetId;
    private final long oldestTweetId;
    private final int resultCount;
    private final String nextToken;
    private final List<ReplyTweet> replyTweets = Lists.newArrayList();

    public ReplyObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        JSONObject meta = jsonObject.optJSONObject("meta");
        this.newestTweetId = meta != null ? meta.optLong("newest_id", -1L) : -1L;
        this.oldestTweetId = meta != null ? meta.optLong("oldest_id", -1L) : -1L;
        this.resultCount = meta != null ? meta.optInt("result_count", -1) : -1;
        this.nextToken = meta != null ? meta.optString("next_token") : "";

        JSONArray jsonArray = jsonObject.optJSONArray("data");
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject reply = jsonArray.optJSONObject(i);
                if (reply != null) {
                    this.replyTweets.add(new ReplyTweet(reply));
                }
            }
        }
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

    public long getNewestTweetId() {
        return this.newestTweetId;
    }

    public long getOldestTweetId() {
        return this.oldestTweetId;
    }

    public int getResultCount() {
        return this.resultCount;
    }

    public String getNextToken() {
        return this.nextToken;
    }

    public List<ReplyTweet> getReplyTweets() {
        return ImmutableList.copyOf(this.replyTweets);
    }

    public void removeOtherReplies(long tweetId) {
        this.replyTweets.removeIf((replyTweet) -> replyTweet.getRepliedToTweetId() != tweetId);
    }

    @Override
    public String toString() {
        return "ReplyObject{jsonObject=" + this.jsonObject + ", newestTweetId=" + this.newestTweetId + ", oldestTweetId=" + this.oldestTweetId + ", resultCount=" + this.resultCount + ", nextToken='" + this.nextToken + '\'' + ", replyTweets=" + this.replyTweets + '}';
    }
}
