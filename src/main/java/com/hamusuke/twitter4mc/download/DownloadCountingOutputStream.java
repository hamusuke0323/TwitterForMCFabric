package com.hamusuke.twitter4mc.download;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.output.CountingOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class DownloadCountingOutputStream extends CountingOutputStream {
    @Nullable
    private final Consumer<DownloadCountingOutputStream> listener;

    public DownloadCountingOutputStream(OutputStream out, @Nullable Consumer<DownloadCountingOutputStream> listener) {
        super(out);
        this.listener = listener;
    }

    protected void afterWrite(int n) throws IOException {
        super.afterWrite(n);
        if (this.listener != null) {
            this.listener.accept(this);
        }
    }
}
