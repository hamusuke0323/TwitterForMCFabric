package com.hamusuke.twitter4mc.gui.toasts;

import net.minecraft.client.toast.Toast;

public interface IClickableToast extends Toast {
default void mouseClicked(int toastX, int toastY, double x, double y, int button) {
}

default void mouseReleased(int toastX, int toastY, double x, double y, int button) {
}
}
