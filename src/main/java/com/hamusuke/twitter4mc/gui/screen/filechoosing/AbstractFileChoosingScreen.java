package com.hamusuke.twitter4mc.gui.screen.filechoosing;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Environment(EnvType.CLIENT)
public abstract class AbstractFileChoosingScreen extends Screen {
    protected static final TranslatableText OPEN = new TranslatableText("tw.open.file");
    protected static final TranslatableText SAVE = new TranslatableText("tw.save.file");
    protected final Mode mode;
    protected final boolean multipleSelectable;
    @Nullable
    protected File currentSelectedFile;

    public AbstractFileChoosingScreen(@Nullable File current, Mode mode, boolean multipleSelectable) {
        super(mode == Mode.OPEN ? OPEN : SAVE);
        this.currentSelectedFile = current;
        this.mode = mode;
        this.multipleSelectable = multipleSelectable;
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {


        super.render(matrices, mouseX, mouseY, delta);
    }

    protected void update() {

    }

    public enum Mode {
        OPEN,
        SAVE
    }
}
