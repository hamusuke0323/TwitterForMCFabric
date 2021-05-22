package com.hamusuke.twitter4mc.gui.filechooser;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SFileChooser {
    private final Consumer<File> onChose;

    public SFileChooser(Consumer<File> onChose) {
        this.onChose = onChose;
    }

    public void choose() {
        Platform.runLater(() -> this.onChose.accept(new FileChooser().showOpenDialog(new Stage())));
    }
}
