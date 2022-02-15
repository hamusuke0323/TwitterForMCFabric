package com.hamusuke.twitter4mc.emoji;

import com.google.gson.stream.JsonWriter;
import com.hamusuke.twitter4mc.TwitterForMC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
final class EmojiFactory {
    public static void main(String[] args) {
        Path path = new File("./src/main/resources/assets/" + TwitterForMC.MOD_ID + "/textures/twitter").toPath();
        File[] files = path.resolve("emoji").toFile().listFiles((dir, name) -> name.endsWith(".png"));
        if (files != null) {
            try (JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(path.resolve("emoji.json").toFile()), StandardCharsets.UTF_8))) {
                jsonWriter.setIndent("\t");
                jsonWriter.beginArray();
                for (File file : files) {
                    jsonWriter.beginObject();
                    jsonWriter.name("hex").value(file.getName().replace(".png", ""));
                    jsonWriter.name("image").value(TwitterForMC.MOD_ID + ":" + "textures/twitter/emoji/" + file.getName());
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
