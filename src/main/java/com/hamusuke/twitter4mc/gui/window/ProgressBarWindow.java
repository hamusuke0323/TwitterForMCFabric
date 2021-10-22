package com.hamusuke.twitter4mc.gui.window;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
public class ProgressBarWindow extends JFrame {
    private final JProgressBar bar;
    private final JLabel label;
    private static final BiFunction<Integer, Pair<Integer, Integer>, String> FUNCTION = (progress, count$size) -> String.format("Importing Timeline - %d%% completed. (%d/%d)", progress, count$size.getLeft(), count$size.getRight());
    private final int size;
    private final MutableInt count = new MutableInt();

    public ProgressBarWindow(int size) {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Twitter for MC Worker");
        this.size = size;
        this.bar = new JProgressBar();
        this.bar.setValue(0);
        JToolBar jToolBar = new JToolBar();
        jToolBar.setFloatable(false);
        jToolBar.add(this.bar);
        this.label = new JLabel(FUNCTION.apply(0, Pair.of(0, this.size)));
        JPanel labelPanel = new JPanel();
        labelPanel.add(this.label);
        getContentPane().add(jToolBar, BorderLayout.CENTER);
        getContentPane().add(labelPanel, BorderLayout.PAGE_END);
        this.pack();
        this.setSize(400, this.getHeight());
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void increment() {
        this.count.increment();
        int progress = (int) ((this.count.floatValue() / (float) this.size) * 100.0F);
        this.bar.setValue(progress);
        this.label.setText(FUNCTION.apply(progress, Pair.of(this.count.getValue(), this.size)));
    }
}
