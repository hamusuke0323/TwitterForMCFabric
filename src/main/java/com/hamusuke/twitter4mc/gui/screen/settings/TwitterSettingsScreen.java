package com.hamusuke.twitter4mc.gui.screen.settings;

import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class TwitterSettingsScreen extends ParentalScreen {
    public TwitterSettingsScreen(Screen parent) {
        super(new TranslatableText("tw.settings"), parent);
    }

    @Override
    protected void init() {
        super.init();
        int i = this.width / 2;
        int j = this.width / 4;
        this.addDrawableChild(new ButtonWidget(j, this.height / 2 - 20, i, 20, new TranslatableText("tw.about.this.mod"), b -> {
            this.client.setScreen(new AboutThisModScreen(this));
        }));

        this.addDrawableChild(new ButtonWidget(j, this.height / 2, i, 20, new TranslatableText("tw.view.emoji"), b -> {
            this.client.setScreen(new ViewEmojiScreen(this));
        }));

        this.addDrawableChild(new ButtonWidget(j, this.height - 20, i, 20, ScreenTexts.BACK, b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices, 0);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
