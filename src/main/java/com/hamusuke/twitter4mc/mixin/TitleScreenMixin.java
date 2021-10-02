package com.hamusuke.twitter4mc.mixin;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.widget.ScalableImageButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    private static final Identifier TWITTER_ICON = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/twbtn.png");

    private TitleScreenMixin() {
        super(NarratorManager.EMPTY);
    }

    @Inject(at = @At("RETURN"), method = "init()V")
    private void init(CallbackInfo info) {
        if (!TwitterForMC.twitterScreen.isInitialized()) {
            TwitterForMC.twitterScreen.init(this.client, this.width, this.height);
        }

        this.addDrawableChild(new ScalableImageButton(this.width / 2 + 104, this.height / 4 + 48, 20, 20, 40, 40, 0.5F, 0, 0, 40, TWITTER_ICON, 40, 80, (b) -> {
            TwitterForMC.twitterScreen.setParentScreen(this);
            this.minecraft.openScreen(TwitterForMC.twitterScreen);
        }, "Twitter"));
    }

    @Inject(cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;blit(IIFFIIII)V", shift = At.Shift.AFTER), method = "render")
    private void render(CallbackInfo info) {
        if (this.client.currentScreen != this) {
            info.cancel();
        }
    }
}
