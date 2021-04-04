package com.hamusuke.twitter4mc.mixin;

import com.hamusuke.twitter4mc.TwitterForMinecraft;
import com.hamusuke.twitter4mc.gui.widget.ScalableImageButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    private static final Identifier TWITTER_ICON = new Identifier(TwitterForMinecraft.MOD_ID, "textures/twitter/icon/twbtn.png");

    public TitleScreenMixin() {
        super(NarratorManager.EMPTY);
    }

    @Inject(at = @At("RETURN"), method = "init()V")
    public void init(CallbackInfo info) {
        this.addButton(new ScalableImageButton(this.width / 2 + 104, this.height / 4 + 48, 20, 20, 40, 40, 0.5F, 0, 0, 40, TWITTER_ICON, 40, 80, (b) -> {
            TwitterForMinecraft.twitterScreen.setParentScreen(this);
            this.minecraft.openScreen(TwitterForMinecraft.twitterScreen);
        }, "Twitter"));
    }

    @Inject(cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;blit(IIFFIIII)V", shift = At.Shift.AFTER), method = "render(IIF)V")
    public void render(CallbackInfo info) {
        if(this.minecraft.currentScreen != this) {
            info.cancel();
        }
    }
}
