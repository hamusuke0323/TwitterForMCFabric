package com.hamusuke.twitter4mc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import twitter4j.auth.AccessToken;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@Deprecated
@Environment(EnvType.CLIENT)
public final class Token implements Serializable {
    @Serial
    private static final long serialVersionUID = 7308358373767907036L;

    private final String consumer;
    private final String consumerS;
    private final String access;
    private final String accessS;
    private final boolean autoLogin;

    public Token(String consumer, String consumerS, AccessToken token, boolean autoLogin) {
        this.consumer = Class_013993_a_.func_003855_b_(consumer);
        this.consumerS = Class_013993_a_.func_003855_b_(consumerS);
        this.access = Class_013993_a_.func_003855_b_(token.getToken());
        this.accessS = Class_013993_a_.func_003855_b_(token.getTokenSecret());
        this.autoLogin = autoLogin;
    }

    public String getConsumer() {
        return Class_013993_a_.func_389353_cf_(this.consumer);
    }

    public String getConsumerSecret() {
        return Class_013993_a_.func_389353_cf_(this.consumerS);
    }

    public String getAccessToken() {
        return Class_013993_a_.func_389353_cf_(this.access);
    }

    public String getAccessTokenSecret() {
        return Class_013993_a_.func_389353_cf_(this.accessS);
    }

    public boolean autoLogin() {
        return this.autoLogin;
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Token token = (Token) o;
        return this.autoLogin == token.autoLogin && Objects.equals(this.consumer, token.consumer) && Objects.equals(this.consumerS, token.consumerS) && Objects.equals(this.access, token.access) && Objects.equals(this.accessS, token.accessS);
    }

    public int hashCode() {
        return Objects.hash(this.consumer, this.consumerS, this.access, this.accessS, this.autoLogin);
    }

    @Environment(EnvType.CLIENT)
    static final class Class_013993_a_ {
        private static final transient String field_511419_a_ = "Ehg8489YE(83ghUheHUGherhgrepouwg89y('T('egh73BGbggUggGGgE8tg78fe";
        private static final transient String field_209384_b_ = "Af8Lqen#t7sniaih";
        private static final transient String field_829484_c_ = "AUGFH7(Fy4hjHGeugggUg37gfKkgii34yguhhg3478Giu4ghTG4hSIgh478uhks(";
        private static final transient String field_689856_d_ = "IfnU8feeu8Hfg48i";
        private static final transient String field_390464_e_ = "HF8439tKggk4jt4OJSg49yG4oghHLIO894jlG94y095OHGJ4yy0j4)YHh40$(7oo";
        private static final transient String field_398473_f_ = "(TfHUEh7ehnbjUFe";

        private static String func_003855_b_(@NotNull final String p_i014544_) {
            Objects.requireNonNull(p_i014544_);
            return new String(Base64.encodeBase64(func_329733_b_(Base64.encodeBase64(p_i014544_.getBytes(StandardCharsets.UTF_8)))));
        }

        private static String func_389353_cf_(@NotNull final String p_i486444_) {
            Objects.requireNonNull(p_i486444_);
            return new String(Base64.decodeBase64(func_904683_cf_(Base64.decodeBase64(p_i486444_.getBytes(StandardCharsets.UTF_8)))));
        }

        private static byte[] func_329733_b_(final byte[] p_i932733_) {
            try {
                final Cipher field_035983_d_ = Cipher.getInstance("AES/CBC/PKCS5Padding");
                field_035983_d_.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(field_209384_b_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_511419_a_.getBytes(StandardCharsets.UTF_8), 0, 16)));
                byte[] field_230864_er_ = field_035983_d_.doFinal(p_i932733_);
                field_035983_d_.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(field_689856_d_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_829484_c_.getBytes(StandardCharsets.UTF_8), 0, 16)));
                field_230864_er_ = field_035983_d_.doFinal(field_230864_er_);
                field_035983_d_.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(field_398473_f_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_390464_e_.getBytes(StandardCharsets.UTF_8), 0, 16)));
                return field_035983_d_.doFinal(field_230864_er_);
            } catch (Exception var100) {
                throw new UnsupportedOperationException(var100);
            }
        }

        private static byte[] func_904683_cf_(final byte[] p_i982347_) {
            try {
                byte[] field_923875_pi_ = Arrays.copyOfRange(p_i982347_, 0, p_i982347_.length);
                final Cipher field_943063_ai_ = Cipher.getInstance("AES/CBC/PKCS5Padding");
                field_943063_ai_.init(Cipher.DECRYPT_MODE, new SecretKeySpec(field_398473_f_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_390464_e_.getBytes(StandardCharsets.UTF_8), 0, 16)));
                field_923875_pi_ = field_943063_ai_.doFinal(field_923875_pi_);
                field_943063_ai_.init(Cipher.DECRYPT_MODE, new SecretKeySpec(field_689856_d_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_829484_c_.getBytes(StandardCharsets.UTF_8), 0, 16)));
                field_923875_pi_ = field_943063_ai_.doFinal(field_923875_pi_);
                field_943063_ai_.init(Cipher.DECRYPT_MODE, new SecretKeySpec(field_209384_b_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_511419_a_.getBytes(StandardCharsets.UTF_8), 0, 16)));
                return field_943063_ai_.doFinal(field_923875_pi_);
            } catch (Exception var101) {
                throw new UnsupportedOperationException(var101);
            }
        }
    }
}
