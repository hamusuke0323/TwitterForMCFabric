package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.toasts.TwitterNotificationToast;
import com.hamusuke.twitter4mc.gui.widget.MaskableTextFieldWidget;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.net.URI;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class TwitterLoginScreen extends ParentalScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    public TwitterLoginScreen(Screen parent) {
        super(new TranslatableText("twitter.login"), parent);
    }

    protected void init() {
        super.init();
        int i = this.width / 2;
        int j = this.width / 4;
        int k = this.width / 3;

        boolean flag = TwitterForMC.consumer == null;
        TwitterForMC.consumer = new MaskableTextFieldWidget(this.textRenderer, j, 20, i, 20, TwitterForMC.consumer, new TranslatableText("tw.consumer.key"), '●', 1000);
        this.addDrawableChild(TwitterForMC.consumer);

        TwitterForMC.consumerS = new MaskableTextFieldWidget(this.textRenderer, j, 60, i, 20, TwitterForMC.consumerS, new TranslatableText("tw.consumer.secret"), '●', 1000);
        this.addDrawableChild(TwitterForMC.consumerS);

        TwitterForMC.access = new MaskableTextFieldWidget(this.textRenderer, j, 100, i, 20, TwitterForMC.access, new TranslatableText("tw.access.token"), '●', 1000);
        this.addDrawableChild(TwitterForMC.access);

        TwitterForMC.accessS = new MaskableTextFieldWidget(this.textRenderer, j, 140, i, 20, TwitterForMC.accessS, new TranslatableText("tw.access.token.secret"), '●', 1000);
        this.addDrawableChild(TwitterForMC.accessS);

        if (flag) {
            TwitterForMC.update();
        }

        TwitterForMC.save = this.addDrawableChild(new CheckboxWidget(j, 170, 20, 20, new TranslatableText("tw.save.keys"), TwitterForMC.save != null ? TwitterForMC.save.isChecked() : TwitterForMC.readToken()));

        TwitterForMC.autoLogin = this.addDrawableChild(new CheckboxWidget(j, 200, 20, 20, new TranslatableText("tw.auto.login"), TwitterForMC.autoLogin != null ? TwitterForMC.autoLogin.isChecked() : TwitterForMC.readToken() && TwitterForMC.isAutoLogin()));

        TwitterForMC.login = TwitterForMC.login != null ? TwitterForMC.login : new ButtonWidget(0, this.height - 20, this.width / 2, 20, this.title, b -> {
            this.active(false);
            if (TwitterForMC.access.getText().isEmpty() || TwitterForMC.accessS.getText().isEmpty()) {
                this.pinLogin((twitter) -> {
                    this.client.setScreen(TwitterForMC.twitterScreen);
                    this.addToast(twitter);
                });
            } else {
                this.simpleLogin((twitter) -> {
                    this.client.setScreen(TwitterForMC.twitterScreen);
                    this.addToast(twitter);
                });
            }
            this.active(true);
        });
        TwitterForMC.login.y = this.height - 20;
        TwitterForMC.login.setWidth(k);
        TwitterForMC.login.setMessage(this.title);
        this.addDrawableChild(TwitterForMC.login);

        this.addDrawableChild(new ButtonWidget(k, this.height - 20, k, 20, new TranslatableText("tw.token.file.choose"), b -> TwitterForMC.tokenFileChooser.choose()));

        this.addDrawableChild(new ButtonWidget(k * 2, this.height - 20, k, 20, ScreenTexts.BACK, b -> this.onClose()));
    }

    private void addToast(Twitter twitter) {
        try {
            this.client.getToastManager().add(new TwitterNotificationToast(TwitterUtil.getInputStream(twitter.showUser(twitter.getId()).get400x400ProfileImageURLHttps()), new TranslatableText("tw.login.successful"), null));
        } catch (Throwable t) {
            this.client.getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("tw.login.successful"), null));
        }
    }

    private void active(boolean flag) {
        TwitterForMC.consumer.setEditable(flag);
        TwitterForMC.consumerS.setEditable(flag);
        TwitterForMC.access.setEditable(flag);
        TwitterForMC.accessS.setEditable(flag);
        TwitterForMC.save.active = flag;
        TwitterForMC.autoLogin.active = flag;
        TwitterForMC.login.active = flag;
    }

    public void tick() {
        TwitterForMC.login.active = !(TwitterForMC.consumer.active && TwitterForMC.consumer.getText().isEmpty()) && !(TwitterForMC.consumerS.active && TwitterForMC.consumerS.getText().isEmpty());
        TwitterForMC.autoLogin.active = TwitterForMC.save.active && TwitterForMC.save.isChecked();
        TwitterForMC.autoLogin.visible = TwitterForMC.save.isChecked();
        super.tick();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.parent.render(matrices, -1, -1, delta);
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        super.render(matrices, mouseX, mouseY, delta);
        if (TwitterForMC.save.isMouseOver(mouseX, mouseY)) {
            this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(new TranslatableText("tw.save.keys.desc"), this.width / 2), mouseX, mouseY);
        }
    }

    private synchronized void simpleLogin(Consumer<Twitter> callback) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(TwitterForMC.consumer.getText(), TwitterForMC.consumerS.getText());
            AccessToken token = new AccessToken(TwitterForMC.access.getText(), TwitterForMC.accessS.getText());
            twitter.setOAuthAccessToken(token);
            twitter.getId();
            TwitterForMC.mcTwitter = twitter;
            TwitterForMC.store(TwitterForMC.consumer.getText(), TwitterForMC.consumerS.getText(), token, TwitterForMC.autoLogin.isChecked());
            callback.accept(TwitterForMC.mcTwitter);
        } catch (Throwable e) {
            TwitterForMC.mcTwitter = null;
            LOGGER.error("Error occurred while logging in twitter", e);
            this.client.setScreen(new ErrorScreen(new TranslatableText("tw.login.failed"), this, e.getLocalizedMessage()));
        }
    }

    private synchronized void pinLogin(Consumer<Twitter> callback) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(TwitterForMC.consumer.getText(), TwitterForMC.consumerS.getText());
            RequestToken requestToken = twitter.getOAuthRequestToken();
            Util.getOperatingSystem().open(new URI(requestToken.getAuthorizationURL()));
            this.client.setScreen(new EnterPinScreen((pin) -> {
                try {
                    AccessToken token = twitter.getOAuthAccessToken(requestToken, pin);
                    twitter.setOAuthAccessToken(token);
                    twitter.getId();
                    TwitterForMC.mcTwitter = twitter;
                    TwitterForMC.access.setText(token.getToken());
                    TwitterForMC.accessS.setText(token.getTokenSecret());
                    TwitterForMC.store(TwitterForMC.consumer.getText(), TwitterForMC.consumerS.getText(), token, TwitterForMC.autoLogin.isChecked());
                    callback.accept(TwitterForMC.mcTwitter);
                } catch (Throwable e) {
                    TwitterForMC.mcTwitter = null;
                    LOGGER.error("Error occurred while logging in twitter", e);
                    this.client.setScreen(new ErrorScreen(new TranslatableText("tw.login.failed"), this, e.getLocalizedMessage()));
                }
            }));
        } catch (Throwable e) {
            LOGGER.error("Error occurred while logging in twitter", e);
            this.client.setScreen(new ErrorScreen(new TranslatableText("tw.login.failed"), this, e.getLocalizedMessage()));
        }
    }
}
