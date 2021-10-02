package com.hamusuke.twitter4mc.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class ParentalScreen extends Screen {
    @Nullable
    protected final Screen parent;

    public ParentalScreen(Text title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected void init() {
        if (this.parent != null) {
            this.parent.resize(this.client, this.width, this.height);
        }

        super.init();
    }

    public void onClose() {
        this.client.setScreen(this.parent);
    }
}
