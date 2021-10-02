package com.hamusuke.twitter4mc.gui.widget.list;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class WidgetList extends ElementListWidget<WidgetList.AbstractButtonEntry> {
    public WidgetList(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.centerListVertically = false;
    }

    public int addEntry(AbstractButtonEntry entry) {
        return super.addEntry(entry);
    }

    public void addEntry(ClickableWidget widget) {
        AbstractButtonEntry entry = new AbstractButtonEntry();
        entry.addWidget(widget);
        this.addEntry(entry);
    }

    @Environment(EnvType.CLIENT)
    public static class AbstractButtonEntry extends ElementListWidget.Entry<WidgetList.AbstractButtonEntry> {
        private final List<ClickableWidget> buttons = Lists.newArrayList();

        public <T extends ClickableWidget> T addWidget(T button) {
            this.buttons.add(button);
            return button;
        }

        public List<? extends Element> children() {
            return this.buttons;
        }

        public List<? extends Selectable> selectableChildren() {
            return this.buttons;
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.buttons.forEach((button) -> {
                button.y = y;
                button.render(matrices, mouseX, mouseY, tickDelta);
            });
        }
    }
}
