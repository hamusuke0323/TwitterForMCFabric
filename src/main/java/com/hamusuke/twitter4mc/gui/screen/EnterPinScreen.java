package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EnterPinScreen extends Screen {
    private final Consumer<String> callback;
    private TextFieldWidget pin;

    public EnterPinScreen(Consumer<String> callback) {
        super(new TranslatableText("tw.enter.pin"));
        this.callback = callback;
    }

    protected void init() {
        super.init();
        int i = this.width / 3;
        this.pin = new TextFieldWidget(this.font, i, this.height / 2, i, 20, this.pin, "");
        this.addButton(this.pin);

        this.addButton(new ButtonWidget(i, this.height - 20, i, 20, I18n.translate("gui.done"), (b) -> {
            b.active = false;
            this.pin.setEditable(false);
            this.callback.accept(this.pin.getText());
        }));
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, this.height / 2 - 15, Formatting.WHITE.getColorValue());
        super.render(mouseX, mouseY, delta);
    }
}
