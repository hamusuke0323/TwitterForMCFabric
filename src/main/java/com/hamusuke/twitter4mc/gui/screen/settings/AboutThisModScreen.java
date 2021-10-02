package com.hamusuke.twitter4mc.gui.screen.settings;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import com.hamusuke.twitter4mc.gui.widget.TextWidget;
import com.hamusuke.twitter4mc.gui.widget.list.WidgetList;
import com.hamusuke.twitter4mc.license.License;
import com.hamusuke.twitter4mc.license.LicenseManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public class AboutThisModScreen extends ParentalScreen {
    private WidgetList list;

    public AboutThisModScreen(Screen parent) {
        super(new TranslatableText("tw.about.this.mod"), parent);
    }

    protected void init() {
        super.init();
        int i = this.width / 2;
        int j = this.width / 4;

        this.addButton(new ButtonWidget(j, this.height - 20, i, 20, I18n.translate("gui.back"), (b) -> {
            this.onClose();
        }));

        this.list = new WidgetList(this.minecraft, this.width, this.height, 20, this.height - 20, 20) {
            public int getRowWidth() {
                return i;
            }

            protected int getScrollbarPosition() {
                return this.width - 5;
            }
        };

        FabricLoader.getInstance().getModContainer(TwitterForMC.MOD_ID).ifPresent((mod) -> {
            ModMetadata data = mod.getMetadata();
            this.list.addEntry(new TextWidget(j, 0, i, 20, new TranslatableText("tw.mod.id", data.getId())));
            this.list.addEntry(new TextWidget(j, 30, i, 20, new TranslatableText("tw.mod.name", data.getName())));
            this.list.addEntry(new TextWidget(j, 60, i, 20, new TranslatableText("tw.mod.version", data.getVersion())));
        });

        this.list.addEntry(new TextWidget(j, 90, i, 20, new TranslatableText("tw.open.source.license")));

        List<License> licenses = LicenseManager.getLicenseList();
        for (int index = 0; index < licenses.size(); index++) {
            License license = licenses.get(index);
            this.list.addEntry(new ButtonWidget(j, 120 + index * 30, i, 20, I18n.translate(license.getTranslationKey()), (b) -> {
                this.minecraft.openScreen(new ViewLicenseScreen(license.getTranslationText(), this, license));
            }));
        }

        this.children.add(this.list);
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        this.list.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 5, Formatting.WHITE.getColorValue());
        super.render(mouseX, mouseY, delta);
    }
}
