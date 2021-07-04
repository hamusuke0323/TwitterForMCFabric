package com.hamusuke.twitter4mc.tweet;

import com.google.common.collect.*;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.utils.TweetSummaryCreator;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;
import twitter4j.User;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class UserSummary {
    private static final Logger LOGGER = LogManager.getLogger();
    private final User user;
    private final long id;
    private final String name;
    private final String screenName;
    private final String description;
    private final int statusesCount;
    @Nullable
    private final InputStream icon;
    @Nullable
    private final InputStream header;
    private final TreeSet<TweetSummary> userTimeline = Sets.newTreeSet(Collections.reverseOrder());
    private final AtomicBoolean isGettingUserTimeline = new AtomicBoolean();
    private final AtomicBoolean isAlreadyGotUserTimeline = new AtomicBoolean();
    private final boolean isProtected;
    private final boolean isVerified;

    public UserSummary(User user) {
        this.user = user;
        this.id = this.user.getId();
        this.name = this.user.getName();
        this.screenName = this.user.getScreenName();
        this.description = this.user.getDescription();
        this.statusesCount = this.user.getStatusesCount();
        this.icon = TwitterUtil.getInputStream(this.user.get400x400ProfileImageURLHttps());
        this.header = TwitterUtil.getInputStream(this.user.getProfileBanner1500x500URL());
        this.isProtected = this.user.isProtected();
        this.isVerified = this.user.isVerified();
    }

    public void startGettingUserTimeline(Runnable onSend) {
        if (TwitterForMC.mctwitter != null && !this.isGettingUserTimeline()) {
            this.isGettingUserTimeline.set(true);
            try {
                List<Status> statuses = TwitterForMC.mctwitter.getUserTimeline(this.user.getId());
                Collections.reverse(statuses);
                new TweetSummaryCreator(statuses, (tweetSummary) -> {
                    if (this.userTimeline.add(tweetSummary)) {
                        onSend.run();
                    }
                }, () -> {
                    this.isGettingUserTimeline.set(false);
                    this.isAlreadyGotUserTimeline.set(true);
                }).createAll();
            } catch (Throwable e) {
                LOGGER.error("Error occurred while getting user timeline", e);
                this.isGettingUserTimeline.set(false);
                this.isAlreadyGotUserTimeline.set(false);
                this.userTimeline.clear();
            }
        }
    }

    public boolean isGettingUserTimeline() {
        return this.isGettingUserTimeline.get();
    }

    public boolean isAlreadyGotUserTimeline() {
        return this.isAlreadyGotUserTimeline.get();
    }

    public User getUser() {
        return this.user;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getScreenName() {
        return "@" + this.screenName;
    }

    public String getDescription() {
        return this.description;
    }

    public int getStatusesCount() {
        return this.statusesCount;
    }

    @Nullable
    public InputStream getIcon() {
        return this.icon;
    }

    @Nullable
    public InputStream getHeader() {
        return this.header;
    }

    public TreeSet<TweetSummary> getUserTimeline() {
        return this.userTimeline;
    }

    public boolean isProtected() {
        return this.isProtected;
    }

    public boolean isVerified() {
        return this.isVerified;
    }
}
