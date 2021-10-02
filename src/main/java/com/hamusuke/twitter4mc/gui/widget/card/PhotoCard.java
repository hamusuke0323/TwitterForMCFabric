package com.hamusuke.twitter4mc.gui.widget.card;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.widget.list.TweetElement;
import com.hamusuke.twitter4mc.texture.TextureManager;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

import java.util.List;

//TODO
@Environment(EnvType.CLIENT)
public class PhotoCard extends AbstractButtonWidget implements TweetElement {
    private final List<TwitterPhotoMedia> photos;

    public PhotoCard(List<TwitterPhotoMedia> photos, int x, int y, int width, int height) {
        super(x, y, width, height, "");
        this.photos = photos;
    }

    public void renderButton(int mouseX, int mouseY, float delta) {
        TextureManager textureManager = TwitterForMC.getTextureManager();

    }

    public void setHeight(int height) {

    }

    public int getHeight() {
        return 0;
    }

    public int getY() {
        return 0;
    }

    public void setY(int y) {

    }
}
