package com.hamusuke.twitter4mc.gui.screen.twitter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.screen.DownloadTwitterVideoScreen;
import com.hamusuke.twitter4mc.gui.screen.ParentalScreen;
import com.hamusuke.twitter4mc.gui.screen.ReturnableGame;
import com.hamusuke.twitter4mc.gui.widget.ChangeableImageButton;
import com.hamusuke.twitter4mc.gui.widget.FunctionalButtonWidget;
import com.hamusuke.twitter4mc.gui.widget.MessageWidget;
import com.hamusuke.twitter4mc.gui.widget.TwitterButton;
import com.hamusuke.twitter4mc.gui.widget.list.AbstractTwitterTweetList;
import com.hamusuke.twitter4mc.gui.widget.list.ExtendedTwitterTweetList;
import com.hamusuke.twitter4mc.invoker.TextRendererInvoker;
import com.hamusuke.twitter4mc.text.TweetText;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.ImageDataDeliverer;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import twitter4j.TwitterException;
import twitter4j.User;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public abstract class AbstractTwitterScreen extends ParentalScreen implements ReturnableGame {
    protected static final String PROTOCOL = TwitterForMC.MOD_ID;

    protected static final Identifier PROTECTED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/protected.png");
    protected static final Identifier VERIFIED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/verified.png");
    protected static final Identifier REPLY = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/reply.png");
    protected static final Identifier RETWEET = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/retweet.png");
    protected static final Identifier RETWEETED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/retweeted.png");
    protected static final Identifier RETWEET_USER = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/retweetuser.png");
    protected static final Identifier FAVORITE = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/favorite.png");
    protected static final Identifier FAVORITED = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/favorited.png");
    protected static final Identifier SHARE = new Identifier(TwitterForMC.MOD_ID, "textures/twitter/icon/share.png");

    protected static final Text FOLLOW = new TranslatableText("tw.follow").formatted(Formatting.GRAY);
    protected static final Text FOLLOWER = new TranslatableText("tw.follower").formatted(Formatting.GRAY);
    protected static final Text THREE_PERIOD = new LiteralText("...").formatted(Formatting.BOLD);
    protected static final Text THREE_PERIOD_GRAY = new LiteralText("...").formatted(Formatting.GRAY);
    private static final Logger LOGGER = LogManager.getLogger();

    protected final List<ClickableWidget> renderLaterButtons = Lists.newArrayList();
    @Nullable
    protected AbstractTwitterScreen.TweetList list;
    @Nullable
    Screen previousScreen;
    @Nullable
    public static MessageWidget messageWidget;

    protected AbstractTwitterScreen(Text title, @Nullable Screen parent) {
        super(title, parent);
    }

    protected static void renderMessage(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        getMessageWidget().ifPresent(messageWidget -> messageWidget.render(matrices, mouseX, mouseY, delta));
    }

    public static int getMaxWidth(TextRenderer textRenderer, List<OrderedText> messageLines) {
        MutableInt mutableInt = new MutableInt();
        messageLines.forEach(orderedText -> mutableInt.setValue(Math.max(mutableInt.getValue(), textRenderer.getWidth(orderedText))));
        return mutableInt.getValue();
    }

    protected <T extends ClickableWidget> T addRenderLaterButton(T button) {
        this.renderLaterButtons.add(button);
        this.addSelectableChild(button);
        return button;
    }

    public void renderButtonLater(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        for (ClickableWidget abstractButtonWidget : this.renderLaterButtons) {
            abstractButtonWidget.render(matrices, mouseX, mouseY, tickDelta);
        }
    }

    @Override
    protected void clearChildren() {
        super.clearChildren();
        this.renderLaterButtons.clear();
    }

    public static Optional<MessageWidget> getMessageWidget() {
        return Optional.ofNullable(messageWidget);
    }

    @Override
    public void tick() {
        if (this.list != null) {
            this.list.tick();
        }

        getMessageWidget().ifPresent(MessageWidget::tick);
        super.tick();
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - this.width / 4, this.height - 20, this.width / 2, 20, new TranslatableText("menu.returnToGame"), button -> this.returnToGame()));
        getMessageWidget().ifPresent(messageWidget -> messageWidget.init(this.width, this.height));
        super.init();
    }

    public void accept(Text text) {
        List<OrderedText> messageLines = this.textRenderer.wrapLines(text, this.width / 2);
        if (messageLines.size() > 0) {
            int width = getMaxWidth(this.textRenderer, messageLines);
            messageWidget = new MessageWidget(this, this.client, (this.width - width) / 2, this.height - 20 - messageLines.size() * this.textRenderer.fontHeight, width, messageLines.size() * 9, text);
            messageWidget.init(this.width, this.height);
        }
    }

    @Override
    public void returnToGame() {
        TwitterForMC.twitterScreen.previousScreen = this;
        this.client.setScreen(null);
    }

    protected final Optional<Screen> getPreviousScreen() {
        Optional<Screen> screen = Optional.ofNullable(this.previousScreen);
        this.previousScreen = null;
        return screen;
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (this.list != null && this.list.hoveringEntry != null && this.list.hoveringEntry.summary != null && this.list.hoveringEntry.mayClickIcon(p_mouseClicked_1_, p_mouseClicked_3_)) {
            this.displayTwitterUser(this, this.list.hoveringEntry.summary.getUser());
            return true;
        } else if (this.list != null && !this.list.isHovering) {
            boolean bl = false;
            if (messageWidget != null) {
                bl = messageWidget.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
            }

            if (!bl) {
                bl = super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
            }

            return bl;
        }

        return false;
    }

    public boolean renderTwitterUser(MatrixStack matrices, TweetSummary summary, int x, int y, int mouseX, int mouseY) {
        User user = summary.getUser();
        ImageDataDeliverer icon = summary.getUserIconData();
        List<OrderedText> desc = this.wrapLines(new TweetText(user.getDescription()), Math.min(this.width / 2, 150));
        Text follow = new LiteralText(user.getFriendsCount() + "").formatted(Formatting.BOLD);
        Text space = Text.of(" ");
        Text follower = new LiteralText(user.getFollowersCount() + "").formatted(Formatting.BOLD);
        List<OrderedText> ff = this.wrapLines(StringVisitable.concat(follow, space, FOLLOW, Text.of("  "), follower, space, FOLLOWER), 150);

        RenderSystem.disableDepthTest();
        int i = 0;

        for (OrderedText s : desc) {
            int j = this.getWidthWithEmoji(s);
            if (j > i) {
                i = j;
            }
        }

        for (OrderedText s1 : ff) {
            int j2 = this.textRenderer.getWidth(s1);
            if (j2 > i) {
                i = j2;
            }
        }

        int i2 = y;
        int k = 0;
        k += icon.readyToRender() ? 22 : 0;
        k += user.getName().isEmpty() ? 0 : 10;
        k += user.getScreenName().isEmpty() ? 0 : 10;
        k += 4 + (desc.size() * (this.textRenderer.fontHeight + 1)) + 4;
        k += ff.size() == 1 ? 10 : 20 + 2;

        if (i2 + k + 6 > this.height - 20) {
            i2 = this.height - 20 - k - 6;
        }

        this.fillGradient(matrices, x - 3, i2 - 4, x + i + 3, i2 - 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, i2 + k + 3, x + i + 3, i2 + k + 4, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, i2 - 3, x + i + 3, i2 + k + 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 4, i2 - 3, x - 3, i2 + k + 3, -267386864, -267386864);
        this.fillGradient(matrices, x + i + 3, i2 - 3, x + i + 4, i2 + k + 3, -267386864, -267386864);
        this.fillGradient(matrices, x - 3, i2 - 3 + 1, x - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(matrices, x + i + 2, i2 - 3 + 1, x + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(matrices, x - 3, i2 - 3, x + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
        this.fillGradient(matrices, x - 3, i2 + k + 2, x + i + 3, i2 + k + 3, 1344798847, 1344798847);
        MatrixStack matrixstack = new MatrixStack();
        VertexConsumerProvider.Immediate vertexConsumerProvider$immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        Matrix4f matrix4f = matrixstack.peek().getModel();

        int yy = i2;
        if (icon.readyToRender()) {
            TwitterForMC.getTextureManager().bindTexture(icon.deliver());
            drawTexture(matrices, x, i2, 0.0F, 0.0F, 20, 20, 20, 20);
            i2 += 20;
        }

        int yyy = i2;
        boolean p = user.isProtected();
        boolean v = user.isVerified();
        int m = (p ? 10 : 0) + (v ? 10 : 0);
        StringVisitable name = new TweetText(user.getName()).formatted(Formatting.BOLD);
        List<OrderedText> nameFormatted = this.wrapLines(name, i - this.getWidthWithEmoji(THREE_PERIOD.asOrderedText()) - m);
        int n = ((TextRendererInvoker) this.textRenderer).drawWithEmoji(nameFormatted.size() == 1 ? nameFormatted.get(0) : OrderedText.concat(nameFormatted.get(0), THREE_PERIOD.asOrderedText()), (float) x, (float) i2 + 2, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
        ((TextRendererInvoker) this.textRenderer).drawWithEmoji(new TweetText(summary.getScreenName()).formatted(Formatting.GRAY), (float) x, (float) i2 + 12, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);

        for (OrderedText s1 : desc) {
            if (s1 != null) {
                ((TextRendererInvoker) this.textRenderer).drawWithEmoji(s1, (float) x, (float) i2 + 26, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
            }

            i2 += 10;
        }

        if (ff.size() == 1) {
            ((TextRendererInvoker) this.textRenderer).drawWithEmoji(ff.get(0), (float) x, (float) i2 + 30, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
        } else {
            ((TextRendererInvoker) this.textRenderer).drawWithEmoji(OrderedText.concat(follow.asOrderedText(), space.asOrderedText(), FOLLOW.asOrderedText()), (float) x, (float) i2 + 30, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
            ((TextRendererInvoker) this.textRenderer).drawWithEmoji(OrderedText.concat(follower.asOrderedText(), space.asOrderedText(), FOLLOWER.asOrderedText()), (float) x, (float) i2 + 30 + 10, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
        }

        vertexConsumerProvider$immediate.draw();

        if (p) {
            n += this.renderProtected(matrices, n, yyy + 2);
        }
        if (v) {
            this.renderVerified(matrices, n, yyy + 2);
        }

        RenderSystem.enableDepthTest();

        return x - 4 < mouseX && x + i + 4 > mouseX && yy - 4 < mouseY && yy + k + 4 > mouseY;
    }

    public int renderRetweetedUser(MatrixStack matrices, @Nullable TweetSummary retweetedSummary, int iconX, int x, int y, int width) {
        if (retweetedSummary != null) {
            RenderSystem.setShaderTexture(0, RETWEET_USER);
            matrices.push();
            matrices.translate(iconX, y, 0.0F);
            matrices.scale(0.625F, 0.625F, 0.625F);
            DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
            matrices.pop();
            List<OrderedText> names = this.wrapUserNameToWidth(retweetedSummary, width);
            for (int i = 0; i < names.size(); i++) {
                this.drawWithShadowAndEmoji(matrices, names.get(i), x, y + i * this.textRenderer.fontHeight, 11184810);
            }
            return y + names.size() * this.textRenderer.fontHeight;
        }

        return y;
    }

    protected void displayStatus(@Nullable Screen parent, TweetSummary summary) {
        this.client.setScreen(new TwitterShowStatusScreen(parent, summary));
    }

    protected void displayTwitterUser(@Nullable Screen parent, User user) {
        this.client.setScreen(new TwitterShowUserScreen(parent, user));
    }

    public List<OrderedText> wrapUserNameToWidth(TweetSummary summary, int width) {
        return this.wrapLines(new TranslatableText("tw.retweeted.user", new TweetText(summary.getUser().getName())), width);
    }

    protected List<OrderedText> wrapLines(StringVisitable visitable, int width) {
        return ((TextRendererInvoker) this.textRenderer).wrapLinesWithEmoji(visitable, width);
    }

    protected int drawWithEmoji(MatrixStack matrices, Text text, float x, float y, int color) {
        return ((TextRendererInvoker) this.textRenderer).drawWithEmoji(matrices, text, x, y, color);
    }

    protected int drawWithShadowAndEmoji(MatrixStack matrices, Text text, float x, float y, int color) {
        return ((TextRendererInvoker) this.textRenderer).drawWithShadowAndEmoji(matrices, text, x, y, color);
    }

    protected int drawWithEmoji(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return ((TextRendererInvoker) this.textRenderer).drawWithEmoji(matrices, text, x, y, color);
    }

    protected int drawWithShadowAndEmoji(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return ((TextRendererInvoker) this.textRenderer).drawWithShadowAndEmoji(matrices, text, x, y, color);
    }

    protected int getWidthWithEmoji(OrderedText text) {
        return ((TextRendererInvoker) this.textRenderer).getWidthWithEmoji(text);
    }

    public int renderProtected(MatrixStack matrices, int x, int y) {
        RenderSystem.setShaderTexture(0, PROTECTED);
        matrices.push();
        matrices.translate(x, y, 0.0F);
        matrices.scale(0.625F, 0.625F, 0.625F);
        DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
        matrices.pop();
        return 10;
    }

    public int renderVerified(MatrixStack matrices, int x, int y) {
        RenderSystem.setShaderTexture(0, VERIFIED);
        matrices.push();
        matrices.translate(x, y, 0.0F);
        matrices.scale(0.625F, 0.625F, 0.625F);
        DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, 16, 16, 16, 16);
        matrices.pop();
        return 10;
    }

    @Override
    public boolean handleTextClick(@Nullable Style style) {
        if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
            try {
                URI uri = new URI(style.getClickEvent().getValue());
                if (uri.getScheme().equalsIgnoreCase(PROTOCOL)) {
                    String path = uri.getPath().substring(1);
                    switch (HostType.from(uri.getHost())) {
                        case SHOW_STATUS -> {
                            long id = Long.parseLong(path);

                            if (this instanceof TwitterShowStatusScreen showStatusScreen && showStatusScreen.getTweetSummary().getId() == id) {
                                return true;
                            }

                            for (TweetSummary summary : TwitterForMC.tweetSummaries) {
                                if (summary.getId() == id) {
                                    this.displayStatus(this, summary);
                                    return true;
                                }
                            }

                            TweetSummary tweetSummary = new TweetSummary(TwitterForMC.mcTwitter.showStatus(id));
                            TwitterForMC.tweets.add(tweetSummary.getStatus());
                            TwitterForMC.tweetSummaries.add(tweetSummary);
                            this.displayStatus(this, tweetSummary);
                            return true;
                        }
                        case SHOW_USER -> {
                            this.displayTwitterUser(this, TwitterForMC.mcTwitter.showUser(path));
                            return true;
                        }
                    }
                } else {
                    return super.handleTextClick(style);
                }
            } catch (Exception e) {
                LOGGER.warn("Error occurred while handling text click", e);
                this.accept(new TranslatableText("tw.simple.error", e.getLocalizedMessage()));
            }
        }

        return false;
    }

    @Environment(EnvType.CLIENT)
    protected enum HostType {
        UNKNOWN(""),
        SHOW_STATUS("status"),
        SHOW_USER("screenname"),
        HASHTAG("hashtag");

        private final String hostName;

        HostType(String hostName) {
            this.hostName = hostName;
        }

        public static HostType from(String hostName) {
            for (HostType hostType : values()) {
                if (hostType.hostName.equals(hostName)) {
                    return hostType;
                }
            }

            return UNKNOWN;
        }

        public String getHostName() {
            return this.hostName;
        }
    }

    @Environment(EnvType.CLIENT)
    protected class TweetList extends ExtendedTwitterTweetList<AbstractTwitterScreen.TweetList.TweetEntry> {
        @Nullable
        protected AbstractTwitterScreen.TweetList.TweetEntry hoveringEntry;
        protected boolean isHovering;
        protected int fade;

        protected TweetList(MinecraftClient mcIn, int width, int height, int top, int bottom) {
            super(mcIn, width, height, top, bottom);
        }

        @Override
        public void tick() {
            this.fade = this.isHovering ? 10 : this.fade - 1;
            this.children().forEach(AbstractTwitterScreen.TweetList.TweetEntry::tick);
            super.tick();
        }

        @Override
        protected int getScrollbarPositionX() {
            return AbstractTwitterScreen.this.width - 5;
        }

        @Override
        public int getRowWidth() {
            return AbstractTwitterScreen.this.width / 2;
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
        }

        @Override
        protected void renderHoleBackground(MatrixStack matrices, int top, int bottom, int alphaTop, int alphaBottom) {
            this.fillGradient(matrices, this.left + this.width, bottom, this.left, top, -15392725, -15392725);
        }

        @Override
        public void render(MatrixStack matrices, int p_render_1_, int p_render_2_, float p_render_3_) {
            super.render(matrices, p_render_1_, p_render_2_, p_render_3_);

            boolean bl = false;
            matrices.push();
            matrices.translate(0.0D, 0.0D, 0.5D);
            for (ImmutableList<ClickableWidget> clickableWidgets : this.children().stream().map(AbstractTwitterTweetList.AbstractTwitterListEntry::getOverlayButtons).toList()) {
                for (ClickableWidget clickableWidget : clickableWidgets.stream().filter(clickableWidget -> clickableWidget.active && clickableWidget.visible).toList()) {
                    clickableWidget.render(matrices, p_render_1_, p_render_2_, p_render_3_);
                    bl = true;
                }
            }
            matrices.pop();

            if (bl) {
                return;
            }

            AbstractTwitterScreen.TweetList.TweetEntry e = this.getEntryAtPosition(p_render_1_, p_render_2_);
            if (this.hoveringEntry != null && this.hoveringEntry.summary != null) {
                this.isHovering = AbstractTwitterScreen.this.renderTwitterUser(matrices, this.hoveringEntry.summary, this.getRowLeft() - 60, this.hoveringEntry.getY() + this.hoveringEntry.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
                if (!this.isHovering && this.fade < 0) {
                    this.hoveringEntry = null;
                    this.fade = 0;
                }
            } else if (e != null && e.summary != null && e.mayClickIcon(p_render_1_, p_render_2_)) {
                this.hoveringEntry = e;
                this.isHovering = AbstractTwitterScreen.this.renderTwitterUser(matrices, e.summary, this.getRowLeft() - 60, e.getY() + e.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
                this.fade = 10;
            }
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            for (ImmutableList<ClickableWidget> clickableWidgets : this.children().stream().map(AbstractTwitterTweetList.AbstractTwitterListEntry::getOverlayButtons).toList()) {
                for (ClickableWidget clickableWidget : clickableWidgets.stream().filter(clickableWidget -> clickableWidget.active && clickableWidget.visible).toList()) {
                    if (clickableWidget.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                        this.children().forEach(TweetEntry::hideAllOverlayButtons);
                        return true;
                    }
                }
            }

            this.children().forEach(TweetEntry::hideAllOverlayButtons);
            return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        @Override
        protected boolean isFocused() {
            return AbstractTwitterScreen.this.getFocused() == this;
        }

        @Environment(EnvType.CLIENT)
        protected class TweetEntry extends ExtendedTwitterTweetList.AbstractTwitterListEntry<AbstractTwitterScreen.TweetList.TweetEntry> {
            @Nullable
            protected final TweetSummary summary;
            @Nullable
            protected final TweetSummary retweetedSummary;
            @Nullable
            protected final TweetSummary quoteSourceSummary;
            protected final List<OrderedText> strings;
            protected final List<OrderedText> quotedTweetStrings;
            protected final int retweetedUserNameHeight;
            protected int height;
            protected int y;
            protected int photoRenderingWidth;
            protected int photoRenderingHeight;
            @Nullable
            protected TwitterButton replyButton;
            @Nullable
            protected TwitterButton retweetButton;
            @Nullable
            protected ButtonWidget retweetButton$retweet;
            @Nullable
            protected ButtonWidget retweetButton$quoteRetweet;
            @Nullable
            protected TwitterButton favoriteButton;
            @Nullable
            protected TwitterButton shareButton;
            protected int fourBtnHeightOffset;

            protected TweetEntry(@Nullable TweetSummary tweet) {
                if (tweet != null) {
                    boolean flag = tweet.getRetweetedSummary() != null;
                    this.summary = flag ? tweet.getRetweetedSummary() : tweet;
                    this.retweetedSummary = flag ? tweet : null;
                    this.quoteSourceSummary = this.summary.getQuotedTweetSummary();
                    this.strings = AbstractTwitterScreen.this.wrapLines(new TweetText(this.summary.getText()), AbstractTwitterScreen.TweetList.this.getRowWidth() - 25);
                    this.quotedTweetStrings = this.quoteSourceSummary != null ? AbstractTwitterScreen.this.wrapLines(new TweetText(this.quoteSourceSummary.getText()), AbstractTwitterScreen.TweetList.this.getRowWidth() - 40) : Lists.newArrayList();
                    this.photoRenderingWidth = TweetList.this.getRowWidth() - 30;
                    this.photoRenderingHeight = (int) (0.5625F * this.photoRenderingWidth);
                    this.height = ((this.strings.size() - 1) * AbstractTwitterScreen.this.textRenderer.fontHeight) + 10 + 30;
                    this.height += this.summary.isIncludeImages() || this.summary.isIncludeVideo() ? this.photoRenderingHeight + 3 : 0;
                    this.retweetedUserNameHeight = flag ? AbstractTwitterScreen.this.wrapUserNameToWidth(this.retweetedSummary, AbstractTwitterScreen.TweetList.this.getRowWidth() - 24).size() * AbstractTwitterScreen.this.textRenderer.fontHeight : 0;
                    this.height += this.retweetedUserNameHeight;
                    this.height += this.quoteSourceSummary != null ? 20 + this.quotedTweetStrings.size() * AbstractTwitterScreen.this.textRenderer.fontHeight : 0;
                    this.fourBtnHeightOffset = this.height - 14;
                } else {
                    this.summary = this.retweetedSummary = this.quoteSourceSummary = null;
                    this.strings = this.quotedTweetStrings = Lists.newArrayList();
                    this.height = this.retweetedUserNameHeight = this.fourBtnHeightOffset = this.photoRenderingWidth = this.photoRenderingHeight = 0;
                }
            }

            @Override
            public void tick() {
                this.updateButtonY(this.fourBtnHeightOffset + this.y);
            }

            @Override
            public void init() {
                int i = AbstractTwitterScreen.TweetList.this.getRowLeft() + 24;
                int h = AbstractTwitterScreen.TweetList.this.getRowWidth();
                int j = (h - 64) / 3;

                if (this.summary != null) {
                    this.replyButton = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, REPLY, 16, 32, 16, 16, (p) -> {
                        AbstractTwitterScreen.this.client.setScreen(new TwitterReplyScreen(AbstractTwitterScreen.this, this.summary));
                    }));

                    i += j;

                    boolean bl = this.summary.isRetweeted();
                    this.retweetButton = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, bl ? 0 : 16, bl ? RETWEETED : RETWEET, 16, bl ? 16 : 32, 16, 16, (p) -> {
                        if (!this.summary.getUser().isProtected()) {
                            this.showRetweetButtons();
                        }
                    }));

                    this.retweetButton$retweet = this.addOverlayButton(new ButtonWidget(i + 5 - h / 4, this.fourBtnHeightOffset, h / 2, 20, bl ? new TranslatableText("tw.unretweet") : new TranslatableText("tw.retweet"), button -> {
                        this.hideRetweetButtons();
                        try {
                            if (this.summary.isRetweeted()) {
                                TwitterForMC.mcTwitter.unRetweetStatus(this.summary.getId());
                                this.summary.retweet(false);
                                this.retweetButton$retweet.setMessage(new TranslatableText("tw.retweet"));
                                this.retweetButton.setImage(RETWEET);
                                this.retweetButton.setWhenHovered(16);
                                this.retweetButton.setSize(16, 32);
                            } else {
                                TwitterForMC.mcTwitter.retweetStatus(this.summary.getId());
                                this.summary.retweet(true);
                                this.retweetButton$retweet.setMessage(new TranslatableText("tw.unretweet"));
                                this.retweetButton.setImage(RETWEETED);
                                this.retweetButton.setWhenHovered(0);
                                this.retweetButton.setSize(16, 16);
                            }
                        } catch (TwitterException e) {
                            AbstractTwitterScreen.this.accept(new TranslatableText("tw.failed.retweet", e.getErrorMessage()));
                        }
                    }));

                    this.retweetButton$quoteRetweet = this.addOverlayButton(new FunctionalButtonWidget(i + 5 - h / 4, this.fourBtnHeightOffset + 20, h / 2, 20, new TranslatableText("tw.quote.tweet"), button -> {
                        this.hideRetweetButtons();

                    }, integer -> integer + 20));

                    this.hideRetweetButtons();

                    i += j;

                    this.favoriteButton = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, this.summary.isFavorited() ? 0 : 16, this.summary.isFavorited() ? FAVORITED : FAVORITE, 16, this.summary.isFavorited() ? 16 : 32, 16, 16, (b) -> {
                        try {
                            if (this.summary.isFavorited()) {
                                TwitterForMC.mcTwitter.destroyFavorite(this.summary.getId());
                                this.summary.favorite(false);
                                ((ChangeableImageButton) b).setImage(FAVORITE);
                                ((ChangeableImageButton) b).setWhenHovered(16);
                                ((ChangeableImageButton) b).setSize(16, 32);
                            } else {
                                TwitterForMC.mcTwitter.createFavorite(this.summary.getId());
                                this.summary.favorite(true);
                                ((ChangeableImageButton) b).setImage(FAVORITED);
                                ((ChangeableImageButton) b).setWhenHovered(0);
                                ((ChangeableImageButton) b).setSize(16, 16);
                            }
                        } catch (TwitterException e) {
                            AbstractTwitterScreen.this.accept(new TranslatableText("tw.failed.like", e.getErrorMessage()));
                        }
                    }));

                    i += j;

                    this.shareButton = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, SHARE, 16, 32, 16, 16, (p) -> {
                        AbstractTwitterScreen.this.client.keyboard.setClipboard(this.summary.getTweetURL());
                        AbstractTwitterScreen.this.accept(new TranslatableText("tw.copy.tweeturl.to.clipboard"));
                    }));
                }
            }

            protected void hideAllOverlayButtons() {
                this.overlayButtons.forEach(clickableWidget -> clickableWidget.active = clickableWidget.visible = false);
            }

            private void hideOrShowRetweetButtons(boolean flag) {
                if (this.retweetButton$retweet != null && this.retweetButton$quoteRetweet != null) {
                    this.retweetButton$retweet.active = this.retweetButton$quoteRetweet.active = this.retweetButton$retweet.visible = this.retweetButton$quoteRetweet.visible = flag;
                }
            }

            protected void hideRetweetButtons() {
                this.hideOrShowRetweetButtons(false);
            }

            protected void showRetweetButtons() {
                this.hideOrShowRetweetButtons(true);
            }

            @Override
            public void render(MatrixStack matrices, int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float delta) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                int nowY = rowTop;
                nowY = AbstractTwitterScreen.this.renderRetweetedUser(matrices, this.retweetedSummary, rowLeft + 6, rowLeft + 24, nowY, rowWidth - 24);

                this.renderIcon(matrices, rowLeft, nowY);
                RenderSystem.disableBlend();

                if (this.summary != null) {
                    this.renderUserName(matrices, this.summary, rowLeft + 24, nowY, rowWidth - 24);
                }

                for (int i = 0; i < this.strings.size(); i++) {
                    AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, this.strings.get(i), (float) (rowLeft + 24), (float) (nowY + 10 + i * AbstractTwitterScreen.this.textRenderer.fontHeight), 16777215);
                }
                nowY += 10 + this.strings.size() * AbstractTwitterScreen.this.textRenderer.fontHeight;

                if (this.summary != null && this.summary.isIncludeVideo()) {
                    AbstractTwitterScreen.this.fillGradient(matrices, rowLeft + 24, nowY, rowLeft + 24 + this.photoRenderingWidth, nowY + this.photoRenderingHeight, -1072689136, -804253680);
                    if (mouseX >= rowLeft + 24 && mouseX <= rowLeft + 24 + this.photoRenderingWidth && mouseY >= nowY && mouseY <= nowY + this.photoRenderingHeight) {
                        AbstractTwitterScreen.this.renderTooltip(matrices, new TranslatableText("tw.play.video"), mouseX, mouseY);
                    }
                    nowY += this.photoRenderingHeight;
                }

                nowY += this.renderPhotos(matrices, rowLeft, nowY);

                if (this.quoteSourceSummary != null) {
                    nowY += 10;
                    ImageDataDeliverer qsIco = this.quoteSourceSummary.getUserIconData();
                    if (qsIco.readyToRender()) {
                        TwitterForMC.getTextureManager().bindTexture(qsIco.deliver());
                        drawTexture(matrices, rowLeft + 24 + 5, nowY, 0.0F, 0.0F, 10, 10, 10, 10);
                    }
                    this.renderUserName(matrices, this.quoteSourceSummary, rowLeft + 24 + 5 + 10 + 4, nowY, AbstractTwitterScreen.TweetList.this.getRowWidth() - 24 - 5 - 10 - 4 - 10);
                    for (int i = 0; i < this.quotedTweetStrings.size(); i++) {
                        AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, this.quotedTweetStrings.get(i), rowLeft + 24 + 5, nowY + 10 + i * AbstractTwitterScreen.this.textRenderer.fontHeight, 16777215);
                    }
                    nowY += 10 + this.quotedTweetStrings.size() * AbstractTwitterScreen.this.textRenderer.fontHeight;
                }

                this.renderButtons(matrices, mouseX, mouseY, delta);

                if (this.summary != null) {
                    if (this.summary.getRetweetCount() != 0 && this.retweetButton != null) {
                        AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, Text.of("" + this.summary.getRetweetCountF()), this.retweetButton.x + 16.0F, this.retweetButton.y, 11184810);
                    }
                    if (this.summary.getFavoriteCount() != 0 && this.favoriteButton != null) {
                        AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, Text.of("" + this.summary.getFavoriteCountF()), this.favoriteButton.x + 16.0F, this.favoriteButton.y, 11184810);
                    }
                }
            }

            public void renderUserName(MatrixStack matrices, TweetSummary summary, int x, int y, int width) {
                boolean p = summary.getUser().isProtected();
                boolean v = summary.getUser().isVerified();

                int threeBoldWidth = AbstractTwitterScreen.this.textRenderer.getWidth(THREE_PERIOD);
                int threeWidth = AbstractTwitterScreen.this.textRenderer.getWidth(THREE_PERIOD_GRAY);
                Text time = new LiteralText("・" + summary.getDifferenceTime()).formatted(Formatting.GRAY);
                int timeWidth = AbstractTwitterScreen.this.textRenderer.getWidth(time);
                Text screenName = new TweetText(summary.getScreenName()).formatted(Formatting.GRAY);
                Text name = new TweetText(summary.getUser().getName()).formatted(Formatting.BOLD);

                int protectedVerifiedWidth = (p ? 10 : 0) + (v ? 10 : 0);
                List<OrderedText> nameFormatted = AbstractTwitterScreen.this.wrapLines(name, width - protectedVerifiedWidth - timeWidth);
                boolean isOver = nameFormatted.size() > 1;
                List<OrderedText> nameFormatted2 = isOver ? AbstractTwitterScreen.this.wrapLines(name, width - protectedVerifiedWidth - timeWidth - threeBoldWidth) : nameFormatted;

                OrderedText formattedName = nameFormatted2.size() == 1 ? nameFormatted2.get(0) : OrderedText.concat(nameFormatted2.get(0), THREE_PERIOD.asOrderedText());
                int formattedNameWidth = AbstractTwitterScreen.this.getWidthWithEmoji(formattedName);
                AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, formattedName, x, y, 16777215);
                x += formattedNameWidth;
                if (p) {
                    x += AbstractTwitterScreen.this.renderProtected(matrices, x, y);
                }
                if (v) {
                    x += AbstractTwitterScreen.this.renderVerified(matrices, x, y);
                }

                List<OrderedText> screenNameFormatted = AbstractTwitterScreen.this.wrapLines(screenName, width - formattedNameWidth - protectedVerifiedWidth - timeWidth - threeWidth);
                if (!isOver) {
                    OrderedText text = screenNameFormatted.size() == 1 ? screenNameFormatted.get(0) : OrderedText.concat(screenNameFormatted.get(0), THREE_PERIOD_GRAY.asOrderedText());
                    AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, text, x, y, 11184810);
                    x += AbstractTwitterScreen.this.getWidthWithEmoji(text);
                }
                AbstractTwitterScreen.this.drawWithShadowAndEmoji(matrices, time, x, y, 11184810);
            }

            public void renderIcon(MatrixStack matrices, int x, int y) {
                ImageDataDeliverer icon = this.summary != null ? this.summary.getUserIconData() : null;
                if (icon != null && icon.readyToRender()) {
                    TwitterForMC.getTextureManager().bindTexture(icon.deliver());
                    DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
                }
            }

            public int renderPhotos(MatrixStack matrices, int rowLeft, int rowTop) {
                if (this.summary != null) {
                    int w2 = this.photoRenderingWidth / 2;
                    int h2 = this.photoRenderingHeight / 2;
                    List<TwitterPhotoMedia> p = this.summary.getPhotoMedias();
                    if (p.size() == 1) {
                        TwitterPhotoMedia media = p.get(0);
                        if (media.readyToRender()) {
                            Dimension d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(this.photoRenderingWidth, this.photoRenderingHeight));
                            TwitterForMC.getTextureManager().bindTexture(media.getData());
                            DrawableHelper.drawTexture(matrices, rowLeft + 24, rowTop, 0.0F, (float) (d.height - this.photoRenderingHeight) / 2, this.photoRenderingWidth, this.photoRenderingHeight, d.width, d.height);
                        }
                    } else if (p.size() == 2) {
                        for (int i = 0; i < 2; i++) {
                            TwitterPhotoMedia media = p.get(i);
                            if (media.readyToRender()) {
                                Dimension d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, this.photoRenderingHeight));
                                TwitterForMC.getTextureManager().bindTexture(media.getData());
                                DrawableHelper.drawTexture(matrices, rowLeft + 24 + i * w2 + 1, rowTop, 0.0F, (float) (d.height - this.photoRenderingHeight) / 2, w2, this.photoRenderingHeight, d.width, d.height);
                            }
                        }
                    } else if (p.size() == 3) {
                        for (int i = 0; i < 3; i++) {
                            TwitterPhotoMedia media = p.get(i);
                            if (media.readyToRender()) {
                                Dimension d;
                                TwitterForMC.getTextureManager().bindTexture(media.getData());
                                if (i == 0) {
                                    d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, this.photoRenderingHeight));
                                    DrawableHelper.drawTexture(matrices, rowLeft + 24, rowTop, 0.0F, (float) (d.height - this.photoRenderingHeight) / 2, w2, this.photoRenderingHeight, d.width, d.height);
                                } else if (i == 1) {
                                    d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, h2 - 1));
                                    DrawableHelper.drawTexture(matrices, rowLeft + 24 + w2 + 1, rowTop, 0.0F, (float) (d.height - h2 - 1) / 2, w2, h2 - 1, d.width, d.height - 1);
                                } else {
                                    d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, h2 - 1));
                                    DrawableHelper.drawTexture(matrices, rowLeft + 24 + w2 + 1, rowTop + h2 + 1, 0.0F, (float) (d.height - h2 - 1) / 2, w2, h2 - 1, d.width, d.height - 1);
                                }
                            }
                        }
                    } else if (p.size() == 4) {
                        for (int i = 0; i < 4; i++) {
                            TwitterPhotoMedia media = p.get(i);
                            if (media.readyToRender()) {
                                Dimension d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, h2));
                                TwitterForMC.getTextureManager().bindTexture(media.getData());
                                if (i % 2 == 0) {
                                    DrawableHelper.drawTexture(matrices, rowLeft + 24, rowTop + ((i / 2) * (h2 + 1)), 0.0F, (float) (d.height - h2) / 2, w2, h2, d.width, d.height);
                                } else {
                                    DrawableHelper.drawTexture(matrices, rowLeft + 24 + w2 + 1, rowTop + ((i / 3) * (h2 + 1)), 0.0F, (float) (d.height - h2) / 2, w2, h2, d.width, d.height);
                                }
                            }
                        }
                    }

                    return p.size() == 0 ? 0 : this.photoRenderingHeight;
                }

                return 0;
            }

            @Override
            public boolean mouseClicked(double x, double y, int button) {
                if (this.summary != null) {
                    int i = AbstractTwitterScreen.TweetList.this.getRowLeft() + 24;
                    int j = this.y + this.retweetedUserNameHeight + 11 + this.strings.size() * AbstractTwitterScreen.this.textRenderer.fontHeight;
                    int k = this.summary.getPhotoMediaLength();
                    int w2 = this.photoRenderingWidth / 2;
                    int h2 = this.photoRenderingHeight / 2;
                    boolean xMore = x >= i;
                    boolean yMore = y >= j;
                    boolean b = xMore && x <= i + this.photoRenderingWidth && yMore && y <= j + this.photoRenderingHeight;
                    boolean b1 = xMore && x <= i + w2 && yMore && y <= j + this.photoRenderingHeight;
                    boolean b2 = x >= i + w2 + 1 && x <= i + this.photoRenderingWidth && yMore && y <= j + h2;

                    if (k == 1) {
                        if (b) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                        }
                    } else if (k == 2) {
                        if (b1) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                        } else if (x >= i + w2 + 1 && x <= i + this.photoRenderingWidth && yMore && y <= j + this.photoRenderingHeight) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
                        }
                    } else if (k == 3) {
                        if (b1) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                        } else if (b2) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
                        } else if (xMore && x <= i + this.photoRenderingWidth && y >= j + h2 + 1 && y <= j + this.photoRenderingHeight) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 2);
                        }
                    } else if (k == 4) {
                        if (xMore && x <= i + w2 && yMore && y <= j + h2) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                        } else if (b2) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
                        } else if (xMore && x <= i + w2 && y >= j + h2 + 1 && y <= j + this.photoRenderingHeight) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 2);
                        } else if (x >= i + w2 + 1 && x <= i + this.photoRenderingWidth && y >= j + h2 + 1 && y <= j + this.photoRenderingHeight) {
                            return this.displayTwitterPhotoAndShowStatusScreen(button, 3);
                        }
                    }

                    if (this.summary.isIncludeVideo()) {
                        if (b) {
                            return this.videoClicked(button);
                        }
                    }
                }

                for (ClickableWidget w : this.buttons) {
                    if (w.mouseClicked(x, y, button)) {
                        return true;
                    }
                }

                if (button == 0) {
                    if (this.summary != null && TweetList.this.getSelected() == this) {
                        AbstractTwitterScreen.this.displayStatus(AbstractTwitterScreen.this, this.summary);
                    } else {
                        TweetList.this.setSelected(this);
                    }
                    return true;
                } else {
                    return false;
                }
            }

            protected boolean mayClickIcon(double x, double y) {
                int i = AbstractTwitterScreen.TweetList.this.getRowLeft();
                int j = this.y + this.retweetedUserNameHeight;
                return this.summary != null && TweetList.this.top < y && TweetList.this.bottom > y && x > i && x < i + 16 && y > j && y < j + 16;
            }

            protected boolean displayTwitterPhotoAndShowStatusScreen(int mouseButton, int index) {
                if (this.summary != null) {
                    AbstractTwitterScreen.this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    if (mouseButton == 0) {
                        AbstractTwitterScreen.this.client.setScreen(new TwitterPhotoAndShowStatusScreen(AbstractTwitterScreen.this, this.summary, index));
                    } else if (mouseButton == 1) {
                        //TODO save picture action;
                    }

                    return true;
                }

                return false;
            }

            protected boolean videoClicked(int mouseButton) {
                if (this.summary != null) {
                    if (mouseButton == 0) {
                        Util.getOperatingSystem().open(this.summary.getVideoURL());
                    } else if (mouseButton == 1) {
                        AbstractTwitterScreen.this.client.setScreen(new DownloadTwitterVideoScreen(AbstractTwitterScreen.this, this.summary));
                    }
                }

                return false;
            }

            @Override
            public int getHeight() {
                return this.height;
            }

            @Override
            public void setHeight(int height) {
                this.height = height;
                this.fourBtnHeightOffset = this.height - 14;
                this.buttons.clear();
                this.overlayButtons.clear();
                this.init();
                TweetList.this.calcAllHeight();
                TweetList.this.calcAverage();
                TweetList.this.setY(-(int) TweetList.this.getScrollAmount());
            }

            @Override
            public int getY() {
                return this.y;
            }

            @Override
            public void setY(int y) {
                this.y = y;
                this.updateButtonY(this.fourBtnHeightOffset + this.y);
            }

            protected void updateButtonY(int y) {
                this.buttons.forEach(clickableWidget -> {
                    if (clickableWidget instanceof FunctionalButtonWidget functionalButtonWidget) {
                        functionalButtonWidget.y = functionalButtonWidget.yFunction.apply(y);
                    } else {
                        clickableWidget.y = y;
                    }
                });

                this.overlayButtons.forEach(clickableWidget -> {
                    if (clickableWidget instanceof FunctionalButtonWidget functionalButtonWidget) {
                        functionalButtonWidget.y = functionalButtonWidget.yFunction.apply(y);
                    } else {
                        clickableWidget.y = y;
                    }
                });
            }

            @Override
            public boolean equals(Object obj) {
                if (this.summary != null) {
                    return this.summary.equals(obj);
                }

                return super.equals(obj);
            }

            @Override
            public int hashCode() {
                if (this.summary != null) {
                    return this.summary.hashCode();
                }

                return super.hashCode();
            }
        }
    }
}
