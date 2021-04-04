package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import twitter4j.User;

//TODO
@Environment(EnvType.CLIENT)
public class TwitterShowUserScreen extends Screen {
	private final User user;

	protected TwitterShowUserScreen(User user) {
		super(NarratorManager.EMPTY);
		this.user = user;
	}
}
