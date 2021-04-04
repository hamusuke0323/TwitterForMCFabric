package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class TwitterSettingsScreen extends ParentalScreen {
    public TwitterSettingsScreen(Screen parent) {
        super(new TranslatableText("tw.settings"), parent);
    }

    protected void init() {
        super.init();
        int i = this.width / 2;
        int j = this.width / 4;
        this.addButton(new ButtonWidget(j, this.height / 2, i, 20, I18n.translate("tw.about.this.mod"), (b) -> {
            this.minecraft.openScreen(new AboutThisModScreen(this));
        }));

        this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("gui.back"), (b) -> {
            this.onClose();
        }));
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 10, Formatting.WHITE.getColorValue());
        super.render(mouseX, mouseY, delta);
    }
}
