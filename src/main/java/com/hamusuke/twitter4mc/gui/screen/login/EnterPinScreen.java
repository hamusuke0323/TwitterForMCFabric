package com.hamusuke.twitter4mc.gui.screen.login;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EnterPinScreen extends Screen {
    private final Consumer<String> callback;
    private TextFieldWidget pin;

    public EnterPinScreen(Consumer<String> callback) {
        super(new TranslatableText("tw.enter.pin"));
        this.callback = callback;
    }

    @Override
    protected void init() {
        super.init();
        int i = this.width / 3;
        this.pin = new TextFieldWidget(this.textRenderer, i, this.height / 2, i, 20, this.pin, NarratorManager.EMPTY);
        this.addDrawableChild(this.pin);

        this.addDrawableChild(new ButtonWidget(i, this.height - 20, i, 20, ScreenTexts.DONE, b -> {
            b.active = false;
            this.pin.setEditable(false);
            this.callback.accept(this.pin.getText());
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices, 0);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.height / 2 - 15, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
