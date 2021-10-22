package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class ClickSpaceToCloseScreen extends ParentalScreen {
    protected ClickSpaceToCloseScreen(Text title, @Nullable Screen parent) {
        super(title, parent);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = super.mouseClicked(mouseX, mouseY, button);
        boolean bl2 = false;

        for (Element element : this.children()) {
            if (element instanceof ClickableWidget clickableWidget && clickableWidget.isHovered()) {
                bl2 = true;
                break;
            }
        }

        if (!bl && !bl2) {
            this.onClose();
        }

        return bl;
    }
}
