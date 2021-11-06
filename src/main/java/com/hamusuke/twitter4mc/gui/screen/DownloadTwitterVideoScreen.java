package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.download.FileDownload;
import com.hamusuke.twitter4mc.gui.filechooser.FileChooserSave;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class DownloadTwitterVideoScreen extends ParentalScreen {
    private final String videoUrl;
    private final MutableObject<File> saveTo = new MutableObject<>();
    private final FileDownload fileDownload;
    private final MutableBoolean started = new MutableBoolean();
    private final FileChooserSave fileChooserSave;
    private String progress = "";
    private int tickCount;
    private long previousWrittenBytes;
    private long previousTime;
    private long bytesPerSecond;

    public DownloadTwitterVideoScreen(@Nullable Screen parent, TweetSummary tweetSummary) {
        super(new TranslatableText("tw.video.download.title"), parent);
        this.saveTo.setValue(SystemUtils.getUserHome().toPath().resolve("twitter_status_id_" + tweetSummary.getId() + "_video.mp4").toFile());
        this.fileChooserSave = new FileChooserSave(file -> {
            if (file == null) {
                return;
            }

            this.saveTo.setValue(file);
        }, this.saveTo.getValue());
        this.videoUrl = tweetSummary.getVideoURL();
        this.fileDownload = new FileDownload(this.videoUrl);
    }

    protected void init() {
        this.addDrawableChild(new ButtonWidget(0, this.height - 20, this.width / 2, 20, new TranslatableText("tw.select.file"), button -> {
            this.fileChooserSave.choose();
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2, this.height - 20, this.width / 2, 20, new TranslatableText("tw.video.download"), button -> {
            button.active = false;
            this.download();
        }));

        super.init();
    }

    public void tick() {
        this.tickCount++;
        super.tick();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.parent != null) {
            this.parent.render(matrices, -1, -1, delta);
        }

        if (this.client.currentScreen == this) {
            this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            return;
        }

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
        drawCenteredText(matrices, this.textRenderer, "URL:" + this.videoUrl, this.width / 2, 40, 16777215);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("selectWorld.resultFolder").append(" ").append(this.saveTo.getValue().getAbsolutePath()), this.width / 2, 50, 16777215);
        if (this.fileDownload.bytesWritten() != 0L && !this.fileDownload.cancelled()) {
            this.renderProgressBar(matrices);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderProgressBar(MatrixStack matrices) {
        double d = Math.min((double) this.fileDownload.bytesWritten() / (double) this.fileDownload.totalBytes(), 1.0D);
        this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0D);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        double e = (double) this.width / 4;
        double y = this.height - 20 - 15;
        bufferBuilder.vertex(e - 0.5D, y + 10.5D, 0.0D).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(e + (double) this.width / 2 + 10.5D, y + 0.5D, 0.0D).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(e + (double) this.width / 2 + 0.5D, y + 10.5D, 0.0D).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(e - 0.5D, y + 0.5D, 0.0D).color(255, 255, 255, 255).next();
        bufferBuilder.vertex(e, y + 10.0D, 0.0D).color(0, 255, 0, 255).next();
        bufferBuilder.vertex(e + (double) this.width / 2 * d, y + 10.0D, 0.0D).color(0, 255, 0, 255).next();
        bufferBuilder.vertex(e + (double) this.width / 2 * d, y, 0.0D).color(0, 255, 0, 255).next();
        bufferBuilder.vertex(e, y, 0.0D).color(0, 255, 0, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        drawCenteredText(matrices, this.textRenderer, this.progress + " % " + this.updateAndReturnDownloadSpeedString(), this.width / 2, (int) (y - 10.0D), 16777215);
    }

    private String updateAndReturnDownloadSpeedString() {
        if (!this.fileDownload.finished() && this.tickCount % 20 == 0) {
            long l = MathHelper.clamp(Util.getMeasuringTimeMs() - this.previousTime, 1L, Long.MAX_VALUE);
            this.bytesPerSecond = 1000L * (this.fileDownload.bytesWritten() - this.previousWrittenBytes) / l;
            this.previousWrittenBytes = this.fileDownload.bytesWritten();
            this.previousTime = Util.getMeasuringTimeMs();
        }

        return this.getDownloadSpeedString();
    }

    private String getDownloadSpeedString() {
        if (this.bytesPerSecond > 0L) {
            return "(" + SizeUnit.getUserFriendlyString(this.bytesPerSecond) + "/s)";
        }

        return "";
    }

    private synchronized void download() {
        if (this.saveTo.getValue() != null && this.started.isFalse()) {
            this.started.setTrue();
            this.fileDownload.download(this.saveTo.getValue());
        }
    }

    public FileDownload getFileDownload() {
        return this.fileDownload;
    }
}
