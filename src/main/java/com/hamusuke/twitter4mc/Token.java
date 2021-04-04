package com.hamusuke.twitter4mc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.codec.binary.Base64;
import twitter4j.auth.AccessToken;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
public final class Token implements Serializable {
    private static final long serialVersionUID = 7308358373767907036L;

    private final String consumer;
    private final String consumerS;
    private final String access;
    private final String accessS;
    private final boolean autoLogin;

    public Token(String consumer, String consumerS, AccessToken token, boolean autoLogin) {
        this.consumer = Crypto.encrypt(consumer);
        this.consumerS = Crypto.encrypt(consumerS);
        this.access = Crypto.encrypt(token.getToken());
        this.accessS = Crypto.encrypt(token.getTokenSecret());
        this.autoLogin = autoLogin;
    }

    public String getConsumer() {
        return Crypto.decrypt(this.consumer);
    }

    public String getConsumerSecret() {
        return Crypto.decrypt(this.consumerS);
    }

    public String getAccessToken() {
        return Crypto.decrypt(this.access);
    }

    public String getAccessTokenSecret() {
        return Crypto.decrypt(this.accessS);
    }

    public boolean autoLogin() {
        return this.autoLogin;
    }

    @Environment(EnvType.CLIENT)
    static final class Crypto {
        private static final String IV1 = "IvParameter1";
        private static final String SECRET1 = "Secret Key1";
        private static final String IV2 = "IvParameter2";
        private static final String SECRET2 = "Secret Key2";
        private static final String IV3 = "IvParameter3";
        private static final String SECRET3 = "Secret Key3";

        public static String encrypt(String text) {
            return new String(Base64.encodeBase64(enDoFinal3(Base64.encodeBase64(text.getBytes(StandardCharsets.UTF_8)))));
        }

        public static String decrypt(String text) {
            return new String(Base64.decodeBase64(deDoFinal3(Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8)))));
        }

        static byte[] enDoFinal3(byte[] b) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET1.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(IV1.getBytes(StandardCharsets.UTF_8), 0, 16)));
                byte[] bytes = cipher.doFinal(b);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET2.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(IV2.getBytes(StandardCharsets.UTF_8), 0, 16)));
                bytes = cipher.doFinal(bytes);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET3.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(IV3.getBytes(StandardCharsets.UTF_8), 0, 16)));
                return cipher.doFinal(bytes);
            } catch (Exception e) {
                throw new UnsupportedOperationException(e);
            }
        }

        static byte[] deDoFinal3(byte[] b) {
            try {
                byte[] bytes = Arrays.copyOfRange(b, 0, b.length);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET3.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(IV3.getBytes(StandardCharsets.UTF_8), 0, 16)));
                bytes = cipher.doFinal(bytes);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET2.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(IV2.getBytes(StandardCharsets.UTF_8), 0, 16)));
                bytes = cipher.doFinal(bytes);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET1.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(IV1.getBytes(StandardCharsets.UTF_8), 0, 16)));
                return cipher.doFinal(bytes);
            } catch (Exception e) {
                throw new UnsupportedOperationException(e);
            }
        }
    }
}
