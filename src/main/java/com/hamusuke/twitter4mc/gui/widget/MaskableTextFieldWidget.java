package com.hamusuke.twitter4mc.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
public class MaskableTextFieldWidget extends ClickableWidget implements Drawable, Element {
    private final TextRenderer textRenderer;
    private String text;
    private final char mask;
    private int maxLength;
    private int focusedTicks;
    private boolean focused;
    private boolean focusUnlocked;
    private boolean editable;
    private boolean selecting;
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private int editableColor;
    private int uneditableColor;
    private String suggestion;
    private final BiFunction<String, Integer, String> renderTextProvider;

    public MaskableTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message, char mask, int maxLength) {
        this(textRenderer, x, y, width, height, null, message, mask, maxLength);
    }

    public MaskableTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable MaskableTextFieldWidget copyFrom, Text message, char mask, int maxLength) {
        super(x, y, width, height, message);
        this.text = "";
        this.mask = mask;
        this.maxLength = maxLength;
        this.focused = true;
        this.focusUnlocked = true;
        this.editable = true;
        this.editableColor = 14737632;
        this.uneditableColor = 7368816;
        this.renderTextProvider = (string, integer) -> this.mask(string);
        this.textRenderer = textRenderer;
        if (copyFrom != null) {
            this.setText(copyFrom.getText());
        }
    }

    public void tick() {
        ++this.focusedTicks;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    protected MutableText getNarrationMessage() {
        Text text = this.getMessage();
        return new TranslatableText("gui.narrate.editBox", text, this.text);
    }

    public void setText(String text) {
        if (text.length() > this.maxLength) {
            this.text = text.substring(0, this.maxLength);
        } else {
            this.text = text;
        }

        this.setCursorToEnd();
        this.setSelectionEnd(this.selectionStart);
        this.onChanged(text);
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.mask(this.text.substring(i, j));
    }

    private String mask(String text) {
        return String.valueOf(this.mask).repeat(text.length());
    }

    public void write(String text) {
        String string = "";
        String string2 = SharedConstants.stripInvalidChars(text);
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        if (!this.text.isEmpty()) {
            string = string + this.text.substring(0, i);
        }

        int m;
        if (k < string2.length()) {
            string = string + string2.substring(0, k);
            m = k;
        } else {
            string = string + string2;
            m = string2.length();
        }

        if (!this.text.isEmpty() && j < this.text.length()) {
            string = string + this.text.substring(j);
        }

        this.text = string;
        this.setSelectionStart(i + m);
        this.setSelectionEnd(this.selectionStart);
        this.onChanged(this.text);
    }

    private void onChanged(String newText) {
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }

    }

    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }

    public void eraseCharacters(int characterOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                boolean bl = characterOffset < 0;
                int i = bl ? this.selectionStart + characterOffset : this.selectionStart;
                int j = bl ? this.selectionStart : this.selectionStart + characterOffset;
                String string = "";
                if (i >= 0) {
                    string = this.text.substring(0, i);
                }

                if (j < this.text.length()) {
                    string = string + this.text.substring(j);
                }
                this.text = string;
                if (bl) {
                    this.moveCursor(characterOffset);
                }

                this.onChanged(this.text);
            }
        }
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);

        for (int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int offset) {
        this.setCursor(this.selectionStart + offset);
    }

    public void setCursor(int cursor) {
        this.setSelectionStart(cursor);
        if (!this.selecting) {
            this.setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
    }

    public void setCursorToStart() {
        this.setCursor(0);
    }

    public void setCursorToEnd() {
        this.setCursor(this.text.length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isActive()) {
            return false;
        } else {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.editable) {
                    this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                if (this.editable) {
                    this.write("");
                }

                return true;
            } else {
                switch (keyCode) {
                    case 259:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(-1);
                            this.selecting = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(1);
                            this.selecting = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.setCursorToStart();
                        return true;
                    case 269:
                        this.setCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean isActive() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char chr, int keyCode) {
        if (!this.isActive()) {
            return false;
        } else if (SharedConstants.isValidChar(chr)) {
            if (this.editable) {
                this.write(Character.toString(chr));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean bl = mouseX >= (double) this.x && mouseX < (double) (this.x + this.width) && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height);
            if (this.focusUnlocked) {
                this.setSelected(bl);
            }

            if (this.isFocused() && bl && button == 0) {
                int i = MathHelper.floor(mouseX) - this.x;
                if (this.focused) {
                    i -= 4;
                }

                String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
                this.setCursor(this.textRenderer.trimToWidth(string, i).length() + this.firstCharacterIndex);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setSelected(boolean selected) {
        super.setFocused(selected);
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            if (this.hasBorder()) {
                fill(matrices, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
                fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            int i = this.editable ? this.editableColor : this.uneditableColor;
            int j = this.selectionStart - this.firstCharacterIndex;
            int k = this.selectionEnd - this.firstCharacterIndex;
            String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && this.focusedTicks / 6 % 2 == 0 && bl;
            int l = this.focused ? this.x + 4 : this.x;
            int m = this.focused ? this.y + (this.height - 8) / 2 : this.y;
            int n = l;
            if (k > string.length()) {
                k = string.length();
            }

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                n = this.textRenderer.drawWithShadow(matrices, this.renderTextProvider.apply(string2, this.firstCharacterIndex), (float) l, (float) m, i);
            }

            boolean bl3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
            int o = n;
            if (!bl) {
                o = j > 0 ? l + this.width : l;
            } else if (bl3) {
                o = n - 1;
                --n;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                this.textRenderer.drawWithShadow(matrices, this.renderTextProvider.apply(string.substring(j), this.selectionStart), (float) n, (float) m, i);
            }

            if (!bl3 && this.suggestion != null) {
                this.textRenderer.drawWithShadow(matrices, this.suggestion, (float) (o - 1), (float) m, -8355712);
            }

            int var10002;
            int var10003;
            if (bl2) {
                if (bl3) {
                    int var10001 = m - 1;
                    var10002 = o + 1;
                    var10003 = m + 1;
                    DrawableHelper.fill(matrices, o, var10001, var10002, var10003 + 9, -3092272);
                } else {
                    this.textRenderer.drawWithShadow(matrices, "_", (float) o, (float) m, i);
                }
            }

            if (k != j) {
                int p = l + this.textRenderer.getWidth(string.substring(0, k));
                var10002 = m - 1;
                var10003 = p - 1;
                int var10004 = m + 1;
                this.drawSelectionHighlight(o, var10002, var10003, var10004 + 9);
            }
            int p = this.active ? 16777215 : 10526880;
            drawCenteredText(matrices, this.textRenderer, this.getMessage(), this.x + this.width / 2, this.y - 15, p | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }

    private void drawSelectionHighlight(int x1, int y1, int x2, int y2) {
        int j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        if (x2 > this.x + this.width) {
            x2 = this.x + this.width;
        }

        if (x1 > this.x + this.width) {
            x1 = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.clearColor(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(x1, y2, 0.0D).next();
        bufferBuilder.vertex(x2, y2, 0.0D).next();
        bufferBuilder.vertex(x2, y1, 0.0D).next();
        bufferBuilder.vertex(x1, y1, 0.0D).next();
        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursor() {
        return this.selectionStart;
    }

    private boolean hasBorder() {
        return this.focused;
    }

    public void setHasBorder(boolean hasBorder) {
        this.focused = hasBorder;
    }

    public void setEditableColor(int color) {
        this.editableColor = color;
    }

    public void setUneditableColor(int color) {
        this.uneditableColor = color;
    }

    public boolean changeFocus(boolean bl) {
        return this.visible && this.editable && super.changeFocus(bl);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double) this.x && mouseX < (double) (this.x + this.width) && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height);
    }

    protected void onFocusedChanged(boolean bl) {
        if (bl) {
            this.focusedTicks = 0;
        }

    }

    private boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public int getInnerWidth() {
        return this.hasBorder() ? this.width - 8 : this.width;
    }

    public void setSelectionEnd(int i) {
        int j = this.text.length();
        this.selectionEnd = MathHelper.clamp(i, 0, j);
        if (this.textRenderer != null) {
            if (this.firstCharacterIndex > j) {
                this.firstCharacterIndex = j;
            }

            int k = this.getInnerWidth();
            String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), k);
            int l = string.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, k, true).length();
            }

            if (this.selectionEnd > l) {
                this.firstCharacterIndex += this.selectionEnd - l;
            } else if (this.selectionEnd <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, j);
        }

    }

    public void setFocusUnlocked(boolean focusUnlocked) {
        this.focusUnlocked = focusUnlocked;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getCharacterX(int index) {
        return index > this.text.length() ? this.x : this.x + this.textRenderer.getWidth(this.text.substring(0, index));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
