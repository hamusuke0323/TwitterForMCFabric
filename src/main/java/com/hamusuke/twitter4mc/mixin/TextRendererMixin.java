package com.hamusuke.twitter4mc.mixin;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.emoji.Emoji;
import com.hamusuke.twitter4mc.emoji.Fitzpatrick;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(TextRenderer.class)
public class TextRendererMixin {
    @Shadow
    @Final
    private FontStorage fontStorage;

    @Shadow
    private void drawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
    }

    @Shadow
    @Final
    private TextureManager textureManager;

    /**
     * @reason render Emoji
     * @author hamusuke0323
     */
    @Overwrite
    private float drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
        float f = shadow ? 0.25F : 1.0F;
        float g = (float) (color >> 16 & 255) / 255.0F * f;
        float h = (float) (color >> 8 & 255) / 255.0F * f;
        float i = (float) (color & 255) / 255.0F * f;
        float j = x;
        float k = g;
        float l = h;
        float m = i;
        float n = (float) (color >> 24 & 255) / 255.0F;
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        List<GlyphRenderer.Rectangle> list = Lists.newArrayList();

        StringBuilder emoji = new StringBuilder();
        for (int o = 0; o < text.length(); ++o) {
            char c = text.charAt(o);
            if (c == 167 && o + 1 < text.length()) {
                Formatting formatting = Formatting.byCode(text.charAt(o + 1));
                if (formatting != null) {
                    if (formatting.affectsGlyphWidth()) {
                        bl = false;
                        bl2 = false;
                        bl5 = false;
                        bl4 = false;
                        bl3 = false;
                        k = g;
                        l = h;
                        m = i;
                    }

                    if (formatting.getColorValue() != null) {
                        int p = formatting.getColorValue();
                        k = (float) (p >> 16 & 255) / 255.0F * f;
                        l = (float) (p >> 8 & 255) / 255.0F * f;
                        m = (float) (p & 255) / 255.0F * f;
                    } else if (formatting == Formatting.OBFUSCATED) {
                        bl = true;
                    } else if (formatting == Formatting.BOLD) {
                        bl2 = true;
                    } else if (formatting == Formatting.STRIKETHROUGH) {
                        bl5 = true;
                    } else if (formatting == Formatting.UNDERLINE) {
                        bl4 = true;
                    } else if (formatting == Formatting.ITALIC) {
                        bl3 = true;
                    }
                }

                ++o;
            } else if (TwitterForMC.getEmojiManager().isEmoji(Integer.toHexString(c))) {
                Emoji e = getEmoji(Integer.toHexString(c));
                e.renderEmoji(matrix, vertexConsumerProvider, j, y, n, light);
                j += e.getEmojiWidth();
            } else if (Character.isHighSurrogate(c) && o + 1 < text.length() && Character.isLowSurrogate(text.charAt(o + 1))) {
                char low = text.charAt(o + 1);
                emoji.append(Integer.toHexString(Character.toCodePoint(c, low)));
                o++;
                if (o + 1 < text.length() && (text.charAt(o + 1) == 0x200d || (o + 2 < text.length() && Fitzpatrick.isFitzpatrick(Integer.toHexString(Character.toCodePoint(text.charAt(o + 1), text.charAt(o + 2))))))) {
                    emoji.append("-");
                } else {
                    Emoji e = getEmoji(emoji.toString());
                    e.renderEmoji(matrix, vertexConsumerProvider, j, y, n, light);
                    j += e.getEmojiWidth();
                    emoji = new StringBuilder();
                }
            } else if (c == 0x200d) {
                emoji.append(Integer.toHexString(c)).append("-");
            } else if (!emoji.toString().isEmpty()) {
                Emoji e = getEmoji(emoji.substring(0, emoji.length() - 2));
                e.renderEmoji(matrix, vertexConsumerProvider, j, y, n, light);
                j += e.getEmojiWidth();
                emoji = new StringBuilder();
            } else {
                Glyph glyph = this.getGlyph(c);

                GlyphRenderer glyphRenderer = bl ? this.fontStorage.getObfuscatedGlyphRenderer(glyph) : this.fontStorage.getGlyphRenderer(c);
                float s;
                float t;
                if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
                    s = bl2 ? glyph.getBoldOffset() : 0.0F;
                    t = shadow ? glyph.getShadowOffset() : 0.0F;
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(glyphRenderer.method_24045(seeThrough));
                    this.drawGlyph(glyphRenderer, bl2, bl3, s, j + t, y + t, matrix, vertexConsumer, k, l, m, n, light);
                }

                s = glyph.getAdvance(bl2);
                t = shadow ? 1.0F : 0.0F;
                if (bl5) {
                    list.add(new GlyphRenderer.Rectangle(j + t - 1.0F, y + t + 4.5F, j + t + s, y + t + 4.5F - 1.0F, -0.01F, k, l, m, n));
                }

                if (bl4) {
                    list.add(new GlyphRenderer.Rectangle(j + t - 1.0F, y + t + 9.0F, j + t + s, y + t + 9.0F - 1.0F, -0.01F, k, l, m, n));
                }

                j += s;
            }
        }

        if (underlineColor != 0) {
            float u = (float) (underlineColor >> 24 & 255) / 255.0F;
            float v = (float) (underlineColor >> 16 & 255) / 255.0F;
            float w = (float) (underlineColor >> 8 & 255) / 255.0F;
            float z = (float) (underlineColor & 255) / 255.0F;
            list.add(new GlyphRenderer.Rectangle(x - 1.0F, y + 9.0F, j + 1.0F, y - 1.0F, 0.01F, v, w, z, u));
        }

        if (!list.isEmpty()) {
            GlyphRenderer glyphRenderer2 = this.fontStorage.getRectangleRenderer();
            VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(glyphRenderer2.method_24045(seeThrough));

            for (GlyphRenderer.Rectangle rectangle : list) {
                glyphRenderer2.drawRectangle(rectangle, matrix, vertexConsumer2, light);
            }
        }

        return j;
    }

    /**
     * @reason emoji width
     * @author hamusuke0323
     */
    @Overwrite
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            float f = 0.0F;
            boolean bl = false;

            StringBuilder emoji = new StringBuilder();
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if (c == 167 && i < text.length() - 1) {
                    ++i;
                    Formatting formatting = Formatting.byCode(text.charAt(i));
                    if (formatting == Formatting.BOLD) {
                        bl = true;
                    } else if (formatting != null && formatting.affectsGlyphWidth()) {
                        bl = false;
                    }
                } else if (TwitterForMC.getEmojiManager().isEmoji(Integer.toHexString(c))) {
                    Emoji e = getEmoji(Integer.toHexString(c));
                    f += e.getEmojiWidth();
                } else if (Character.isHighSurrogate(c) && i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) {
                    char low = text.charAt(i + 1);
                    emoji.append(Integer.toHexString(Character.toCodePoint(c, low)));
                    i++;
                    if (i + 1 < text.length() && (text.charAt(i + 1) == 0x200d || (i + 2 < text.length() && Fitzpatrick.isFitzpatrick(Integer.toHexString(Character.toCodePoint(text.charAt(i + 1), text.charAt(i + 2))))))) {
                        emoji.append("-");
                    } else {
                        Emoji e = getEmoji(emoji.toString());
                        f += e.getEmojiWidth();
                        emoji = new StringBuilder();
                    }
                } else if (c == 0x200d) {
                    emoji.append(Integer.toHexString(c)).append("-");
                } else if (!emoji.toString().isEmpty()) {
                    Emoji e = getEmoji(emoji.substring(0, emoji.length() - 2));
                    f += e.getEmojiWidth();
                    emoji = new StringBuilder();
                } else {
                    f += this.getGlyph(c).getAdvance(bl);
                }
            }

            return MathHelper.ceil(f);
        }
    }

    private Glyph getGlyph(char c) {
        try {
            return this.fontStorage.getGlyph(c);
        } catch (ArrayIndexOutOfBoundsException e) {
            return () -> 4.0F;
        }
    }

    private static Emoji getEmoji(String hex) {
        return TwitterForMC.getEmojiManager().getEmoji(hex);
    }
}
