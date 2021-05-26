package com.hamusuke.twitter4mc.mixin;

import javafx.application.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At(target = "Ljava/lang/System;exit(I)V", value = "INVOKE"), method = "stop()V")
    private void stop(CallbackInfo info) {
        Platform.exit();
    }
}
