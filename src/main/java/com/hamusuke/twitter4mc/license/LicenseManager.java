package com.hamusuke.twitter4mc.license;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class LicenseManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<License> LICENSE_LIST = Lists.newArrayList();

    private LicenseManager() {
        throw new IllegalStateException();
    }

    public static void registerLicense(Identifier location, int width, String translationKey) {
        try {
            List<String> list = getLicenseTextList(location);
            registerLicense(new License(location, list, width, translationKey));
        } catch (IOException e) {
            LOGGER.warn("Couldn't load license file", e);
        }
    }

    public static void registerLicense(License license) {
        LOGGER.info("Registering License: {}:{}", license.getTextLocation().getNamespace(), license.getTextLocation().getPath());
        LICENSE_LIST.add(license);
    }

    public static List<License> getLicenseList() {
        return ImmutableList.copyOf(LICENSE_LIST);
    }

    private static List<String> getLicenseTextList(Identifier location) throws IOException {
        InputStream inputStream = LicenseManager.class.getResourceAsStream("/assets/" + location.getNamespace() + "/" + location.getPath());
        if (inputStream == null) {
            throw new IOException("License file not found");
        }

        return IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
    }
}
