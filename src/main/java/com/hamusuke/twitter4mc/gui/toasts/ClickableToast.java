package com.hamusuke.twitter4mc.gui.toasts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.toast.Toast;

@Environment(EnvType.CLIENT)
public interface ClickableToast extends Toast {
    default void mouseClicked(int toastX, int toastY, double x, double y, int button) {
    }

    default void mouseReleased(int toastX, int toastY, double x, double y, int button) {
    }
}
