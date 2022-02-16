package com.hamusuke.twitter4mc.gui.screen.settings;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.emoji.Emoji;
import com.hamusuke.twitter4mc.emoji.EmojiManager;
import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ViewEmojiScreen extends ParentalScreen {
    private ViewEmojiScreen.EmojiList emojiList;

    public ViewEmojiScreen(Screen parent) {
        super(new TranslatableText("tw.view.emoji"), parent);
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height - 20, this.width / 2, 20, ScreenTexts.BACK, b -> this.onClose()));

        this.emojiList = new EmojiList(TwitterForMC.getEmojiManager(), this.client, this.width, this.height, 30, this.height - 20, 50);
        this.addSelectableChild(this.emojiList);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.emojiList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    private class EmojiList extends EntryListWidget<EmojiList.EmojiEntry> {
        private EmojiList(EmojiManager emojiManager, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
            emojiManager.getAllEmojis().forEach((hex, emoji) -> this.addEntry(new EmojiEntry(emoji)));
        }

        @Override
        protected int getScrollbarPositionX() {
            return this.width - 5;
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }

        @Override
        protected boolean isSelectedEntry(int index) {
            return true;
        }

        @Override
        protected boolean isFocused() {
            return true;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
        }

        @Environment(EnvType.CLIENT)
        private class EmojiEntry extends EntryListWidget.Entry<EmojiEntry> {
            private final Emoji emoji;

            private EmojiEntry(Emoji emoji) {
                this.emoji = emoji;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovering, float delta) {
                ViewEmojiScreen.this.textRenderer.drawWithShadow(matrices, "Hexadecimal(Character code): " + this.emoji.getHex(), x, y + (float) height / 2 - 4, 16777215);
                RenderSystem.setShaderTexture(0, this.emoji.getId());
                DrawableHelper.drawTexture(matrices, x + width - 60, y + 2, 0.0F, 0.0F, 42, 42, 42, 42);
            }
        }
    }
}
