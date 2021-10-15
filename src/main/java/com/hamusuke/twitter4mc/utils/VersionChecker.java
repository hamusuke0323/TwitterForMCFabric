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
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class VersionChecker {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private static UpdateInfo updateInfo;

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
                        String type = "NONE";

                        if (!current.equalsIgnoreCase(newVersion)) {
                            updateInfo = new UpdateInfo(newVersion, updateUrl);
                            type = newVersion;
                        }

                        LOGGER.info("current TwitterForMC version: {}, new version: {}", current, type);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Couldn't check new update", e);
                }
            }
        });
    }

    public static Optional<UpdateInfo> getUpdateInfo() {
        return Optional.ofNullable(updateInfo);
    }

    @Environment(EnvType.CLIENT)
    public static record UpdateInfo(String newVersion, String url) {
    }
}
