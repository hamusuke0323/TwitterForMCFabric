package com.hamusuke.twitter4mc.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import twitter4j.auth.AccessToken;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class NewToken implements Serializable {
    @Serial
    private static final long serialVersionUID = 1446196596512638593L;

    private final String consumer;
    private final String consumerS;
    private final String access;
    private final String accessS;
    private final boolean autoLogin;

    public NewToken(String consumer, String consumerS, AccessToken token, boolean autoLogin) {
        this(consumer, consumerS, token.getToken(), token.getTokenSecret(), autoLogin);
    }

    public NewToken(String consumer, String consumerS, String access, String accessS, boolean autoLogin) {
        this.consumer = consumer;
        this.consumerS = consumerS;
        this.access = access;
        this.accessS = accessS;
        this.autoLogin = autoLogin;
    }

    public String getConsumer() {
        return this.consumer;
    }

    public String getConsumerSecret() {
        return this.consumerS;
    }

    public String getAccessToken() {
        return this.access;
    }

    public String getAccessTokenSecret() {
        return this.accessS;
    }

    public boolean autoLogin() {
        return this.autoLogin;
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        NewToken token = (NewToken) o;
        return this.autoLogin == token.autoLogin && Objects.equals(this.consumer, token.consumer) && Objects.equals(this.consumerS, token.consumerS) && Objects.equals(this.access, token.access) && Objects.equals(this.accessS, token.accessS);
    }

    public int hashCode() {
        return Objects.hash(this.consumer, this.consumerS, this.access, this.accessS, this.autoLogin);
    }
}
