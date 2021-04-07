package com.hamusuke.twitter4mc.emoji;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Emoji {
    private final String hex;
    private final Identifier id;
    private final int width;

    public Emoji(String hex, Identifier location, int width) {
        this.hex = hex;
        this.id = location;
        this.width = width;
    }

    public String getHex() {
        return this.hex;
    }

    public Identifier getId() {
        return this.id;
    }

    public int getEmojiWidth() {
        return this.width;
    }

    public void renderEmoji(Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, float x, float y, float alpha, int light) {
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getText(this.id));
        vertexConsumer.vertex(matrix, x, y, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 0.0F).light(light).next();
        vertexConsumer.vertex(matrix, x, y + this.getEmojiWidth(), 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix, x + this.getEmojiWidth(), y + this.getEmojiWidth(), 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix, x + this.getEmojiWidth(), y, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 0.0F).light(light).next();
    }

    public boolean equals(Object obj) {
        if (obj instanceof Emoji) {
            Emoji emoji = (Emoji) obj;
            return this.hex.equals(emoji.getHex());
        } else {
            return false;
        }
    }
}
