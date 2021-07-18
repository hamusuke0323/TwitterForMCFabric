package com.hamusuke.twitter4mc.gui.card;

import net.minecraft.client.gui.Element;

public interface TwitterTweetCardElement extends Element {
    void renderCard(int cardLeft, int cardTop, int mouseX, int mouseY, float delta);
}
