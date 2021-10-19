package com.hamusuke.twitter4mc.gui.filechooser;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.swing.*;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class AbstractFileChooser {
    protected final Consumer<File> onChose;
    protected final File initDir;
    protected final AtomicBoolean choosing = new AtomicBoolean();
    protected final AtomicReference<JFileChooser> jFileChooser = new AtomicReference<>();

    protected AbstractFileChooser(Consumer<File> onChose, File initDir) {
        this.onChose = onChose;
        this.initDir = initDir;
    }

    public void choose() {
        if (!this.choosing.get()) {
            this.choosing.set(true);
            this.startChoosing();
        } else {
            this.getJFileChooser().ifPresent(JComponent::requestFocus);
        }
    }

    protected abstract void startChoosing();

    protected Optional<JFileChooser> getJFileChooser() {
        return Optional.ofNullable(this.jFileChooser.get());
    }
}
