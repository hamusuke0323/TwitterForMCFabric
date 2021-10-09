package com.hamusuke.twitter4mc.invoker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface TextRendererInvoker {
    int drawWithShadowAndEmoji(MatrixStack matrices, Text text, float x, float y, int color);

    int drawWithEmoji(MatrixStack matrices, Text text, float x, float y, int color);

    int drawWithEmoji(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light);

    int drawWithShadowAndEmoji(MatrixStack matrices, OrderedText text, float x, float y, int color);

    int drawWithEmoji(MatrixStack matrices, OrderedText text, float x, float y, int color);

    int drawWithEmoji(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light);

    int getWidthWithEmoji(OrderedText text);

    List<OrderedText> wrapLinesWithEmoji(StringVisitable text, int width);
}
