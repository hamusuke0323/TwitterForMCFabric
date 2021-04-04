package com.hamusuke.twitter4mc.photomedia;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

@Environment(EnvType.CLIENT)
public class InputStreamTexture extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();

    public void load(ResourceManager manager) {
    }

    public void load(InputStream inputStream) throws IOException {
        InputStreamTexture.TextureData textureData = InputStreamTexture.TextureData.load(inputStream);
        textureData.checkException();
        NativeImage nativeImage = textureData.getImage();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                this.uploadTexture(nativeImage);
            });
        } else {
            this.uploadTexture(nativeImage);
        }

    }

    private void uploadTexture(NativeImage nativeImage) {
        TextureUtil.prepareImage(this.getGlId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, false, false, true);
    }

    @Environment(EnvType.CLIENT)
    public static class TextureData implements Closeable {
        @Nullable
        private final NativeImage image;
        @Nullable
        private final IOException exception;

        public TextureData(IOException exception) {
            this.exception = exception;
            this.image = null;
        }

        public TextureData(NativeImage image) {
            this.exception = null;
            this.image = image;
        }

        public static InputStreamTexture.TextureData load(InputStream inputStream) {
            try {
                Throwable throwable = null;
                InputStreamTexture.TextureData data;
                try {
                    NativeImage nativeImage = NativeImage.read(inputStream);
                    data = new InputStreamTexture.TextureData(nativeImage);
                } catch (Throwable throwable1) {
                    throwable = throwable1;
                    throw throwable1;
                } finally {
                    if (inputStream != null) {
                        if (throwable != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        } else {
                            inputStream.close();
                        }
                    }
                }
                return data;
            } catch (IOException e) {
                return new InputStreamTexture.TextureData(e);
            }
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            } else {
                return this.image;
            }
        }

        public void close() {
            if (this.image != null) {
                this.image.close();
            }
        }

        public void checkException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}
