package com.hamusuke.twitter4mc.gui.widget.list.entry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;

@Environment(EnvType.CLIENT)
public interface TwitterListEntry extends Element {
	int getHeight();

	int getY();

	void setY(int y);
}
