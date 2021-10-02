package com.hamusuke.twitter4mc.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hamusuke.twitter4mc.TwitterForMC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class VersionChecker {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isUpdateAvailable;
    private static String version = "";
    private static String url = "";

    private VersionChecker() {
        throw new IllegalStateException();
    }

    public static void checkUpdate() {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(TwitterForMC.MOD_ID);
        modContainerOptional.ifPresent(modContainer -> {

            ModMetadata modMetadata = modContainer.getMetadata();
            CustomValue customValue = modMetadata.getCustomValue("updateJsonUrl");
            String urlString = customValue == null ? "" : customValue.getAsString();
            ContactInformation contactInformation = modMetadata.getContact();
            String updateUrl = contactInformation == null ? "" : contactInformation.get("homepage").orElse("");
            if (!urlString.isEmpty() && !updateUrl.isEmpty()) {
                try (InputStream inputStream = new URL(urlString).openStream()) {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
                    String version = SharedConstants.getGameVersion().getName();

                    if (jsonObject != null && jsonObject.has(version)) {
                        String newVersion = jsonObject.get(version).getAsString();
                        String current = modMetadata.getVersion().getFriendlyString();
                        VersionChecker.isUpdateAvailable = !current.equalsIgnoreCase(newVersion);
                        VersionChecker.version = newVersion;
                        VersionChecker.url = updateUrl;

                        LOGGER.info("current TwitterForMC version: {}, new version: {}", current, VersionChecker.isUpdateAvailable ? newVersion : "NONE");
                    }
                } catch (Exception e) {
                    LOGGER.warn("Couldn't check new update", e);
                }
            }
        });
    }

    public static boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public static String getNewVersion() {
        return version;
    }

    public static String getUrl() {
        return url;
    }
}
