package com.hamusuke.twitter4mc.gui.filechooser;

import javafx.stage.Stage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class AbstractFileChooser {
    protected final Consumer<File> onChose;
    protected boolean choosing;
    protected Stage stage;

    public AbstractFileChooser(Consumer<File> onChose) {
        this.onChose = onChose;
    }

    public void choose() {
        if (!this.choosing) {
            this.choosing = true;
            this.onChoose();
        } else {
            //Doesn't work
            this.stage.requestFocus();
        }
    }

    protected abstract void onChoose();
}
