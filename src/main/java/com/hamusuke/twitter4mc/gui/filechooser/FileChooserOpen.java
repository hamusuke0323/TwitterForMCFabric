package com.hamusuke.twitter4mc.gui.filechooser;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FileChooserOpen extends AbstractFileChooser {
    public FileChooserOpen(Consumer<File> onChose, File initDir) {
        super(onChose, initDir);
    }

    protected void startChoosing() {
        SwingUtilities.invokeLater(() -> {
            JFrame jFrame = new JFrame();
            this.jFrame.set(jFrame);
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setCurrentDirectory(this.initDir);
            if (jFileChooser.showOpenDialog(jFrame) == JFileChooser.APPROVE_OPTION) {
                this.onChose.accept(jFileChooser.getSelectedFile());
            }
            jFrame.dispose();
            this.jFrame.set(null);
            this.choosing.set(false);
        });
    }
}
