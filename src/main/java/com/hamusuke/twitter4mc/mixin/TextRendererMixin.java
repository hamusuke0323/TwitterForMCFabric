package com.hamusuke.twitter4mc.mixin;

import com.hamusuke.twitter4mc.font.TweetTextDrawer;
import com.hamusuke.twitter4mc.font.TweetTextVisitFactory;
import com.hamusuke.twitter4mc.invoker.TextHandlerInvoker;
import com.hamusuke.twitter4mc.invoker.TextRendererInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
@Mixin(TextRenderer.class)
public abstract class TextRendererMixin implements TextRendererInvoker {
    @Shadow
    @Final
    private static Vec3f FORWARD_SHIFT;
    @Shadow
    @Final
    private Function<Identifier, FontStorage> fontStorageAccessor;
    @Shadow
    @Final
    private TextHandler handler;

    @Shadow
    private static int tweakTransparency(int argb) {
        return 0;
    }

    @Shadow
    public abstract boolean isRightToLeft();

    @Shadow
    public abstract String mirror(String text);

    public int drawWithShadowAndEmoji(MatrixStack matrices, String text, float x, float y, int color) {
        return this.drawWithEmoji(text, x, y, color, matrices.peek().getModel(), true, this.isRightToLeft());
    }

    public int drawWithShadowAndEmoji(MatrixStack matrices, String text, float x, float y, int color, boolean rightToLeft) {
        return this.drawWithEmoji(text, x, y, color, matrices.peek().getModel(), true, rightToLeft);
    }

    public int drawWithEmoji(MatrixStack matrices, String text, float x, float y, int color) {
        return this.drawWithEmoji(text, x, y, color, matrices.peek().getModel(), false, this.isRightToLeft());
    }

    private int drawWithEmoji(String text, float x, float y, int color, Matrix4f matrix, boolean shadow, boolean mirror) {
        if (text == null) {
            return 0;
        } else {
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            int i = this.drawWithEmoji(text, x, y, color, shadow, matrix, immediate, false, 0, 15728880, mirror);
            immediate.draw();
            return i;
        }
    }

    public int drawWithEmoji(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        return this.drawWithEmoji(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light, this.isRightToLeft());
    }

    public int drawWithEmoji(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean rightToLeft) {
        return this.drawInternalWithEmoji(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light, rightToLeft);
    }

    public int getWidthWithEmoji(@Nullable String text) {
        return MathHelper.ceil(((TextHandlerInvoker) this.handler).getWidthWithEmoji(text));
    }

    private int drawInternalWithEmoji(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean mirror) {
        if (mirror) {
            text = this.mirror(text);
        }

        color = tweakTransparency(color);
        Matrix4f matrix4f = matrix.copy();
        if (shadow) {
            this.drawLayerWithEmoji(text, x, y, color, true, matrix, vertexConsumers, seeThrough, backgroundColor, light);
            matrix4f.addToLastColumn(FORWARD_SHIFT);
        }

        x = this.drawLayerWithEmoji(text, x, y, color, false, matrix4f, vertexConsumers, seeThrough, backgroundColor, light);
        return (int) x + (shadow ? 1 : 0);
    }

    private float drawLayerWithEmoji(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
        TweetTextDrawer drawer = new TweetTextDrawer(this.fontStorageAccessor, vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);
        TweetTextVisitFactory.visitCharacterOrEmoji(text, Style.EMPTY, drawer, drawer);
        return drawer.drawLayer(underlineColor, x);
    }
}
