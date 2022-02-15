package com.hamusuke.twitter4mc.gui.screen.twitter;

import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.ClickSpaceToCloseScreen;
import com.hamusuke.twitter4mc.gui.widget.TwitterTweetFieldWidget;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.utils.TwitterThread;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import twitter4j.TwitterException;
import twitter4j.util.CharacterUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class TwitterReplyScreen extends ClickSpaceToCloseScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final TweetSummary replyTo;
    private TwitterTweetFieldWidget tweetText;
    private ButtonWidget back;
    private ButtonWidget tweet;

    public TwitterReplyScreen(@Nullable Screen parent, TweetSummary tweetSummary) {
        super(new TranslatableText("tw.reply.to", tweetSummary.getScreenName()), parent);
        this.replyTo = tweetSummary;
    }

    public void tick() {
        this.tweetText.tick();
        this.tweet.active = !this.tweetText.getText().isBlank();

        super.tick();
    }

    protected void init() {
        super.init();
        int i = this.width / 4;
        this.client.keyboard.setRepeatEvents(true);
        this.tweetText = new TwitterTweetFieldWidget(this.textRenderer, i, this.height / 4, i * 2, this.height / 2, NarratorManager.EMPTY);
        this.tweetText.setEditableColor(-1);
        this.tweetText.setMaxLength(CharacterUtil.MAX_TWEET_LENGTH);

        this.back = this.addDrawableChild(new ButtonWidget(i, (this.height / 4 + this.height / 2) + 10, i, 20, ScreenTexts.BACK, a -> this.onClose()));

        this.tweet = this.addDrawableChild(new ButtonWidget(i * 2, (this.height / 4 + this.height / 2) + 10, i, 20, new TranslatableText("tweet"), b -> {
            this.tweet.active = this.back.active = false;
            CompletableFuture.runAsync(() -> {
                try {
                    TweetSummary tweetSummary = new TweetSummary(TwitterForMC.mcTwitter.updateStatus(TwitterUtil.createReplyTweet(this.tweetText.getText(), this.replyTo.getStatus())));
                    TwitterForMC.tweets.add(tweetSummary.getStatus());
                    TwitterForMC.tweetSummaries.add(tweetSummary);
                    this.accept(new TranslatableText("sent.tweet", new TranslatableText("sent.tweet.view").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, AbstractTwitterScreen.PROTOCOL + "://" + AbstractTwitterScreen.HostType.SHOW_STATUS.getHostName() + "/" + tweetSummary.getId())))));
                } catch (TwitterException e) {
                    LOGGER.error("Error occurred while sending tweet", e);
                    this.accept(new TranslatableText("failed.send.tweet", e.getErrorMessage()));
                }
            }, Executors.newCachedThreadPool(TwitterThread::new)).whenComplete((unused, throwable) -> this.onClose());
        }));

        this.addSelectableChild(this.tweetText);
    }

    public boolean shouldCloseOnEsc() {
        return this.back.active;
    }

    private void accept(Text msg) {
        if (this.parent instanceof AbstractTwitterScreen abstractTwitterScreen) {
            abstractTwitterScreen.accept(msg);
        }
    }

    public void resize(MinecraftClient p_resize_1_, int p_resize_2_, int p_resize_3_) {
        String s = this.tweetText.getText();
        this.init(p_resize_1_, p_resize_2_, p_resize_3_);
        this.tweetText.setText(s);
    }

    public void removed() {
        super.removed();
        this.client.keyboard.setRepeatEvents(false);
    }

    public void render(MatrixStack matrices, int p_render_1_, int p_render_2_, float p_render_3_) {
        if (this.parent != null) {
            matrices.push();
            matrices.translate(0.0D, 0.0D, -1.0D);
            this.parent.render(matrices, -1, -1, p_render_3_);
            matrices.pop();
        }
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        RenderSystem.disableBlend();
        this.tweetText.render(matrices, p_render_1_, p_render_2_, p_render_3_);
        this.textRenderer.drawWithShadow(matrices, this.getTitle(), this.tweetText.x, this.tweetText.y - 10, 16777215);
        super.render(matrices, p_render_1_, p_render_2_, p_render_3_);
    }
}
