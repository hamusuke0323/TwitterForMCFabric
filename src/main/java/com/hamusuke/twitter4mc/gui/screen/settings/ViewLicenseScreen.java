package com.hamusuke.twitter4mc.gui.screen.settings;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import com.hamusuke.twitter4mc.gui.widget.TextWidget;
import com.hamusuke.twitter4mc.gui.widget.list.WidgetList;
import com.hamusuke.twitter4mc.license.License;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ViewLicenseScreen extends ParentalScreen {
    protected final License license;
    protected List<StringVisitable> lines = Lists.newArrayList();
    protected WidgetList list;

    public ViewLicenseScreen(Text title, Screen parent, License license) {
        super(title, parent);
        this.license = license;
    }

    protected void init() {
        super.init();

        this.lines.clear();
        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height - 20, this.width / 2, 20, ScreenTexts.BACK, b -> this.onClose()));

        this.list = new WidgetList(this.client, this.width, this.height, 20, this.height - 20, 10) {
            public int getRowWidth() {
                return ViewLicenseScreen.this.license.getWidth();
            }

            protected int getScrollbarPositionX() {
                return this.width - 5;
            }
        };

        for (String s : this.license.getLicenseTextList()) {
            this.lines.addAll(this.textRenderer.getTextHandler().wrapLines(s, this.list.getRowWidth(), Style.EMPTY));
        }

        for (int i = 0; i < this.lines.size(); i++) {
            this.list.addEntry(new TextWidget((this.width - this.list.getRowWidth()) / 2, i * this.textRenderer.fontHeight, this.list.getRowWidth(), this.textRenderer.fontHeight, new LiteralText(this.lines.get(i).getString())) {
                public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                    ViewLicenseScreen.this.textRenderer.drawWithShadow(matrices, this.getMessage(), this.x, this.y, 16777215);
                }
            });
        }

        this.addDrawableChild(this.list);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.list.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
