package com.hamusuke.twitter4mc.gui.widget.list;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;

@Environment(EnvType.CLIENT)
public interface TweetElement extends Element {
	void setHeight(int height);

	int getHeight();

	int getY();

	void setY(int y);
}
