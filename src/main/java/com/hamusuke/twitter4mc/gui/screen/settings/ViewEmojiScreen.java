package com.hamusuke.twitter4mc.gui.screen.settings;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.emoji.Emoji;
import com.hamusuke.twitter4mc.emoji.EmojiManager;
import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class ViewEmojiScreen extends ParentalScreen {
    private EntryListWidget<EmojiList.EmojiEntry> entryListWidget;

    public ViewEmojiScreen(Screen parent) {
        super(new TranslatableText("tw.view.emoji"), parent);
    }

    protected void init() {
        super.init();

        this.addButton(new ButtonWidget(this.width / 4, this.height - 20, this.width / 2, 20, I18n.translate("gui.back"), (b) -> {
            this.onClose();
        }));

        this.entryListWidget = new EmojiList(TwitterForMC.getEmojiManager(), this.minecraft, this.width, this.height, 30, this.height - 20, 50);
        this.children.add(this.entryListWidget);
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.entryListWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 10, Formatting.WHITE.getColorValue());
        super.render(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class EmojiList extends EntryListWidget<EmojiList.EmojiEntry> {
        public EmojiList(EmojiManager emojiManager, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
            emojiManager.getAllEmojis().forEach((hex, emoji) -> this.addEntry(new EmojiEntry(emoji)));
        }

        protected int getScrollbarPosition() {
            return this.width - 5;
        }

        public int getRowWidth() {
            return this.width;
        }

        protected boolean isSelectedItem(int index) {
            return true;
        }

        protected boolean isFocused() {
            return true;
        }

        @Environment(EnvType.CLIENT)
        class EmojiEntry extends EntryListWidget.Entry<EmojiEntry> {
            private final Emoji emoji;

            public EmojiEntry(Emoji emoji) {
                this.emoji = emoji;
            }

            public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovering, float delta) {
                ViewEmojiScreen.this.font.drawWithShadow("Hexadecimal(Character code): " + this.emoji.getHex(), x, y + height / 2 - 4, Formatting.WHITE.getColorValue());
                ViewEmojiScreen.this.minecraft.getTextureManager().bindTexture(this.emoji.getId());
                DrawableHelper.blit(x + width - 60, y + 2, 0.0F, 0.0F, 42, 42, 42, 42);
            }
        }
    }
}
