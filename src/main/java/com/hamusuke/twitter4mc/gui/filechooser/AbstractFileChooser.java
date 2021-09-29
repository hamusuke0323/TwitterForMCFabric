package com.hamusuke.twitter4mc.gui.filechooser;

import javafx.stage.Stage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class AbstractFileChooser {
    protected final Consumer<File> onChose;
    protected final File initDir;
    protected final AtomicBoolean choosing = new AtomicBoolean();
    protected Stage stage;

    protected AbstractFileChooser(Consumer<File> onChose, File initDir) {
        this.onChose = onChose;
        this.initDir = initDir;
    }

    public void choose() {
        if (!this.choosing.get()) {
            this.choosing.set(true);
            this.onChoose();
        } else {
            //Doesn't work
            this.stage.requestFocus();
        }
    }

    protected abstract void onChoose();
}
