package com.hamusuke.twitter4mc.tweet.user;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import twitter4j.User;

import java.io.InputStream;
import java.util.List;

@Environment(EnvType.CLIENT)
public class UserSummary {
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
    private final List<TweetSummary> userTimeline;

    public UserSummary(User user) {
        this.user = user;
        this.id = this.user.getId();
        this.name = this.user.getName();
        this.screenName = this.user.getScreenName();
        this.description = this.user.getDescription();
        this.statusesCount = this.user.getStatusesCount();
        this.icon = TwitterUtil.getInputStream(this.user.get400x400ProfileImageURLHttps());
        this.header = TwitterUtil.getInputStream(this.user.getProfileBanner1500x500URL());

        this.userTimeline = Lists.newArrayList();
        if (TwitterForMC.mctwitter != null) {
            try {
                TwitterForMC.mctwitter.getUserTimeline(this.user.getId()).forEach(status -> {
                    this.userTimeline.add(new TweetSummary(status));
                });
            } catch (Exception e) {
            }
        }
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

    public List<TweetSummary> getUserTimeline() {
        return this.userTimeline;
    }
}
