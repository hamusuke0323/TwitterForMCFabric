package com.hamusuke.twitter4mc.font;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.emoji.Emoji;
import com.hamusuke.twitter4mc.text.CharacterAndEmojiVisitor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class TweetTextDrawer implements CharacterAndEmojiVisitor {
    private final Function<Identifier, FontStorage> fontStorageAccessor;
    final VertexConsumerProvider vertexConsumers;
    private final boolean shadow;
    private final float brightnessMultiplier;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final Matrix4f matrix;
    private final TextRenderer.TextLayerType layerType;
    private final int light;
    float x;
    float y;
    @Nullable
    private List<GlyphRenderer.Rectangle> rectangles;

    private void addRectangle(GlyphRenderer.Rectangle rectangle) {
        if (this.rectangles == null) {
            this.rectangles = Lists.newArrayList();
        }

        this.rectangles.add(rectangle);
    }

    public TweetTextDrawer(Function<Identifier, FontStorage> fontStorageAccessor, VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, boolean seeThrough, int light) {
        this(fontStorageAccessor, vertexConsumers, x, y, color, shadow, matrix, seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, light);
    }

    public TweetTextDrawer(Function<Identifier, FontStorage> fontStorageAccessor, VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, TextRenderer.TextLayerType layerType, int light) {
        this.fontStorageAccessor = fontStorageAccessor;
        this.vertexConsumers = vertexConsumers;
        this.x = x;
        this.y = y;
        this.shadow = shadow;
        this.brightnessMultiplier = shadow ? 0.25F : 1.0F;
        this.red = (float) (color >> 16 & 255) / 255.0F * this.brightnessMultiplier;
        this.green = (float) (color >> 8 & 255) / 255.0F * this.brightnessMultiplier;
        this.blue = (float) (color & 255) / 255.0F * this.brightnessMultiplier;
        this.alpha = (float) (color >> 24 & 255) / 255.0F;
        this.matrix = matrix;
        this.layerType = layerType;
        this.light = light;
    }

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        FontStorage fontStorage = this.fontStorageAccessor.apply(style.getFont());
        Glyph glyph = codePoint == 12288 ? () -> 6.0F : fontStorage.getGlyph(codePoint);
        GlyphRenderer glyphRenderer = style.isObfuscated() && codePoint != 32 && codePoint != 12288 ? fontStorage.getObfuscatedGlyphRenderer(glyph) : codePoint == 12288 ? new EmptyGlyphRenderer() : fontStorage.getGlyphRenderer(codePoint);
        boolean bl = style.isBold();
        float f = this.alpha;
        TextColor textColor = style.getColor();
        float m;
        float n;
        float o;
        if (textColor != null) {
            int k = textColor.getRgb();
            m = (float) (k >> 16 & 255) / 255.0F * this.brightnessMultiplier;
            n = (float) (k >> 8 & 255) / 255.0F * this.brightnessMultiplier;
            o = (float) (k & 255) / 255.0F * this.brightnessMultiplier;
        } else {
            m = this.red;
            n = this.green;
            o = this.blue;
        }

        float s;
        float r;
        if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
            r = bl ? glyph.getBoldOffset() : 0.0F;
            s = this.shadow ? glyph.getShadowOffset() : 0.0F;
            VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));
            glyphRenderer.draw(style.isItalic(), this.x + s, this.y + s, this.matrix, vertexConsumer, m, n, o, f, this.light);
            if (bl) {
                glyphRenderer.draw(style.isItalic(), this.x + s + r, this.y + s, this.matrix, vertexConsumer, m, n, o, f, this.light);
            }
        }

        r = glyph.getAdvance(bl);
        s = this.shadow ? 1.0F : 0.0F;
        if (style.isStrikethrough()) {
            this.addRectangle(new GlyphRenderer.Rectangle(this.x + s - 1.0F, this.y + s + 4.5F, this.x + s + r, this.y + s + 4.5F - 1.0F, 0.01F, m, n, o, f));
        }

        if (style.isUnderlined()) {
            this.addRectangle(new GlyphRenderer.Rectangle(this.x + s - 1.0F, this.y + s + 9.0F, this.x + s + r, this.y + s + 9.0F - 1.0F, 0.01F, m, n, o, f));
        }

        this.x += r;
        return true;
    }

    @Override
    public boolean acceptEmoji(Emoji emoji) {
        emoji.renderEmoji(this.matrix, this.vertexConsumers, this.x, this.y, this.alpha, this.light);
        this.x += emoji.getEmojiWidth();
        return true;
    }

    public float drawLayer(int underlineColor, float x) {
        if (underlineColor != 0) {
            float f = (float) (underlineColor >> 24 & 255) / 255.0F;
            float g = (float) (underlineColor >> 16 & 255) / 255.0F;
            float h = (float) (underlineColor >> 8 & 255) / 255.0F;
            float i = (float) (underlineColor & 255) / 255.0F;
            this.addRectangle(new GlyphRenderer.Rectangle(x - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, g, h, i, f));
        }

        if (this.rectangles != null) {
            GlyphRenderer glyphRenderer = this.fontStorageAccessor.apply(Style.DEFAULT_FONT_ID).getRectangleRenderer();
            VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));

            for (GlyphRenderer.Rectangle rectangle : this.rectangles) {
                glyphRenderer.drawRectangle(rectangle, this.matrix, vertexConsumer, this.light);
            }
        }

        return this.x;
    }
}
