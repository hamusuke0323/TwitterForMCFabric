package com.hamusuke.twitter4mc.gui.filechooser;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FileChooserOpen extends AbstractFileChooser {
    public FileChooserOpen(Consumer<File> onChose) {
        super(onChose);
    }

    protected void onChoose() {
        Platform.runLater(() -> {
            this.stage = new Stage();
            this.onChose.accept(new FileChooser().showOpenDialog(this.stage));
            this.choosing.set(false);
        });
    }
}
