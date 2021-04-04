package com.hamusuke.twitter4mc.gui.widget.list.entry;

import net.minecraft.client.gui.Element;

public interface ITwitterListEntry extends Element {
	int getHeight();
	int getY();
	void setY(int y);
}
