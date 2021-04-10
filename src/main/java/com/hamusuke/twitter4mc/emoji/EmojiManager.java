package com.hamusuke.twitter4mc.emoji;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class EmojiManager implements SimpleSynchronousResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private final Map<String, Emoji> emojiMap = Maps.newHashMap();

    public Identifier getFabricId() {
        return new Identifier(TwitterForMC.MOD_ID, "emoji_resources");
    }

    public void apply(ResourceManager manager) {
        this.load(manager);
    }

    private synchronized void load(ResourceManager resourceManager) {
        this.emojiMap.clear();
        Iterator<String> iterator = resourceManager.getAllNamespaces().iterator();

        while (iterator.hasNext()) {
            String nameSpace = iterator.next();
            String file = "textures/twitter/emoji.json";

            try {
                Identifier identifier = new Identifier(nameSpace, file);
                this.load(resourceManager.getAllResources(identifier));
            } catch (FileNotFoundException e) {
            } catch (Exception e) {
                LOGGER.warn("Skipped emoji file: {}:{}", nameSpace, file);
            }
        }
    }

    private void load(List<Resource> list) {
        Iterator<Resource> iterator = list.listIterator();

        while (iterator.hasNext()) {
            Resource resource = iterator.next();
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
        Iterator<JsonElement> iterator = jsonArray.iterator();

        while (iterator.hasNext()) {
            JsonObject jsonObject = iterator.next().getAsJsonObject();
            String hex = jsonObject.get("hex").getAsString();
            Emoji emoji = new Emoji(hex, new Identifier(jsonObject.get("image").getAsString()), jsonObject.get("width").getAsInt());
            if (this.emojiMap.put(hex, emoji) == null) {
                LOGGER.debug("Registering emoji: {}:{}", emoji.getId().getNamespace(), emoji.getHex());
            }
        }
    }

    public boolean isEmoji(String hex) {
        return this.emojiMap.containsKey(hex);
    }

    public Emoji getEmoji(String hex) {
        Emoji e = this.emojiMap.get(hex);
        return e == null ? new Emoji(hex, MissingSprite.getMissingSpriteId(), 10) : e;
    }
}
