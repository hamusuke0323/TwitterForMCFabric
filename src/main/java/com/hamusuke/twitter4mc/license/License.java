package com.hamusuke.twitter4mc.license;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class License {
    private final Identifier textLocation;
    private final List<String> text;
    private final int width;
    private final TranslatableText translatableText;
    private final String translationKey;

    public License(Identifier textLocation, List<String> text, int width, String translationKey) {
        this.textLocation = textLocation;
        this.text = text;
        this.width = width;
        this.translationKey = translationKey;
        this.translatableText = new TranslatableText(this.translationKey);
    }

    public Identifier getTextLocation() {
        return this.textLocation;
    }

    public List<String> getLicenseTextList() {
        return this.text;
    }

    public int getWidth() {
        return this.width;
    }

    public TranslatableText getTranslationText() {
        return this.translatableText;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }
}
