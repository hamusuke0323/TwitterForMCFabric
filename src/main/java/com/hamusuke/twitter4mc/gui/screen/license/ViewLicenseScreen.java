package com.hamusuke.twitter4mc.gui.screen.license;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import com.hamusuke.twitter4mc.gui.widget.TextWidget;
import com.hamusuke.twitter4mc.gui.widget.list.WidgetList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ViewLicenseScreen extends ParentalScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final Identifier text;
    protected List<String> lines = Lists.newArrayList();
    protected WidgetList list;

    public ViewLicenseScreen(Text title, Screen parent, Identifier textFile) {
        super(title, parent);
        this.text = textFile;
    }

    protected void init() {
        super.init();

        this.lines.clear();
        this.addButton(new ButtonWidget(this.width / 4, this.height - 20, this.width / 2, 20, I18n.translate("gui.back"), (b) -> {
            this.onClose();
        }));

        this.list = new WidgetList(this.minecraft, this.width, this.height, 20, this.height - 20, 10) {
            public int getRowWidth() {
                return this.width - (this.width / 10) * 2;
            }

            protected int getScrollbarPosition() {
                return this.width - 5;
            }
        };
        try {
            InputStream inputStream = ViewLicenseScreen.class.getResourceAsStream("/assets/" + this.text.getNamespace() + "/" + this.text.getPath());
            if (inputStream == null) {
                throw new IOException("Couldn't load file");
            }
            List<String> list = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            for (String s : list) {
                this.lines.addAll(this.font.wrapStringToWidthAsList(s, this.list.getRowWidth()));
            }

            for (int i = 0; i < this.lines.size(); i++) {
                this.list.addEntry(new TextWidget(this.width / 10, i * 10, this.list.getRowWidth(), 10, new LiteralText(this.lines.get(i))) {
                    public void renderButton(int mouseX, int mouseY, float delta) {
                        ViewLicenseScreen.this.font.drawWithShadow(this.getMessage(), this.x, this.y, Formatting.WHITE.getColorValue());
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't load license file", e);
        }

        this.children.add(this.list);
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.list.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 5, Formatting.WHITE.getColorValue());
        super.render(mouseX, mouseY, delta);
    }
}
