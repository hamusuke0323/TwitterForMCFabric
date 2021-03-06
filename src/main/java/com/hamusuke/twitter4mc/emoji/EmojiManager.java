package com.hamusuke.twitter4mc.emoji;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hamusuke.twitter4mc.TwitterForMC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class EmojiManager implements SimpleSynchronousResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private final Map<String, Emoji> emojiMap = Maps.newHashMap();

    @Override
    public Identifier getFabricId() {
        return new Identifier(TwitterForMC.MOD_ID, "emoji_resources");
    }

    @Override
    public void reload(ResourceManager manager) {
        this.load(manager);
    }

    private synchronized void load(ResourceManager resourceManager) {
        this.emojiMap.clear();

        for (String nameSpace : resourceManager.getAllNamespaces()) {
            String file = "textures/twitter/emoji.json";

            try {
                Identifier identifier = new Identifier(nameSpace, file);
                this.load(resourceManager.getAllResources(identifier));
            } catch (FileNotFoundException ignored) {
            } catch (Exception e) {
                LOGGER.warn("Skipped emoji file: {}:{}", nameSpace, file);
            }
        }
    }

    private void load(List<Resource> list) {
        for (Resource resource : list) {
            InputStream inputStream = resource.getInputStream();

            try {
                this.load(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private void load(InputStream inputStream) {
        JsonArray jsonArray = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonArray.class);
        long time = System.currentTimeMillis();

        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String hex = jsonObject.get("hex").getAsString();
            Emoji emoji = new Emoji(hex, new Identifier(jsonObject.get("image").getAsString()));
            if (this.emojiMap.put(hex, emoji) == null) {
                LOGGER.debug("Registered emoji: {}:{}", emoji.getId().getNamespace(), emoji.getHex());
            }
        }

        LOGGER.info("Total loaded emoji(s): {}, Total load time: {}ms", this.emojiMap.size(), System.currentTimeMillis() - time);
    }

    public boolean isEmoji(String hex) {
        return this.emojiMap.containsKey(hex);
    }

    public Emoji getEmoji(String hex) {
        Emoji e = this.emojiMap.get(hex);
        return e == null ? new Emoji(hex, MissingSprite.getMissingSpriteId()) : e;
    }

    public ImmutableMap<String, Emoji> getAllEmojis() {
        return ImmutableMap.copyOf(this.emojiMap);
    }
}
