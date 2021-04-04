package com.hamusuke.twitter4mc.gui.widget;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.google.common.collect.Lists;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

//TODO
@Environment(EnvType.CLIENT)
public class TwitterTweetFieldWidget extends AbstractButtonWidget implements Drawable, Element {
	private final TextRenderer fontRenderer;
	private String text = "";
	private List<String> formatted = Lists.newArrayList();
	private int bytes;
	private static final String ENCODING = "Shift_JIS";
	private int maxTextLength = 280;
	public boolean isEnabled = true;
	public boolean enableBackgroundDrawing = true;
	private int cursorX;
	private int cursorY;
	private int enabledColor = 14737632;
	private int disabledColor = 7368816;

	public TwitterTweetFieldWidget(TextRenderer msg, int x, int y, int width, int height, String message) {
		this(msg, x, y, width, height, null, message);
	}

	public TwitterTweetFieldWidget(TextRenderer fontRenderer, int x, int y, int width, int height, @Nullable TwitterTweetFieldWidget p_i1956_6_, String message) {
		super(x, y, width, height, message);
		this.fontRenderer = fontRenderer;

		if (p_i1956_6_ != null) {
			this.setText(p_i1956_6_.getText());
		}
	}

	public void setText(String text) {
		this.text = text;
		this.onTextChanged(this.text);
	}

	public String getText() {
		return this.text;
	}

	public int getTextBytes() {
		return this.bytes;
	}

	private void setByte(String text) {
		try {
			this.bytes = this.text.getBytes(ENCODING).length;
		} catch (UnsupportedEncodingException e) {
			;
		}
	}

	public boolean isOverLength() {
		return this.bytes > this.maxTextLength;
	}

	private void onTextChanged(String text) {
		this.setByte(this.text);
		this.formatted = this.fontRenderer.wrapStringToWidthAsList(this.text, this.width - 8);
	}

	public int getMaxTextLength() {
		return this.maxTextLength;
	}

	public void setMaxTextLength(int length) {
		this.maxTextLength = length;
	}

	public boolean canWrite() {
		return this.visible && this.isFocused() && this.isEnabled;
	}

	public void writeText(String textToWrite) {
		String s = this.text.substring(0, this.cursorX);
		String s1 = this.text.substring(this.cursorX);
		int i = this.formatted.size();
		this.setText(s + textToWrite + s1);
		int j = this.formatted.size();
		boolean flag = i < j;
		this.clampCursorX(this.cursorX + textToWrite.length());
	}

	private void incCursorX() {
		this.clampCursorX(this.cursorX++);
	}

	private void decCursorX() {
		this.clampCursorX(this.cursorX--);
	}

	public void clampCursorX(int pos) {
		this.cursorX = MathHelper.clamp(pos, 0, this.text.length());
	}

	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		if (this.visible) {
			if (this.enableBackgroundDrawing) {
				fill(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
				fill(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
			}

			boolean flag = this.isFocused();
			boolean flag1 = this.cursorX < this.text.length();
			int i = this.isEnabled ? this.enabledColor : this.disabledColor;
			int l = this.enableBackgroundDrawing ? this.x + 4 : this.x;
			int i1 = this.enableBackgroundDrawing ? this.y + 4 : this.y;
			int j1 = l;

			if (flag) {
				if (flag1) {
					//AbstractGui.fill(k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
				} else {
					//this.fontRenderer.drawStringWithShadow("_", (float)k1, (float)i1, i);
				}
			}
		}
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
}
