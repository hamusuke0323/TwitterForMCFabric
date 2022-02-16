package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ErrorScreen extends ParentalScreen {
    private final String errorMsg;

    public ErrorScreen(Text text, @Nullable Screen parent, String errorMsg) {
        super(text, parent);
        this.errorMsg = errorMsg;
    }

    @Override
    protected void init() {
        super.init();
        int i = this.width / 2;
        this.addDrawableChild(new ButtonWidget(i / 2, this.height - 20, i, 20, ScreenTexts.BACK, b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.parent != null) {
            matrices.push();
            matrices.translate(0.0D, 0.0D, -1.0D);
            this.parent.render(matrices, -1, -1, delta);
            matrices.pop();
            this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderBackground(matrices);
        }

        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
        List<OrderedText> list = this.textRenderer.wrapLines(Text.of(this.errorMsg), this.width / 2);
        for (int i = 0; i < list.size(); i++) {
            this.textRenderer.drawWithShadow(matrices, list.get(i), (float) this.width / 4, 50 + i * this.textRenderer.fontHeight, 16777215);
        }
    }
}
