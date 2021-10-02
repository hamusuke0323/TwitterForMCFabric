package com.hamusuke.twitter4mc.utils;

import com.hamusuke.twitter4mc.tweet.TweetSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import twitter4j.Status;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public class TweetSummaryCreator {
    private final TwitterThread twitterThread;

    public TweetSummaryCreator(Collection<Status> statuses, TweetSummarySender pusher, Runnable onFinishCreating) {
        this.twitterThread = new TwitterThread(() -> {
            statuses.forEach((status) -> pusher.send(new TweetSummary(status)));
            onFinishCreating.run();
        });
    }

    public void createAll() {
        if (!this.twitterThread.isAlive()) {
            this.twitterThread.start();
        }
    }

    @Environment(EnvType.CLIENT)
    public interface TweetSummarySender {
        void send(TweetSummary tweetSummary);
    }
}
