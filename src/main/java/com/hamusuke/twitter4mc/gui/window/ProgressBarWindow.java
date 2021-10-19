package com.hamusuke.twitter4mc.gui.window;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ProgressBarWindow extends JFrame {
    private final JProgressBar bar;
    private final JLabel label;
    private static final Function<Integer, String> FUNCTION = integer -> String.format("Importing Timeline - %s%% completed", integer);
    private final MutableInt progress = new MutableInt();

    public ProgressBarWindow() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Twitter for MC Worker");

        this.bar = new JProgressBar();
        this.bar.setValue(0);
        JToolBar jToolBar = new JToolBar();
        jToolBar.setFloatable(false);
        jToolBar.add(this.bar);

        this.label = new JLabel(FUNCTION.apply(0));
        JPanel labelPanel = new JPanel();
        labelPanel.add(this.label);

        getContentPane().add(jToolBar, BorderLayout.CENTER);
        getContentPane().add(labelPanel, BorderLayout.PAGE_END);

        this.pack();
        this.setSize(400, this.getHeight());
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void setProgress(int progress) {
        this.progress.setValue(progress);
        this.bar.setValue(progress);
        this.label.setText(FUNCTION.apply(progress));

        if (progress >= this.bar.getMaximum()) {
            this.dispose();
        }
    }
}
