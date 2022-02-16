package com.hamusuke.twitter4mc.mixin;

import com.google.common.collect.ImmutableList;
import com.hamusuke.twitter4mc.font.TweetTextDrawer;
import com.hamusuke.twitter4mc.invoker.TextHandlerInvoker;
import com.hamusuke.twitter4mc.invoker.TextRendererInvoker;
import com.hamusuke.twitter4mc.text.TweetTextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
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

    @Override
    public int drawWithShadowAndEmoji(MatrixStack matrices, Text text, float x, float y, int color) {
        return this.drawWithEmoji(text.asOrderedText(), x, y, color, matrices.peek().getModel(), true);
    }

    @Override
    public int drawWithEmoji(MatrixStack matrices, Text text, float x, float y, int color) {
        return this.drawWithEmoji(text.asOrderedText(), x, y, color, matrices.peek().getModel(), false);
    }

    @Override
    public int drawWithShadowAndEmoji(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return this.drawWithEmoji(text, x, y, color, matrices.peek().getModel(), true);
    }

    @Override
    public int drawWithEmoji(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return this.drawWithEmoji(text, x, y, color, matrices.peek().getModel(), false);
    }

    private int drawWithEmoji(OrderedText text, float x, float y, int color, Matrix4f matrix, boolean shadow) {
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        int i = this.drawInternalWithEmoji(text, x, y, color, shadow, matrix, immediate, false, 0, 15728880);
        immediate.draw();
        return i;
    }

    @Override
    public int drawWithEmoji(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        return this.drawInternalWithEmoji(text.asOrderedText(), x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light);
    }

    @Override
    public int drawWithEmoji(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        return this.drawInternalWithEmoji(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light);
    }

    @Override
    public int getWidthWithEmoji(OrderedText text) {
        return MathHelper.ceil(((TextHandlerInvoker) this.handler).getWidthWithEmoji(text));
    }

    @Override
    public List<OrderedText> wrapLinesWithEmoji(StringVisitable text, int width) {
        return this.handler.wrapLines(text, width, Style.EMPTY).stream().map(stringVisitable -> TweetTextUtil.reorderIgnoreStyleChar(stringVisitable, this.isRightToLeft())).collect(ImmutableList.toImmutableList());
    }

    private int drawInternalWithEmoji(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light) {
        color = tweakTransparency(color);
        Matrix4f matrix4f = matrix.copy();
        if (shadow) {
            this.drawLayerWithEmoji(text, x, y, color, true, matrix, vertexConsumers, seeThrough, backgroundColor, light);
            matrix4f.addToLastColumn(FORWARD_SHIFT);
        }

        x = this.drawLayerWithEmoji(text, x, y, color, false, matrix4f, vertexConsumers, seeThrough, backgroundColor, light);
        return (int) x + (shadow ? 1 : 0);
    }

    private float drawLayerWithEmoji(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
        TweetTextDrawer drawer = new TweetTextDrawer(this.fontStorageAccessor, vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);
        text.accept(drawer);
        return drawer.drawLayer(underlineColor, x);
    }
}
