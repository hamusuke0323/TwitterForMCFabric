package com.hamusuke.twitter4mc.gui.widget.list;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.hamusuke.twitter4mc.gui.widget.list.entry.TwitterListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractTwitterTweetList<E extends AbstractTwitterTweetList.AbstractTwitterListEntry<E>> extends AbstractParentElement implements Drawable {
	protected final MinecraftClient minecraft;
	private int averageHeight;
	private final SimpleArrayList children = new SimpleArrayList();
	protected int width;
	protected int height;
	protected int top;
	protected int bottom;
	protected int rightpos;
	protected int leftpos;
	protected int yDrag = -2;
	private double scrollAmount;
	protected boolean renderSelection = true;
	protected boolean renderHeader;
	protected int headerHeight;
	private boolean scrolling;
	private E selected;
	private int allHeight;

	public AbstractTwitterTweetList(MinecraftClient mcIn, int width, int height, int top, int bottom) {
		this.minecraft = mcIn;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.leftpos = 0;
		this.rightpos = width;
	}

	public void setRenderSelection(boolean p_setRenderSelection_1_) {
		this.renderSelection = p_setRenderSelection_1_;
	}

	protected void setRenderHeader(boolean p_setRenderHeader_1_, int p_setRenderHeader_2_) {
		this.renderHeader = p_setRenderHeader_1_;
		this.headerHeight = p_setRenderHeader_2_;

		if (!p_setRenderHeader_1_) {
			this.headerHeight = 0;
		}
	}

	public void tick() {
	}

	public int getRowWidth() {
		return 220;
	}

	@Nullable
	public E getSelected() {
		return this.selected;
	}

	public void setSelected(@Nullable E entry) {
		this.selected = entry;
	}

	@Nullable
	public E getFocused() {
		return (E) (super.getFocused());
	}

	public List<E> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.forEach(E::onRemove);
		this.children.clear();
		this.allHeight = 0;
	}

	protected void replaceEntries(Collection<E> p_replaceEntries_1_) {
		this.children.forEach(E::onRemove);
		this.children.clear();
		p_replaceEntries_1_.forEach(E::init);
		this.children.addAll(p_replaceEntries_1_);
		this.calcAllHeight();
		this.calcAverage();
	}

	protected E getEntry(int p_getEntry_1_) {
		return this.children().get(p_getEntry_1_);
	}

	public int addEntry(E p_addEntry_1_) {
		p_addEntry_1_.init();
		this.children.add(p_addEntry_1_);
		this.allHeight += p_addEntry_1_.getHeight();
		this.calcAverage();
		this.setY(0);
		return this.children.size() - 1;
	}

	protected int getItemCount() {
		return this.children().size();
	}

	protected boolean isSelectedItem(int p_isSelectedItem_1_) {
		return Objects.equals(this.getSelected(), this.children().get(p_isSelectedItem_1_));
	}

	@Nullable
	protected final E getEntryAtPosition(double x, double y) {
		int i = this.getRowWidth() / 2;
		int j = this.leftpos + this.width / 2;
		int k = j - i;
		int l = j + i;
		boolean flag = x < (double) this.getScrollbarPosition() && x >= (double) k && x <= (double) l;
		if (flag) {
			for (E e : this.children()) {
				int ey = e.getY();
				int eyheight = ey + e.getHeight();
				if (ey <= (int) y && eyheight >= (int) y) {
					return e;
				}
			}
		}

		return null;
	}

	public void updateSize(int width, int height, int top, int bottom) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.leftpos = 0;
		this.rightpos = width;
	}

	public void setLeftPos(int left) {
		this.leftpos = left;
		this.rightpos = left + this.width;
	}

	public void setLeftRightPos(int left, int right) {
		this.leftpos = left;
		this.rightpos = right;
	}

	protected int getMaxPosition() {
		return this.allHeight + this.headerHeight;
	}

	protected void clickedHeader(int p_clickedHeader_1_, int p_clickedHeader_2_) {
	}

	protected void renderHeader(int p_renderHeader_1_, int p_renderHeader_2_, Tessellator p_renderHeader_3_) {
	}

	protected void renderBackground() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.minecraft.getTextureManager().bindTexture(DrawableHelper.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferbuilder.vertex(this.leftpos, this.bottom, 0.0D).texture((float) this.leftpos / 32.0F, (float) (this.bottom + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
		bufferbuilder.vertex(this.rightpos, this.bottom, 0.0D).texture((float) this.rightpos / 32.0F, (float) (this.bottom + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
		bufferbuilder.vertex(this.rightpos, this.top, 0.0D).texture((float) this.rightpos / 32.0F, (float) (this.top + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
		bufferbuilder.vertex(this.leftpos, this.top, 0.0D).texture((float) this.leftpos / 32.0F, (float) (this.top + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).next();
		tessellator.draw();
	}

	protected void renderDecorations(int p_renderDecorations_1_, int p_renderDecorations_2_) {
	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground();
		int i = this.getScrollbarPosition();
		int j = i + 6;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		int k = this.getRowLeft();
		int l = this.top + 4 - (int) this.getScrollAmount();

		if (this.renderHeader) {
			this.renderHeader(k, l, tessellator);
		}

		this.renderList(k, l, p_render_1_, p_render_2_, p_render_3_);
		RenderSystem.disableDepthTest();
		this.renderHoleBackground(0, this.top, 255, 255);
		this.renderHoleBackground(this.bottom, this.height, 255, 255);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.disableTexture();

		bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferbuilder.vertex(this.leftpos, this.top + 4, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
		bufferbuilder.vertex(this.rightpos, this.top + 4, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
		bufferbuilder.vertex(this.rightpos, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
		bufferbuilder.vertex(this.leftpos, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
		tessellator.draw();
		bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferbuilder.vertex(this.leftpos, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
		bufferbuilder.vertex(this.rightpos, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
		bufferbuilder.vertex(this.rightpos, this.bottom - 4, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 0).next();
		bufferbuilder.vertex(this.leftpos, this.bottom - 4, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 0).next();
		tessellator.draw();
		int j1 = this.getMaxScroll();

		if (j1 > 0) {
			int k1 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
			k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
			int l1 = (int) this.getScrollAmount() * (this.bottom - this.top - k1) / j1 + this.top;

			if (l1 < this.top) {
				l1 = this.top;
			}

			bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferbuilder.vertex(i, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
			bufferbuilder.vertex(j, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
			bufferbuilder.vertex(j, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
			bufferbuilder.vertex(i, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
			tessellator.draw();
			bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferbuilder.vertex(i, l1 + k1, 0.0D).texture(0.0F, 1.0F).color(128, 128, 128, 255).next();
			bufferbuilder.vertex(j, l1 + k1, 0.0D).texture(1.0F, 1.0F).color(128, 128, 128, 255).next();
			bufferbuilder.vertex(j, l1, 0.0D).texture(1.0F, 0.0F).color(128, 128, 128, 255).next();
			bufferbuilder.vertex(i, l1, 0.0D).texture(0.0F, 0.0F).color(128, 128, 128, 255).next();
			tessellator.draw();
			bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferbuilder.vertex(i, l1 + k1 - 1, 0.0D).texture(0.0F, 1.0F).color(192, 192, 192, 255).next();
			bufferbuilder.vertex(j - 1, l1 + k1 - 1, 0.0D).texture(1.0F, 1.0F).color(192, 192, 192, 255).next();
			bufferbuilder.vertex(j - 1, l1, 0.0D).texture(1.0F, 0.0F).color(192, 192, 192, 255).next();
			bufferbuilder.vertex(i, l1, 0.0D).texture(0.0F, 0.0F).color(192, 192, 192, 255).next();
			tessellator.draw();
		}

		this.renderDecorations(p_render_1_, p_render_2_);
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
	}

	protected void centerScrollOn(E p_centerScrollOn_1_) {
		this.setScrollAmount(this.countBefore(this.children().indexOf(p_centerScrollOn_1_)) + (float) (p_centerScrollOn_1_.getHeight() / 2 - (this.bottom - this.top) / 2));
	}

	protected void ensureVisible(E entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.top - 4 - entry.getHeight();

		if (j < 0) {
			this.scroll(j);
		}

		int k = this.bottom - i - (entry.getHeight() * 2);

		if (k < 0) {
			this.scroll(-k);
		}
	}

	private void scroll(int p_scroll_1_) {
		this.setScrollAmount(this.getScrollAmount() + (double) p_scroll_1_);
		this.yDrag = -2;
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double p_setScrollAmount_1_) {
		this.scrollAmount = MathHelper.clamp(p_setScrollAmount_1_, 0.0D, this.getMaxScroll());
		this.setY(-(int) this.scrollAmount);
	}

	protected int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
	}

	public int getScrollBottom() {
		return (int) this.getScrollAmount() - this.height - this.headerHeight;
	}

	protected void updateScrollingState(double mouseX, double mouseY, int mouseButton) {
		this.scrolling = mouseButton == 0 && mouseX >= (double) this.getScrollbarPosition() && mouseX < (double) (this.getScrollbarPosition() + 6);
	}

	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}

	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);

		if (!this.isMouseOver(p_mouseClicked_1_, p_mouseClicked_3_)) {
			return false;
		} else {
			E e = this.getEntryAtPosition(p_mouseClicked_1_, p_mouseClicked_3_);

			if (e != null) {
				if (e.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
					this.setFocused(e);
					this.setDragging(true);
					return true;
				}
			} else if (p_mouseClicked_5_ == 0) {
				this.clickedHeader((int) (p_mouseClicked_1_ - (double) (this.leftpos + this.width / 2 - this.getRowWidth() / 2)), (int) (p_mouseClicked_3_ - (double) this.top) + (int) this.getScrollAmount() - 4);
				return true;
			}

			return this.scrolling;
		}
	}

	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
		if (this.getFocused() != null) {
			this.getFocused().mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
		}

		return false;
	}

	public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
		if (super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_)) {
			return true;
		} else if (p_mouseDragged_5_ == 0 && this.scrolling) {
			if (p_mouseDragged_3_ < (double) this.top) {
				this.setScrollAmount(0.0D);
			} else if (p_mouseDragged_3_ > (double) this.bottom) {
				this.setScrollAmount(this.getMaxScroll());
			} else {
				double d0 = Math.max(1, this.getMaxScroll());
				int i = this.bottom - this.top;
				int j = MathHelper.clamp((int) ((float) (i * i) / (float) this.getMaxPosition()), 32, i - 8);
				double d1 = Math.max(1.0D, d0 / (double) (i - j));
				this.setScrollAmount(this.getScrollAmount() + p_mouseDragged_8_ * d1);
			}

			return true;
		} else {
			return false;
		}
	}

	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
		this.setScrollAmount(this.getScrollAmount() - p_mouseScrolled_5_ * (double) this.averageHeight / 2.0D);
		return true;
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
			return true;
		} else if (p_keyPressed_1_ == 264) {
			this.moveSelection(1);
			return true;
		} else if (p_keyPressed_1_ == 265) {
			this.moveSelection(-1);
			return true;
		} else {
			return false;
		}
	}

	protected void moveSelection(int inc) {
		if (!this.children().isEmpty()) {
			int i = this.children().indexOf(this.getSelected());
			int j = MathHelper.clamp(i + inc, 0, this.getItemCount() - 1);
			E e = this.children().get(j);
			this.setSelected(e);
		}
	}

	public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
		return p_isMouseOver_3_ >= (double) this.top && p_isMouseOver_3_ <= (double) this.bottom && p_isMouseOver_1_ >= (double) this.leftpos && p_isMouseOver_1_ <= (double) this.rightpos;
	}

	protected void renderList(int p_renderList_1_, int p_renderList_2_, int p_renderList_3_, int p_renderList_4_, float p_renderList_5_) {
		int i = this.getItemCount();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		for (int j = 0; j < i; ++j) {
			int k = this.getRowTop(j);
			int l = this.getRowBottom(j);

			if (l >= this.top && k <= this.bottom) {
				E e = this.getEntry(j);
				int i1 = p_renderList_2_ + this.countBefore(j) + this.headerHeight;
				int j1 = e.getHeight() - 4;
				int k1 = this.getRowWidth();

				if (this.renderSelection && this.isSelectedItem(j)) {
					int l1 = this.leftpos + this.width / 2 - k1 / 2;
					int i2 = this.leftpos + this.width / 2 + k1 / 2;
					RenderSystem.disableTexture();
					float f = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.color4f(f, f, f, 1.0F);
					bufferbuilder.begin(7, VertexFormats.POSITION);
					bufferbuilder.vertex(l1, i1 + j1 + 2, 0.0D).next();
					bufferbuilder.vertex(i2, i1 + j1 + 2, 0.0D).next();
					bufferbuilder.vertex(i2, i1 - 2, 0.0D).next();
					bufferbuilder.vertex(l1, i1 - 2, 0.0D).next();
					tessellator.draw();
					RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					bufferbuilder.begin(7, VertexFormats.POSITION);
					bufferbuilder.vertex(l1 + 1, i1 + j1 + 1, 0.0D).next();
					bufferbuilder.vertex(i2 - 1, i1 + j1 + 1, 0.0D).next();
					bufferbuilder.vertex(i2 - 1, i1 - 1, 0.0D).next();
					bufferbuilder.vertex(l1 + 1, i1 - 1, 0.0D).next();
					tessellator.draw();
					RenderSystem.enableTexture();
				}

				int j2 = this.getRowLeft();
				e.render(j, k, j2, k1, j1, p_renderList_3_, p_renderList_4_, this.isMouseOver(p_renderList_3_, p_renderList_4_) && Objects.equals(this.getEntryAtPosition(p_renderList_3_, p_renderList_4_), e), p_renderList_5_);
			}
		}
	}

	protected int getRowLeft() {
		return this.leftpos + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	protected int getRowTop(int p_getRowTop_1_) {
		return this.top + 4 - (int) this.getScrollAmount() + this.countBefore(p_getRowTop_1_) + this.headerHeight;
	}

	protected int getRowBottom(int p_getRowBottom_1_) {
		return this.getRowTop(p_getRowBottom_1_) + this.children().get(p_getRowBottom_1_).getHeight();
	}

	protected boolean isFocused() {
		return false;
	}

	protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.minecraft.getTextureManager().bindTexture(DrawableHelper.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		bufferbuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferbuilder.vertex(this.leftpos, p_renderHoleBackground_2_, 0.0D).texture(0.0F, (float) p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).next();
		bufferbuilder.vertex(this.leftpos + this.width, p_renderHoleBackground_2_, 0.0D).texture((float) this.width / 32.0F, (float) p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).next();
		bufferbuilder.vertex(this.leftpos + this.width, p_renderHoleBackground_1_, 0.0D).texture((float) this.width / 32.0F, (float) p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).next();
		bufferbuilder.vertex(this.leftpos, p_renderHoleBackground_1_, 0.0D).texture(0.0F, (float) p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).next();
		tessellator.draw();
	}

	protected E remove(int p_remove_1_) {
		E e = this.children.get(p_remove_1_);
		return this.removeEntry(this.children.get(p_remove_1_)) ? e : null;
	}

	protected boolean removeEntry(E p_removeEntry_1_) {
		boolean flag = this.children.remove(p_removeEntry_1_);

		if (flag) {
			this.allHeight -= p_removeEntry_1_.getHeight();
			if (p_removeEntry_1_ == this.getSelected()) {
				this.setSelected(null);
			}
			p_removeEntry_1_.onRemove();
		}

		return flag;
	}

	protected int countBefore(int index) {
		int j = 0;
		for (int i = 0; i < index; i++) {
			j += this.children().get(i).getHeight();
		}
		return j;
	}

	protected void calcAllHeight() {
		this.allHeight = 0;
		for (E e : this.children()) {
			this.allHeight += e.getHeight();
		}
	}

	protected void calcAverage() {
		this.averageHeight = this.allHeight / this.children().size();
	}

	protected void setY(int topY) {
		for (int i = 0; i < this.children().size(); i++) {
			this.children().get(i).setY(this.top + topY + this.countBefore(i));
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class AbstractTwitterListEntry<E extends AbstractTwitterTweetList.AbstractTwitterListEntry<E>> implements TwitterListEntry {
		@Deprecated
		AbstractTwitterTweetList<E> list;
		public final List<AbstractButtonWidget> buttons = Lists.newArrayList();
		public final List<Element> children = Lists.newArrayList();

		public void init() {
		}

		public void tick() {
		}

		public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {

			for (AbstractButtonWidget button : this.buttons) {
				button.render(mouseX, mouseY, p_render_9_);
			}
		}

		public void onRemove() {
		}

		public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
			return Objects.equals(this.list.getEntryAtPosition(p_isMouseOver_1_, p_isMouseOver_3_), this);
		}

		protected <T extends AbstractButtonWidget> T addButton(T widget) {
			this.buttons.add(widget);
			this.children.add(widget);
			return widget;
		}
	}

	@Environment(EnvType.CLIENT)
	class SimpleArrayList extends AbstractList<E> {
		private final List<E> field_216871_b = Lists.newArrayList();

		private SimpleArrayList() {
		}

		public E get(int p_get_1_) {
			return this.field_216871_b.get(p_get_1_);
		}

		public int size() {
			return this.field_216871_b.size();
		}

		public E set(int p_set_1_, E p_set_2_) {
			E e = this.field_216871_b.set(p_set_1_, p_set_2_);
			p_set_2_.list = AbstractTwitterTweetList.this;
			return e;
		}

		public void add(int p_add_1_, E p_add_2_) {
			this.field_216871_b.add(p_add_1_, p_add_2_);
			p_add_2_.list = AbstractTwitterTweetList.this;
		}

		public E remove(int p_remove_1_) {
			return this.field_216871_b.remove(p_remove_1_);
		}
	}
}
