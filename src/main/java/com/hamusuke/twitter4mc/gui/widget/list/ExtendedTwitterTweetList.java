package com.hamusuke.twitter4mc.gui.widget.list;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ExtendedTwitterTweetList<E extends AbstractTwitterTweetList.AbstractTwitterListEntry<E>> extends AbstractTwitterTweetList<E> {
	private boolean inFocus;

	public ExtendedTwitterTweetList(MinecraftClient mcIn, int width, int height, int top, int bottom) {
		super(mcIn, width, height, top, bottom);
	}

    @Override
    public boolean changeFocus(boolean p_changeFocus_1_) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return false;
        } else {
            this.inFocus = !this.inFocus;
            if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
                this.moveSelection(1);
            } else if (this.inFocus && this.getSelected() != null) {
                this.moveSelection(0);
            }
            return this.inFocus;
        }
    }

	@Environment(EnvType.CLIENT)
	public abstract static class AbstractTwitterListEntry<E extends ExtendedTwitterTweetList.AbstractTwitterListEntry<E>> extends AbstractTwitterTweetList.AbstractTwitterListEntry<E> {
        @Override
        public boolean changeFocus(boolean p_changeFocus_1_) {
            return false;
        }
	}
}
