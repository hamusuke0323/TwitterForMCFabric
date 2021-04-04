package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.Token;
import com.hamusuke.twitter4mc.TwitterForMinecraft;
import com.hamusuke.twitter4mc.gui.widget.MaskableTextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

public class TwitterLoginScreen extends ParentalScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    public TwitterLoginScreen(Screen parent) {
        super(new TranslatableText("twitter.login"), parent);
    }

    protected void init() {
        super.init();
        int i = this.width / 3;

        boolean flag = TwitterForMinecraft.consumer == null;
        TwitterForMinecraft.consumer = TwitterForMinecraft.consumer != null ? TwitterForMinecraft.consumer : new MaskableTextFieldWidget(this.font, i, 20, i, 20, I18n.translate("tw.consumer.key"), '●', 1000);
        TwitterForMinecraft.consumer.x = i;
        TwitterForMinecraft.consumer.setWidth(i);
        TwitterForMinecraft.consumer.setMessage(I18n.translate("tw.consumer.key"));
        this.addButton(TwitterForMinecraft.consumer);

        TwitterForMinecraft.consumerS = TwitterForMinecraft.consumerS != null ? TwitterForMinecraft.consumerS : new MaskableTextFieldWidget(this.font, i, 60, i, 20, I18n.translate("tw.consumer.secret"), '●', 1000);
        TwitterForMinecraft.consumerS.x = i;
        TwitterForMinecraft.consumerS.setWidth(i);
        TwitterForMinecraft.consumerS.setMessage(I18n.translate("tw.consumer.secret"));
        this.addButton(TwitterForMinecraft.consumerS);

        TwitterForMinecraft.access = TwitterForMinecraft.access != null ? TwitterForMinecraft.access : new MaskableTextFieldWidget(this.font, i, 100, i, 20, I18n.translate("tw.access.token"), '●', 1000);
        TwitterForMinecraft.access.x = i;
        TwitterForMinecraft.access.setWidth(i);
        TwitterForMinecraft.access.setMessage(I18n.translate("tw.access.token"));
        this.addButton(TwitterForMinecraft.access);

        TwitterForMinecraft.accessS = TwitterForMinecraft.accessS != null ? TwitterForMinecraft.accessS : new MaskableTextFieldWidget(this.font, i, 140, i, 20, I18n.translate("tw.access.token.secret"), '●', 1000);
        TwitterForMinecraft.accessS.x = i;
        TwitterForMinecraft.accessS.setWidth(i);
        TwitterForMinecraft.accessS.setMessage(I18n.translate("tw.access.token.secret"));
        this.addButton(TwitterForMinecraft.accessS);

        if(flag) {
            TwitterForMinecraft.update();
        }

        TwitterForMinecraft.save = this.addButton(new CheckboxWidget(i, 170, 20, 20, I18n.translate("tw.save.keys"), TwitterForMinecraft.save != null ? TwitterForMinecraft.save.isChecked() : TwitterForMinecraft.readToken()));

        TwitterForMinecraft.autoLogin = this.addButton(new CheckboxWidget(i, 200, 20, 20, I18n.translate("tw.auto.login"), TwitterForMinecraft.autoLogin != null ? TwitterForMinecraft.autoLogin.isChecked() : TwitterForMinecraft.readToken() && TwitterForMinecraft.getToken().autoLogin()));

        TwitterForMinecraft.login = TwitterForMinecraft.login != null ? TwitterForMinecraft.login : new ButtonWidget(0, this.height - 20, this.width / 2, 20, this.title.asFormattedString(), (b) -> {
            this.active(false);
            if (TwitterForMinecraft.access.getText().isEmpty() || TwitterForMinecraft.accessS.getText().isEmpty()) {
                this.pinLogin(() -> {
                    this.minecraft.openScreen(TwitterForMinecraft.twitterScreen);
                    this.minecraft.getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("tw.login.successful"), null));
                });
            } else {
                this.simpleLogin(() -> {
                    this.minecraft.openScreen(TwitterForMinecraft.twitterScreen);
                    this.minecraft.getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("tw.login.successful"), null));
                });
            }
            this.active(true);
        });
        TwitterForMinecraft.login.y = this.height - 20;
        TwitterForMinecraft.login.setWidth(this.width / 2);
        TwitterForMinecraft.login.setMessage(this.title.asFormattedString());
        this.addButton(TwitterForMinecraft.login);

        this.addButton(new ButtonWidget(this.width / 2, this.height - 20, this.width / 2, 20, I18n.translate("gui.done"), (b) -> {
            this.onClose();
        }));
    }

    private void active(boolean flag) {
        TwitterForMinecraft.consumer.setEditable(flag);
        TwitterForMinecraft.consumerS.setEditable(flag);
        TwitterForMinecraft.access.setEditable(flag);
        TwitterForMinecraft.accessS.setEditable(flag);
        TwitterForMinecraft.save.active = flag;
        TwitterForMinecraft.autoLogin.active = flag;
        TwitterForMinecraft.login.active = flag;
    }

    public void tick() {
        TwitterForMinecraft.login.active = !(TwitterForMinecraft.consumer.active && TwitterForMinecraft.consumer.getText().isEmpty()) && !(TwitterForMinecraft.consumerS.active && TwitterForMinecraft.consumerS.getText().isEmpty());
        TwitterForMinecraft.autoLogin.active = TwitterForMinecraft.save.active && TwitterForMinecraft.save.isChecked();
        TwitterForMinecraft.autoLogin.visible = TwitterForMinecraft.save.isChecked();
        super.tick();
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.parent.render(-1, -1, delta);
        this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        super.render(mouseX, mouseY, delta);
        if (TwitterForMinecraft.save.isMouseOver(mouseX, mouseY)) {
            this.renderTooltip(this.font.wrapStringToWidthAsList(I18n.translate("tw.save.keys.desc"), this.width / 3), mouseX, mouseY);
        }
    }

    private synchronized void simpleLogin(Runnable callback) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(TwitterForMinecraft.consumer.getText(), TwitterForMinecraft.consumerS.getText());
            AccessToken token = new AccessToken(TwitterForMinecraft.access.getText(), TwitterForMinecraft.accessS.getText());
            twitter.setOAuthAccessToken(token);
            twitter.getId();
            TwitterForMinecraft.mctwitter = twitter;
            this.store(new Token(TwitterForMinecraft.consumer.getText(), TwitterForMinecraft.consumerS.getText(), token, TwitterForMinecraft.autoLogin.isChecked()));
            callback.run();
        } catch (Throwable e) {
            TwitterForMinecraft.mctwitter = null;
            LOGGER.error("Error occurred while logging in twitter", e);
            this.minecraft.openScreen(new ErrorScreen(new TranslatableText("tw.login.failed"), this, e.getLocalizedMessage()));
        }
    }

    private synchronized void pinLogin(Runnable callback) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(TwitterForMinecraft.consumer.getText(), TwitterForMinecraft.consumerS.getText());
            RequestToken requestToken = twitter.getOAuthRequestToken();
            Util.getOperatingSystem().open(new URI(requestToken.getAuthorizationURL()));
            this.minecraft.openScreen(new EnterPinScreen((pin) -> {
                try {
                    AccessToken token = twitter.getOAuthAccessToken(requestToken, pin);
                    twitter.setOAuthAccessToken(token);
                    twitter.getId();
                    TwitterForMinecraft.mctwitter = twitter;
                    TwitterForMinecraft.access.setText(token.getToken());
                    TwitterForMinecraft.accessS.setText(token.getTokenSecret());
                    this.store(new Token(TwitterForMinecraft.consumer.getText(), TwitterForMinecraft.consumerS.getText(), token, TwitterForMinecraft.autoLogin.isChecked()));
                    callback.run();
                } catch (Throwable e) {
                    TwitterForMinecraft.mctwitter = null;
                    LOGGER.error("Error occurred while logging in twitter", e);
                    this.minecraft.openScreen(new ErrorScreen(new TranslatableText("tw.login.failed"), this, e.getLocalizedMessage()));
                }
            }));
        } catch (Throwable e) {
            LOGGER.error("Error occurred while logging in twitter", e);
            this.minecraft.openScreen(new ErrorScreen(new TranslatableText("tw.login.failed"), this, e.getLocalizedMessage()));
        }
    }

    private synchronized void store(Token token) throws Throwable {
        if (TwitterForMinecraft.save.isChecked()) {
            ObjectOutputStream var1 = null;
            try {
                var1 = new ObjectOutputStream(new FileOutputStream(TwitterForMinecraft.getTokenFile()));
                var1.writeObject(token);
                var1.flush();
            } catch (Throwable e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(var1);
            }
        }
    }
}
