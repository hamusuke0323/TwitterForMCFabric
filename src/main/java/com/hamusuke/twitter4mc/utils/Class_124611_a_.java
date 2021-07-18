package com.hamusuke.twitter4mc.utils;

import com.hamusuke.twitter4mc.Token;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
final class Class_124611_a_ {
    static final transient String field_511419_a_ = "OXlydkpQSUVvaVp0bHludzd3VzFCZDNWN2NKcEdXU1Npck5GVmQzWWNPazk1U1ZZR0o=";
    static final transient String field_209384_b_ = "psRffVpSMGHdnpX#";

    public static void func_082122_a_(NewToken p_func_082122_a__0_, File p_func_082122_a__1_) throws Exception {
        FileOutputStream field_103104_a_ = new FileOutputStream(p_func_082122_a__1_);
        final Cipher field_035983_d_ = Cipher.getInstance("AES/CBC/PKCS5Padding");
        field_035983_d_.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(field_209384_b_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_511419_a_.getBytes(StandardCharsets.UTF_8), 0, 16)));
        CipherOutputStream field_103106_c_ = new CipherOutputStream(field_103104_a_, field_035983_d_);
        field_103106_c_.write(SerializationUtils.serialize(p_func_082122_a__0_));
        field_103106_c_.flush();
        field_103106_c_.close();
    }

    public static NewToken func_082113_b_(File p_func_082113_b__0_) throws Exception {
        FileInputStream field_991104_a_ = new FileInputStream(p_func_082113_b__0_);
        final Cipher field_943063_ai_ = Cipher.getInstance("AES/CBC/PKCS5Padding");
        field_943063_ai_.init(Cipher.DECRYPT_MODE, new SecretKeySpec(field_209384_b_.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(Arrays.copyOfRange(field_511419_a_.getBytes(StandardCharsets.UTF_8), 0, 16)));
        CipherInputStream field_991105_b_ = new CipherInputStream(field_991104_a_, field_943063_ai_);
        ObjectInputStream field_991106_c_ = new ObjectInputStream(field_991105_b_);
        return (NewToken) field_991106_c_.readObject();
    }

    public static NewToken func_013341_f_(Token p_func_013341_f__0_, File p_func_013341_f__1_) throws Exception {
        NewToken field_125491_a_ = new NewToken(p_func_013341_f__0_.getConsumer(), p_func_013341_f__0_.getConsumerSecret(), p_func_013341_f__0_.getAccessToken(), p_func_013341_f__0_.getAccessTokenSecret(), p_func_013341_f__0_.autoLogin());
        func_082122_a_(field_125491_a_, p_func_013341_f__1_);
        return field_125491_a_;
    }
}
