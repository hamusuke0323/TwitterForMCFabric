package com.hamusuke.twitter4mc.emoji;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Emoji {
    private final String hex;
    private final Identifier id;

    public Emoji(@NotNull String hex, @NotNull Identifier location) {
        this.hex = Objects.requireNonNull(hex, "hex cannot be null.");
        this.id = Objects.requireNonNull(location, "location cannot be null.");
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

    @Environment(EnvType.CLIENT)
    public void renderEmoji(Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, float x, float y, float alpha, int light) {
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getText(this.id));
        vertexConsumer.vertex(matrix, x, y, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 0.0F).light(light).next();
        vertexConsumer.vertex(matrix, x, y + this.getEmojiWidth(), 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix, x + this.getEmojiWidth(), y + this.getEmojiWidth(), 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix, x + this.getEmojiWidth(), y, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 0.0F).light(light).next();
    }

    @Override
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

    @Override
    public int hashCode() {
        return Objects.hash(this.hex, this.id);
    }
}
