package com.hamusuke.twitter4mc.gui.screen.filechoosing;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FileChoosingScreen extends Screen {
    private static final TranslatableText OPEN_FILE = new TranslatableText("tw.open.file");
    private static final TranslatableText NAME_AND_SAVE = new TranslatableText("tw.save.file");
    protected final Mode mode;
    protected final boolean multipleSelectable;
    protected final List<File> currentSelectedFile = Lists.newArrayList();
    protected final List<File> currentlyShowing = Lists.newArrayList();
    @Nullable
    protected File currentDir;
    protected final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public FileChoosingScreen(@Nullable File currentDir, @Nullable File current, Mode mode, boolean multipleSelectable) {
        super(mode.text);
        this.currentDir = currentDir;
        this.currentSelectedFile.clear();
        if (current != null) {
            this.currentSelectedFile.add(current);
        }
        this.mode = mode;
        this.multipleSelectable = this.mode != Mode.SAVE && multipleSelectable;
    }

    public static FileChoosingScreen createOpen(@Nullable File currentDir, boolean multipleSelectable) {
        return new FileChoosingScreen(currentDir, null, Mode.OPEN, multipleSelectable);
    }

    public static FileChoosingScreen createSave(@Nullable File currentDir, @Nullable File current) {
        return new FileChoosingScreen(currentDir, current, Mode.SAVE, false);
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

    @Environment(EnvType.CLIENT)
    public enum Mode {
        OPEN(OPEN_FILE),
        SAVE(NAME_AND_SAVE);

        private final Text text;

        Mode(Text text) {
            this.text = text;
        }

        public Text getText() {
            return this.text;
        }
    }

    @Environment(EnvType.CLIENT)
    class FileList extends ElementListWidget<FileList.FileEntry> {
        public FileList() {
            super(FileChoosingScreen.this.client, FileChoosingScreen.this.width, FileChoosingScreen.this.height, 15, FileChoosingScreen.this.height - 20, 10);


        }


        @Environment(EnvType.CLIENT)
        static class FileEntry extends ElementListWidget.Entry<FileEntry> {

            @Override
            public List<? extends Selectable> selectableChildren() {
                return null;
            }

            @Override
            public List<? extends Element> children() {
                return null;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            }
        }
    }
}
