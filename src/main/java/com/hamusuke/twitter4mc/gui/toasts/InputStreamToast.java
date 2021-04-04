package com.hamusuke.twitter4mc.gui.toasts;

import net.minecraft.client.toast.Toast;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public abstract class InputStreamToast implements Toast {
	@Nullable
	protected final InputStream image;

	public InputStreamToast(@Nullable InputStream image) {
		this.image = image;
	}
}
