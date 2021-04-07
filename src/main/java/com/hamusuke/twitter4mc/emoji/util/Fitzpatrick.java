package com.hamusuke.twitter4mc.emoji.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public enum Fitzpatrick {
    TYPE_1_2("1f3fb"),
    TYPE_3("1f3fc"),
    TYPE_4("1f3fd"),
    TYPE_5("1f3fe"),
    TYPE_6("1f3ff");

    private final String hex;

    Fitzpatrick(String hex) {
        this.hex = hex;
    }

    public static boolean isFitzpatrick(String hexIn) {
        return getFitzpatrick(hexIn) != null;
    }

    @Nullable
    public static Fitzpatrick getFitzpatrick(String hexIn) {
        for (Fitzpatrick fitzpatrick : Fitzpatrick.values()) {
            if (hexIn.equalsIgnoreCase(fitzpatrick.getHex())) {
                return fitzpatrick;
            }
        }
        return null;
    }

    public String getHex() {
        return this.hex;
    }
}
