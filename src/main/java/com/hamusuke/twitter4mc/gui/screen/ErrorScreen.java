package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ErrorScreen extends ParentalScreen {
    private final String errormsg;

    public ErrorScreen(Text text, @Nullable Screen parent, String errormsg) {
        super(text, parent);
        this.errormsg = errormsg;
    }

    protected void init() {
        super.init();
        int i = this.width / 3;
        this.addButton(new ButtonWidget(i, this.height - 20, i, 20, I18n.translate("gui.back"), (b) -> {
            this.onClose();
        }));
    }

    public void render(int mouseX, int mouseY, float delta) {
        if (this.parent != null) {
            this.parent.render(-1, -1, delta);
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderBackground();
        }
        super.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 20, Formatting.WHITE.getColorValue());
        List<String> list = this.font.wrapStringToWidthAsList(this.errormsg, this.width / 3);
        for (int i = 0; i < list.size(); i++) {
            this.font.drawWithShadow(list.get(i), this.width / 3, 50 + i * 10, Formatting.WHITE.getColorValue());
        }
    }
}
