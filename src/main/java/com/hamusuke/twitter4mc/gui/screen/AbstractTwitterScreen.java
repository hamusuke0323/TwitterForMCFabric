package com.hamusuke.twitter4mc.gui.screen;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
import com.hamusuke.twitter4mc.gui.widget.ChangeableImageButton;
import com.hamusuke.twitter4mc.gui.widget.TwitterButton;
import com.hamusuke.twitter4mc.gui.widget.list.ExtendedTwitterTweetList;
import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import twitter4j.TwitterException;
import twitter4j.User;

import java.awt.*;
import java.io.InputStream;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class AbstractTwitterScreen extends ParentalScreen implements DisplayableMessage {
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
    protected static final String THREE_PERIOD = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
    protected static final String THREE_PERIOD_GRAY = new LiteralText("...").formatted(Formatting.GRAY).asFormattedString();

    @Nullable
    protected AbstractTwitterScreen.TweetList list;
    @Nullable
    protected String message;
    protected int fade;
    protected boolean isFade;
    protected final List<AbstractButtonWidget> renderLaterButtons = Lists.newArrayList();

    protected AbstractTwitterScreen(Text title, @Nullable Screen parent) {
        super(title, parent);
    }

    public void tick() {
        if (this.list != null) {
            this.list.tick();
        }

        if (this.message != null) {
            if (this.fade <= 0) {
                this.message = null;
                this.fade = 0;
                this.isFade = false;
            } else if (this.fade <= 20) {
                this.isFade = true;
            }

            this.fade--;
        }

        super.tick();
    }

    protected <T extends AbstractButtonWidget> T addRenderLaterButton(T button) {
        this.renderLaterButtons.add(button);
        this.children.add(button);
        return button;
    }

    public void renderButtonLater(int mouseX, int mouseY, float tickDelta) {
        for (AbstractButtonWidget abstractButtonWidget : this.renderLaterButtons) {
            abstractButtonWidget.render(mouseX, mouseY, tickDelta);
        }
    }

    protected void clearChildren() {
        super.clearChildren();
        this.renderLaterButtons.clear();
    }

    public void renderMessage() {
        if (this.message != null) {
            List<String> list = this.font.wrapStringToWidthAsList(this.message, this.width / 2);
            this.renderTooltip(list, (this.width - this.font.getStringWidth(list.get(0))) / 2, this.height - list.size() * this.font.fontHeight);
        }
    }

    public void accept(String errorMsg) {
        this.message = errorMsg;
        this.fade = 100;
    }

    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (this.list != null && this.list.hoveringEntry != null && this.list.hoveringEntry.summary != null && this.list.hoveringEntry.mayClickIcon(p_mouseClicked_1_, p_mouseClicked_3_)) {
            this.minecraft.openScreen(new TwitterShowUserScreen(this, this.list.hoveringEntry.summary.getUser()));
            return true;
        } else if (this.list != null && !this.list.isHovering) {
            return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        return false;
    }

    public boolean renderTwitterUser(TweetSummary summary, int x, int y, int mouseX, int mouseY) {
        User user = summary.getUser();
        InputStream icon = summary.getUserIconData();
        List<String> desc = this.font.wrapStringToWidthAsList(user.getDescription(), Math.min(this.width / 2, 150));
        String f1 = new LiteralText(user.getFriendsCount() + "").formatted(Formatting.BOLD).asFormattedString();
        String follow = f1 + " " + FOLLOW.asFormattedString();
        String f2 = new LiteralText(user.getFollowersCount() + "").formatted(Formatting.BOLD).asFormattedString();
        String follower = f2 + " " + FOLLOWER.asFormattedString();
        List<String> ff = this.font.wrapStringToWidthAsList(follow + "  " + follower, 150);

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int i = 0;

        for (String s : desc) {
            int j = this.font.getStringWidth(s);

            if (j > i) {
                i = j;
            }
        }

        for (String s1 : ff) {
            int j2 = this.font.getStringWidth(s1);
            if (j2 > i) {
                i = j2;
            }
        }

        int i2 = y;
        int k = 0;
        k += icon != null ? 22 : 0;
        k += user.getName().isEmpty() ? 0 : 10;
        k += user.getScreenName().isEmpty() ? 0 : 10;
        k += 4 + (desc.size() * (this.font.fontHeight + 1)) + 4;
        k += ff.size() == 1 ? 10 : 20 + 2;

        if (i2 + k + 6 > this.height - 20) {
            i2 = this.height - 20 - k - 6;
        }

        this.fillGradient(x - 3, i2 - 4, x + i + 3, i2 - 3, -267386864, -267386864);
        this.fillGradient(x - 3, i2 + k + 3, x + i + 3, i2 + k + 4, -267386864, -267386864);
        this.fillGradient(x - 3, i2 - 3, x + i + 3, i2 + k + 3, -267386864, -267386864);
        this.fillGradient(x - 4, i2 - 3, x - 3, i2 + k + 3, -267386864, -267386864);
        this.fillGradient(x + i + 3, i2 - 3, x + i + 4, i2 + k + 3, -267386864, -267386864);
        this.fillGradient(x - 3, i2 - 3 + 1, x - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(x + i + 2, i2 - 3 + 1, x + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
        this.fillGradient(x - 3, i2 - 3, x + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
        this.fillGradient(x - 3, i2 + k + 2, x + i + 3, i2 + k + 3, 1344798847, 1344798847);
        MatrixStack matrixstack = new MatrixStack();
        VertexConsumerProvider.Immediate vertexConsumerProvider$immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        Matrix4f matrix4f = matrixstack.peek().getModel();

        int yy = i2;
        if (icon != null) {
            TwitterForMC.getTextureManager().bindTexture(icon);
            blit(x, i2, 0.0F, 0.0F, 20, 20, 20, 20);
            i2 += 20;
        }

        int yyy = i2;
        boolean p = user.isProtected();
        boolean v = user.isVerified();
        int m = (p ? 10 : 0) + (v ? 10 : 0);
        String name = new LiteralText(user.getName()).formatted(Formatting.BOLD).asFormattedString();
        List<String> nameFormatted = this.font.wrapStringToWidthAsList(name, i - this.font.getStringWidth(THREE_PERIOD) - m);
        int n = this.font.draw(nameFormatted.size() == 1 ? nameFormatted.get(0) : nameFormatted.get(0) + THREE_PERIOD, (float) x, (float) i2 + 2, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
        this.font.draw(new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString(), (float) x, (float) i2 + 12, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);

        for (String s1 : desc) {
            if (s1 != null) {
                this.font.draw(s1, (float) x, (float) i2 + 26, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
            }

            i2 += 10;
        }

        if (ff.size() == 1) {
            this.font.draw(ff.get(0), (float) x, (float) i2 + 30, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
        } else {
            this.font.draw(follow, (float) x, (float) i2 + 30, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
            this.font.draw(follower, (float) x, (float) i2 + 30 + 10, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
        }

        vertexConsumerProvider$immediate.draw();

        if (p) {
            n += this.renderProtected(n, yyy + 2);
        }
        if (v) {
            this.renderVerified(n, yyy + 2);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();

        return x - 4 < mouseX && x + i + 4 > mouseX && yy - 4 < mouseY && yy + k + 4 > mouseY;
    }

    public int renderRetweetedUser(@Nullable TweetSummary retweetedSummary, int iconX, int x, int y, int width) {
        if (retweetedSummary != null) {
            this.minecraft.getTextureManager().bindTexture(RETWEET_USER);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(iconX, y, 0.0F);
            RenderSystem.scalef(0.625F, 0.625F, 0.625F);
            DrawableHelper.blit(0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.popMatrix();
            List<String> names = this.wrapUserNameToWidth(retweetedSummary, width);
            for (int i = 0; i < names.size(); i++) {
                this.font.drawWithShadow(names.get(i), x, y + i * this.font.fontHeight, 11184810);
            }
            return y + names.size() * this.font.fontHeight;
        }

        return y;
    }

    public List<String> wrapUserNameToWidth(TweetSummary summary, int width) {
        return this.font.wrapStringToWidthAsList(I18n.translate("tw.retweeted.user", summary.getUser().getName()), width);
    }

    public int renderProtected(int x, int y) {
        this.minecraft.getTextureManager().bindTexture(PROTECTED);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0.0F);
        RenderSystem.scalef(0.625F, 0.625F, 0.625F);
        DrawableHelper.blit(0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.popMatrix();
        return 10;
    }

    public int renderVerified(int x, int y) {
        this.minecraft.getTextureManager().bindTexture(VERIFIED);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0.0F);
        RenderSystem.scalef(0.625F, 0.625F, 0.625F);
        DrawableHelper.blit(0, 0, 0, 0, 16, 16, 16, 16);
        RenderSystem.popMatrix();
        return 10;
    }

    @Nullable
    public AbstractTwitterScreen.TweetList getList() {
        return this.list;
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

        public void tick() {
            this.fade = this.isHovering ? 10 : this.fade - 1;
            this.children().forEach(AbstractTwitterScreen.TweetList.TweetEntry::tick);
            super.tick();
        }

        protected int getScrollbarPosition() {
            return AbstractTwitterScreen.this.width - 5;
        }

        public int getRowWidth() {
            return AbstractTwitterScreen.this.width / 2;
        }

        protected void renderBackground() {
        }

        protected void renderHoleBackground(int top, int bottom, int alphaTop, int alphaBottom) {
            this.fillGradient(this.left + this.width, bottom, this.left, top, -15392725, -15392725);
        }

        public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
            super.render(p_render_1_, p_render_2_, p_render_3_);

            AbstractTwitterScreen.TweetList.TweetEntry e = this.getEntryAtPosition(p_render_1_, p_render_2_);
            if (this.hoveringEntry != null && this.hoveringEntry.summary != null) {
                this.isHovering = AbstractTwitterScreen.this.renderTwitterUser(this.hoveringEntry.summary, this.getRowLeft() - 60, this.hoveringEntry.getY() + this.hoveringEntry.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
                if (!this.isHovering && this.fade < 0) {
                    this.hoveringEntry = null;
                    this.fade = 0;
                }
            } else if (e != null && e.summary != null && e.mayClickIcon(p_render_1_, p_render_2_)) {
                this.hoveringEntry = e;
                this.isHovering = AbstractTwitterScreen.this.renderTwitterUser(e.summary, this.getRowLeft() - 60, e.getY() + e.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
                this.fade = 10;
            }
        }

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
            protected final List<String> strings;
            protected final List<String> quotedTweetStrings;
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
                    this.strings = AbstractTwitterScreen.this.font.wrapStringToWidthAsList(this.summary.getText(), AbstractTwitterScreen.TweetList.this.getRowWidth() - 25);
                    this.quotedTweetStrings = this.quoteSourceSummary != null ? AbstractTwitterScreen.this.font.wrapStringToWidthAsList(this.quoteSourceSummary.getText(), AbstractTwitterScreen.TweetList.this.getRowWidth() - 40) : Lists.newArrayList();
                    this.photoRenderingWidth = TweetList.this.getRowWidth() - 30;
                    this.photoRenderingHeight = (int) (0.5625F * this.photoRenderingWidth);
                    this.height = ((this.strings.size() - 1) * AbstractTwitterScreen.this.font.fontHeight) + 10 + 30;
                    this.height += this.summary.isIncludeImages() || this.summary.isIncludeVideo() ? this.photoRenderingHeight + 3 : 0;
                    this.retweetedUserNameHeight = flag ? AbstractTwitterScreen.this.wrapUserNameToWidth(this.retweetedSummary, AbstractTwitterScreen.TweetList.this.getRowWidth() - 24).size() * AbstractTwitterScreen.this.font.fontHeight : 0;
                    this.height += this.retweetedUserNameHeight;
                    this.height += this.quoteSourceSummary != null ? 20 + this.quotedTweetStrings.size() * AbstractTwitterScreen.this.font.fontHeight : 0;
                    this.fourBtnHeightOffset = this.height - 14;
                } else {
                    this.summary = this.retweetedSummary = this.quoteSourceSummary = null;
                    this.strings = this.quotedTweetStrings = Lists.newArrayList();
                    this.height = this.retweetedUserNameHeight = this.fourBtnHeightOffset = this.photoRenderingWidth = this.photoRenderingHeight = 0;
                }
            }

            public void tick() {
                this.updateButtonY(this.fourBtnHeightOffset + this.y);
            }

            public void init() {
                int i = AbstractTwitterScreen.TweetList.this.getRowLeft() + 24;

                this.replyButton = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, REPLY, 16, 32, 16, 16, (p) -> {

                }));

                this.retweetButton = this.addButton(new TwitterButton(i + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, RETWEET, 16, 32, 16, 16, (p) -> {

                }));

                if (this.summary != null) {
                    this.favoriteButton = this.addButton(new TwitterButton(i + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, this.summary.isFavorited() ? 0 : 16, this.summary.isFavorited() ? FAVORITED : FAVORITE, 16, this.summary.isFavorited() ? 16 : 32, 16, 16, (b) -> {
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
                            AbstractTwitterScreen.this.accept(I18n.translate("tw.failed.like", e.getErrorMessage()));
                        }
                    }));
                }

                this.shareButton = this.addButton(new TwitterButton(i + 60 + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, SHARE, 16, 32, 16, 16, (p) -> {

                }));
            }

            public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float delta) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                int nowY = rowTop;
                nowY = AbstractTwitterScreen.this.renderRetweetedUser(this.retweetedSummary, rowLeft + 6, rowLeft + 24, nowY, rowWidth - 24);

                this.renderIcon(rowLeft, nowY);
                RenderSystem.disableBlend();

                if (this.summary != null) {
                    this.renderUserName(this.summary, rowLeft + 24, nowY, rowWidth - 24);
                }

                for (int i = 0; i < this.strings.size(); i++) {
                    AbstractTwitterScreen.this.font.drawWithShadow(this.strings.get(i), (float) (rowLeft + 24), (float) (nowY + 10 + i * AbstractTwitterScreen.this.font.fontHeight), 16777215);
                }
                nowY += 10 + this.strings.size() * AbstractTwitterScreen.this.font.fontHeight;

                if (this.summary != null && this.summary.isIncludeVideo()) {
                    AbstractTwitterScreen.this.fillGradient(rowLeft + 24, nowY, rowLeft + 24 + this.photoRenderingWidth, nowY + this.photoRenderingHeight, -1072689136, -804253680);
                    if (mouseX >= rowLeft + 24 && mouseX <= rowLeft + 24 + this.photoRenderingWidth && mouseY >= nowY && mouseY <= nowY + this.photoRenderingHeight) {
                        AbstractTwitterScreen.this.renderTooltip(I18n.translate("tw.play.video"), mouseX, mouseY);
                    }
                    nowY += this.photoRenderingHeight;
                }

                nowY += this.renderPhotos(rowLeft, nowY);

                if (this.quoteSourceSummary != null) {
                    nowY += 10;
                    InputStream qsIco = this.quoteSourceSummary.getUserIconData();
                    if (qsIco != null) {
                        TwitterForMC.getTextureManager().bindTexture(qsIco);
                        blit(rowLeft + 24 + 5, nowY, 0.0F, 0.0F, 10, 10, 10, 10);
                    }
                    this.renderUserName(this.quoteSourceSummary, rowLeft + 24 + 5 + 10 + 4, nowY, AbstractTwitterScreen.TweetList.this.getRowWidth() - 24 - 5 - 10 - 4 - 10);
                    for (int i = 0; i < this.quotedTweetStrings.size(); i++) {
                        AbstractTwitterScreen.this.font.drawWithShadow(this.quotedTweetStrings.get(i), rowLeft + 24 + 5, nowY + 10 + i * AbstractTwitterScreen.this.font.fontHeight, 16777215);
                    }
                    nowY += 10 + this.quotedTweetStrings.size() * AbstractTwitterScreen.this.font.fontHeight;
                }

                this.renderButtons(mouseX, mouseY, delta);

                if (this.summary != null) {
                    if (this.summary.getRetweetCount() != 0 && this.retweetButton != null) {
                        AbstractTwitterScreen.this.font.drawWithShadow("" + this.summary.getRetweetCountF(), this.retweetButton.x + 16.0F, this.retweetButton.y, 11184810);
                    }
                    if (this.summary.getFavoriteCount() != 0 && this.favoriteButton != null) {
                        AbstractTwitterScreen.this.font.drawWithShadow("" + this.summary.getFavoriteCountF(), this.favoriteButton.x + 16.0F, this.favoriteButton.y, 11184810);
                    }
                }
            }

            public void renderUserName(TweetSummary summary, int x, int y, int width) {
                boolean p = summary.getUser().isProtected();
                boolean v = summary.getUser().isVerified();

                int threeBoldWidth = AbstractTwitterScreen.this.font.getStringWidth(THREE_PERIOD);
                int threeWidth = AbstractTwitterScreen.this.font.getStringWidth(THREE_PERIOD_GRAY);
                String time = new LiteralText("・" + summary.getDifferenceTime()).formatted(Formatting.GRAY).asFormattedString();
                int timeWidth = AbstractTwitterScreen.this.font.getStringWidth(time);
                String screenName = new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString();
                String name = new LiteralText(summary.getUser().getName()).formatted(Formatting.BOLD).asFormattedString();

                int protectedVerifiedWidth = (p ? 10 : 0) + (v ? 10 : 0);
                List<String> nameFormatted = AbstractTwitterScreen.this.font.wrapStringToWidthAsList(name, width - protectedVerifiedWidth - timeWidth);
                boolean isOver = nameFormatted.size() > 1;
                List<String> nameFormatted2 = isOver ? AbstractTwitterScreen.this.font.wrapStringToWidthAsList(name, width - protectedVerifiedWidth - timeWidth - threeBoldWidth) : nameFormatted;

                String formattedName = nameFormatted2.size() == 1 ? nameFormatted2.get(0) : nameFormatted2.get(0) + THREE_PERIOD;
                int formattedNameWidth = AbstractTwitterScreen.this.font.getStringWidth(formattedName);
                AbstractTwitterScreen.this.font.drawWithShadow(formattedName, x, y, 16777215);
                x += formattedNameWidth;
                if (p) {
                    x += AbstractTwitterScreen.this.renderProtected(x, y);
                }
                if (v) {
                    x += AbstractTwitterScreen.this.renderVerified(x, y);
                }

                List<String> screenNameFormatted = AbstractTwitterScreen.this.font.wrapStringToWidthAsList(screenName, width - formattedNameWidth - protectedVerifiedWidth - timeWidth - threeWidth);
                if (!isOver) {
                    String s = screenNameFormatted.size() == 1 ? screenNameFormatted.get(0) : screenNameFormatted.get(0) + THREE_PERIOD_GRAY;
                    AbstractTwitterScreen.this.font.drawWithShadow(s, x, y, 11184810);
                    x += AbstractTwitterScreen.this.font.getStringWidth(s);
                }
                AbstractTwitterScreen.this.font.drawWithShadow(time, x, y, 11184810);
            }

            public void renderIcon(int x, int y) {
                InputStream icon = this.summary != null ? this.summary.getUserIconData() : null;
                if (icon != null) {
                    TwitterForMC.getTextureManager().bindTexture(icon);
                    DrawableHelper.blit(x, y, 0.0F, 0.0F, 16, 16, 16, 16);
                }
            }

            public int renderPhotos(int rowLeft, int rowTop) {
                if (this.summary != null) {
                    int w2 = this.photoRenderingWidth / 2;
                    int h2 = this.photoRenderingHeight / 2;
                    List<TwitterPhotoMedia> p = this.summary.getPhotoMedias();
                    if (p.size() == 1) {
                        TwitterPhotoMedia media = p.get(0);
                        InputStream data = media.getData();
                        if (data != null && media.canRendering()) {
                            Dimension d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(this.photoRenderingWidth, this.photoRenderingHeight));
                            TwitterForMC.getTextureManager().bindTexture(data);
                            DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, (float) (d.height - this.photoRenderingHeight) / 2, this.photoRenderingWidth, this.photoRenderingHeight, d.width, d.height);
                        }
                    } else if (p.size() == 2) {
                        for (int i = 0; i < 2; i++) {
                            TwitterPhotoMedia media = p.get(i);
                            InputStream data = media.getData();
                            if (data != null && media.canRendering()) {
                                Dimension d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, this.photoRenderingHeight));
                                TwitterForMC.getTextureManager().bindTexture(data);
                                DrawableHelper.blit(rowLeft + 24 + i * w2 + 1, rowTop, 0.0F, (float) (d.height - this.photoRenderingHeight) / 2, w2, this.photoRenderingHeight, d.width, d.height);
                            }
                        }
                    } else if (p.size() == 3) {
                        for (int i = 0; i < 3; i++) {
                            TwitterPhotoMedia media = p.get(i);
                            InputStream data = media.getData();
                            if (data != null && media.canRendering()) {
                                Dimension d;
                                TwitterForMC.getTextureManager().bindTexture(data);
                                if (i == 0) {
                                    d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, this.photoRenderingHeight));
                                    DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, (float) (d.height - this.photoRenderingHeight) / 2, w2, this.photoRenderingHeight, d.width, d.height);
                                } else if(i == 1) {
                                    d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, h2 - 1));
                                    DrawableHelper.blit(rowLeft + 24 + w2 + 1, rowTop, 0.0F, (float) (d.height - h2 - 1) / 2, w2, h2 - 1, d.width, d.height - 1);
                                } else {
                                    d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, h2 - 1));
                                    DrawableHelper.blit(rowLeft + 24 + w2 + 1, rowTop + h2 + 1, 0.0F, (float) (d.height - h2 - 1) / 2, w2, h2 - 1, d.width, d.height - 1);
                                }
                            }
                        }
                    } else if (p.size() == 4) {
                        for (int i = 0; i < 4; i++) {
                            TwitterPhotoMedia media = p.get(i);
                            InputStream data = media.getData();
                            if (data != null && media.canRendering()) {
                                Dimension d = TwitterUtil.wrapImageSizeToMax(new Dimension(media.getWidth(), media.getHeight()), new Dimension(w2, h2));
                                TwitterForMC.getTextureManager().bindTexture(data);
                                if (i % 2 == 0) {
                                    DrawableHelper.blit(rowLeft + 24, rowTop + ((i / 2) * (h2 + 1)), 0.0F, (float) (d.height - h2) / 2, w2, h2, d.width, d.height);
                                } else {
                                    DrawableHelper.blit(rowLeft + 24 + w2 + 1, rowTop + ((i / 3) * (h2 + 1)), 0.0F, (float) (d.height - h2) / 2, w2, h2, d.width, d.height);
                                }
                            }
                        }
                    }

                    return p.size() == 0 ? 0 : this.photoRenderingHeight;
                }

                return 0;
            }

            public boolean mouseClicked(double x, double y, int button) {
                if (this.summary != null) {
                    int i = AbstractTwitterScreen.TweetList.this.getRowLeft() + 24;
                    int j = this.y + this.retweetedUserNameHeight + 11 + this.strings.size() * AbstractTwitterScreen.this.font.fontHeight;
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

                for (AbstractButtonWidget w : this.buttons) {
                    if (w.mouseClicked(x, y, button)) {
                        return true;
                    }
                }

                if (button == 0) {
                    if (this.summary != null && TweetList.this.getSelected() == this) {
                        AbstractTwitterScreen.this.minecraft.openScreen(new TwitterShowStatusScreen(AbstractTwitterScreen.this, this.summary));
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
                return this.summary != null && x > i && x < i + 16 && y > j && y < j + 16;
            }

            protected boolean displayTwitterPhotoAndShowStatusScreen(int mouseButton, int index) {
                if (this.summary != null) {
                    AbstractTwitterScreen.this.minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    if (mouseButton == 0) {
                        AbstractTwitterScreen.this.minecraft.openScreen(new TwitterPhotoAndShowStatusScreen(AbstractTwitterScreen.this, this.summary, index));
                    } else if (mouseButton == 1) {
                        //TODO save picture action;
                    }

                    return true;
                }

                return false;
            }

            protected boolean videoClicked(int mouseButton) {
                if (this.summary != null && !this.summary.isVideoNull()) {
                    if (mouseButton == 0) {
                        this.summary.getPlayer().play(AbstractTwitterScreen.this.minecraft.getWindow().getX(), AbstractTwitterScreen.this.minecraft.getWindow().getY(), AbstractTwitterScreen.this.minecraft.getWindow().getWidth() / 2, AbstractTwitterScreen.this.minecraft.getWindow().getHeight() / 2);
                    } else if (mouseButton == 1) {
                        //TODO save video action;
                    }
                }

                return false;
            }

            public void setHeight(int height) {
                this.height = height;
                this.fourBtnHeightOffset = this.height - 14;
                this.buttons.clear();
                this.init();
                TweetList.this.calcAllHeight();
                TweetList.this.calcAverage();
                TweetList.this.setY(-(int) TweetList.this.getScrollAmount());
            }

            public int getHeight() {
                return this.height;
            }

            public int getY() {
                return this.y;
            }

            public void setY(int y) {
                this.y = y;
                this.updateButtonY(this.fourBtnHeightOffset + this.y);
            }

            protected void updateButtonY(int y) {
                this.buttons.forEach((abstractButtonWidget) -> {
                    abstractButtonWidget.y = y;
                });
            }

            public boolean equals(Object obj) {
                if (this.summary != null) {
                    return this.summary.equals(obj);
                }

                return super.equals(obj);
            }

            public int hashCode() {
                if (this.summary != null) {
                    return this.summary.hashCode();
                }

                return super.hashCode();
            }
        }
    }
}
