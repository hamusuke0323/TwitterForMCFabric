package com.hamusuke.twitter4mc.license;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ILicense {
    Identifier getTextLocation();

    List<String> getLicenseTextList();

    int getWidth();

    TranslatableText getTranslationText();

    String getTranslationKey();
}
