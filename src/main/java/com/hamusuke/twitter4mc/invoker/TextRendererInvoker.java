package com.hamusuke.twitter4mc.invoker;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public interface TextRendererInvoker {
    int drawWithShadowAndEmoji(MatrixStack matrices, String text, float x, float y, int color);

    int drawWithShadowAndEmoji(MatrixStack matrices, String text, float x, float y, int color, boolean rightToLeft);

    int drawWithEmoji(MatrixStack matrices, String text, float x, float y, int color);

    int drawWithEmoji(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light);

    int drawWithEmoji(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean rightToLeft);

    int getWidthWithEmoji(String text);
}
