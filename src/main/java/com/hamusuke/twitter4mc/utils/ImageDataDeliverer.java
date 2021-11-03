package com.hamusuke.twitter4mc.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ImageDataDeliverer {
    private final String url;
    private final MutableObject<InputStream> inputStream = new MutableObject<>();
    private final MutablePair<Integer, Integer> widthHeight = MutablePair.of(0, 0);
    private final MutableBoolean failed = new MutableBoolean();
    private boolean started;

    public ImageDataDeliverer(String url) {
        this.url = url;
        if (this.url == null) {
            this.failed.setTrue();
            this.started = true;
        }
    }

    public void startPreparingAsync(Consumer<Exception> exceptionHandler, Consumer<ImageDataDeliverer> whenCompleteCorrectly) {
        if (!this.started) {
            this.started = true;
            CompletableFuture.supplyAsync(() -> {
                try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new URL(this.url).openStream())) {
                    Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputStream);
                    if (it.hasNext()) {
                        ImageReader imageReader = it.next();
                        imageReader.setInput(imageInputStream);
                        BufferedImage bufferedImage = imageReader.read(0);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, imageReader.getFormatName(), byteArrayOutputStream);
                        byteArrayOutputStream.flush();
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();
                        this.widthHeight.setLeft(bufferedImage.getWidth());
                        this.widthHeight.setRight(bufferedImage.getHeight());
                        return new ByteArrayInputStream(bytes);
                    }
                } catch (IOException e) {
                    exceptionHandler.accept(e);
                    this.failed.setTrue();
                }
                return null;
            }, Executors.newCachedThreadPool(TwitterThread::new)).whenComplete((inputStream, throwable) -> {
                this.inputStream.setValue(inputStream);
                this.failed.setValue(inputStream == null);
                if (this.readyToRender()) {
                    whenCompleteCorrectly.accept(this);
                }
            });
        }
    }

    public ImageDataDeliverer prepareAsync(Consumer<Exception> exceptionHandler, Consumer<ImageDataDeliverer> whenComplete) {
        this.startPreparingAsync(exceptionHandler, whenComplete);
        return this;
    }

    public ImageDataDeliverer prepareAsync() {
        this.startPreparingAsync(ignored -> {
        }, ignored -> {
        });
        return this;
    }

    @Nullable
    public InputStream deliver() {
        return this.inputStream.getValue();
    }

    public int getWidth() {
        return this.widthHeight.getLeft();
    }

    public int getHeight() {
        return this.widthHeight.getRight();
    }

    public boolean readyToRender() {
        return this.deliver() != null && !this.failed() && this.getWidth() != 0 && this.getHeight() != 0;
    }

    public boolean failed() {
        return this.failed.booleanValue();
    }
}
