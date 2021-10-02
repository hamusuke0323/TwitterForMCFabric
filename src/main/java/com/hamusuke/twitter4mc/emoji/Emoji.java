package com.hamusuke.twitter4mc.emoji;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class Emoji {
    private final String hex;
    private final Identifier id;

    public Emoji(@NotNull String hex, @NotNull Identifier location) {
        Objects.requireNonNull(hex, "hex cannot be null.");
        Objects.requireNonNull(location, "location cannot be null.");
        this.hex = hex;
        this.id = location;
    }

    public String getHex() {
        return this.hex;
    }

    public Identifier getId() {
        return this.id;
    }

    public int getEmojiWidth() {
        return 9;
    }

    public void renderEmoji(Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, float x, float y, float alpha, int light) {
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getText(this.id));
        vertexConsumer.vertex(matrix, x, y, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 0.0F).light(light).next();
        vertexConsumer.vertex(matrix, x, y + this.getEmojiWidth(), 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix, x + this.getEmojiWidth(), y + this.getEmojiWidth(), 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix, x + this.getEmojiWidth(), y, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 0.0F).light(light).next();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Emoji emoji = (Emoji) o;
        return this.hex.equals(emoji.hex) && this.id.equals(emoji.id);
    }

    public int hashCode() {
        return Objects.hash(this.hex, this.id);
    }
}
