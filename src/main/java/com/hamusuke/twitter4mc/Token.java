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
import java.util.Objects;

@Environment(EnvType.CLIENT)
final class Token implements Serializable {
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

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Token token = (Token) o;
        return autoLogin == token.autoLogin && Objects.equals(consumer, token.consumer) && Objects.equals(consumerS, token.consumerS) && Objects.equals(access, token.access) && Objects.equals(accessS, token.accessS);
    }

    public int hashCode() {
        return Objects.hash(consumer, consumerS, access, accessS, autoLogin);
    }

    @Environment(EnvType.CLIENT)
    private static final class Crypto {
        private static final String IV1 = "Ehg8489YE(83ghUheHUGherhgrepouwg89y('T('egh73BGbggUggGGgE8tg78fe";
        private static final String SECRET1 = "Af8Lqen#t7sniaih";
        private static final String IV2 = "AUGFH7(Fy4hjHGeugggUg37gfKkgii34yguhhg3478Giu4ghTG4hSIgh478uhks(";
        private static final String SECRET2 = "IfnU8feeu8Hfg48i";
        private static final String IV3 = "HF8439tKggk4jt4OJSg49yG4oghHLIO894jlG94y095OHGJ4yy0j4)YHh40$(7oo";
        private static final String SECRET3 = "(TfHUEh7ehnbjUFe";

        private static String encrypt(String text) {
            return new String(Base64.encodeBase64(enDoFinal3(Base64.encodeBase64(text.getBytes(StandardCharsets.UTF_8)))));
        }

        private static String decrypt(String text) {
            return new String(Base64.decodeBase64(deDoFinal3(Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8)))));
        }

        private static byte[] enDoFinal3(byte[] b) {
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

        private static byte[] deDoFinal3(byte[] b) {
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
