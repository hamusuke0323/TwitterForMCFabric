package com.hamusuke.twitter4mc;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.emoji.EmojiManager;
import com.hamusuke.twitter4mc.filechooser.SFileChooser;
import com.hamusuke.twitter4mc.gui.screen.TwitterScreen;
import com.hamusuke.twitter4mc.gui.widget.MaskableTextFieldWidget;
import com.hamusuke.twitter4mc.utils.TextureManager;
import com.hamusuke.twitter4mc.utils.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterThread;
import com.hamusuke.twitter4mc.utils.VersionChecker;
import com.hamusuke.twitter4mc.utils.license.LicenseManager;
import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TwitterForMC implements ClientModInitializer {
    public static final String MOD_ID = "twitter4mc";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final TextureManager textureManager = new TextureManager();
    private static final EmojiManager emojiManager = new EmojiManager();
    @Nullable
    private static Path configFile;
    @Nullable
    private static File tokenFile;
    @Nullable
    public static Twitter mctwitter;
    @Nullable
    private static Token token;
    public static final TwitterScreen twitterScreen = new TwitterScreen();
    public static boolean loginTwitter;
    public static List<Status> tweets = Lists.newArrayList();
    public static List<TweetSummary> tweetSummaries = Lists.newArrayList();
    public static KeyBinding openTwitter;
    @Nullable
    public static MaskableTextFieldWidget consumer;
    @Nullable
    public static MaskableTextFieldWidget consumerS;
    @Nullable
    public static MaskableTextFieldWidget access;
    @Nullable
    public static MaskableTextFieldWidget accessS;
    @Nullable
    public static CheckboxWidget save;
    @Nullable
    public static CheckboxWidget autoLogin;
    @Nullable
    public static ButtonWidget login;
    public static final SFileChooser tokenFileChooser = new SFileChooser((file) -> {
        if (file != null) {
            Token t = read(file);
            if (t != null) {
                token = t;
                update();
                if (token.autoLogin()) {
                    if (!save.isChecked()) {
                        save.onPress();
                    }
                    if (!autoLogin.isChecked()) {
                        autoLogin.onPress();
                    }
                } else {
                    if (autoLogin.isChecked()) {
                        autoLogin.onPress();
                    }
                }
            }
        }
    });

    public void onInitializeClient() {
        LOGGER.info("Hello from TwitterForMC#onInitializeClient!");

        LicenseManager.registerLicense(new Identifier(TwitterForMC.MOD_ID, "license/mitlicense.txt"), 400, "tw.license.thismod");
        LicenseManager.registerLicense(new Identifier(TwitterForMC.MOD_ID, "license/twitter4j_license.txt"), 270, "tw.license.twitter4j");
        LicenseManager.registerLicense(new Identifier(TwitterForMC.MOD_ID, "license/twemoji_graphics_license.txt"), 320, "tw.license.twemoji.graphics");

        VersionChecker.checkUpdate();

        configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        if (!configFile.toFile().exists()) {
            if (configFile.toFile().mkdir()) {
                LOGGER.info("made config directory: {}", MOD_ID);
            }
        }
        tokenFile = configFile.resolve("token").toFile();

        openTwitter = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.twitter4mc.opentw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.gameplay"));
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            while (openTwitter.wasPressed()) {
                twitterScreen.setParentScreen(null);
                client.openScreen(twitterScreen);
            }
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(emojiManager);

        PlatformImpl.startup(() -> {
        });
        Platform.setImplicitExit(false);

        new TwitterThread(() -> {
            token = read(tokenFile);
            if (token != null) {
                AccessToken var1 = new AccessToken(token.getAccessToken(), token.getAccessTokenSecret());
                if (token.autoLogin()) {
                    try {
                        mctwitter = new TwitterFactory().getInstance();
                        mctwitter.setOAuthConsumer(token.getConsumer(), token.getConsumerSecret());
                        mctwitter.setOAuthAccessToken(var1);
                        mctwitter.getId();
                        loginTwitter = true;
                        LOGGER.info("Successfully logged in twitter.");
                    } catch (Throwable e) {
                        mctwitter = null;
                        loginTwitter = false;
                        LOGGER.error("Error occurred while logging in twitter", e);
                    }
                }
            }

            if(loginTwitter) {
                loadTimeline();
                for (Status s : tweets) {
                    tweetSummaries.add(new TweetSummary(s));
                }
            }
        }).start();
    }

    public static synchronized void saveTimeline() throws IOException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(configFile.resolve("timeline").toFile()));
            oos.writeObject(tweets);
            oos.flush();
        } catch (IOException e) {
            LOGGER.error("Error occurred while saving timeline", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(oos);
        }
    }

    private static synchronized void loadTimeline() {
        File file = configFile.resolve("timeline").toFile();
        if (!file.exists()) {
            return;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            tweets = (List<Status>) ois.readObject();
        } catch (Throwable e) {
            LOGGER.error("Error occurred while reading timeline", e);
        } finally {
            IOUtils.closeQuietly(ois);
        }
    }

    @Nullable
    private static synchronized Token read(File tokenFile) {
        if (!tokenFile.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(tokenFile));
            return (Token) ois.readObject();
        } catch (Throwable e) {
            LOGGER.error("Error occurred while reading tokens", e);
        } finally {
            IOUtils.closeQuietly(ois);
        }
        return null;
    }

    @Nullable
    public static File getTokenFile() {
        return tokenFile;
    }

    @Nullable
    public static Path getConfigFile() {
        return configFile;
    }

    @Nullable
    public static Token getToken() {
        return token;
    }

    public static boolean readToken() {
        return token != null;
    }

    public static void update() {
        if (readToken()) {
            TwitterForMC.consumer.setText(token.getConsumer());
            TwitterForMC.consumerS.setText(token.getConsumerSecret());
            TwitterForMC.access.setText(token.getAccessToken());
            TwitterForMC.accessS.setText(token.getAccessTokenSecret());
        }
    }

    public static TextureManager getTextureManager() {
        return textureManager;
    }

    public static EmojiManager getEmojiManager() {
        return emojiManager;
    }
}
