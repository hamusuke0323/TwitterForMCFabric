package com.hamusuke.twitter4mc.gui.screen;

import com.google.common.collect.Lists;
import com.hamusuke.twitter4mc.TwitterForMC;
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

    @Nullable
    protected AbstractTwitterScreen.TweetList list;
    @Nullable
    protected String message;
    protected int fade;
    protected boolean isFade;

    protected AbstractTwitterScreen(Text title, @Nullable Screen parent) {
        super(title, parent);
    }

    public void tick() {
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

    public void renderMessage() {
        if (this.message != null) {
            List<String> list = this.font.wrapStringToWidthAsList(this.message, this.width / 2);
            RenderSystem.pushMatrix();
            RenderSystem.enableAlphaTest();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.isFade ? (float) this.fade / 20 : 1.0F);
            this.renderTooltip(list, (this.width - this.font.getStringWidth(list.get(0))) / 2, this.height - list.size() * this.minecraft.textRenderer.fontHeight);
            RenderSystem.disableAlphaTest();
            RenderSystem.popMatrix();
        }
    }

    public void accept(String errorMsg) {
        this.message = errorMsg;
        this.fade = 100;
    }

    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (this.list != null && this.list.hoveringEntry != null && this.list.hoveringEntry.mayClickIcon(p_mouseClicked_1_, p_mouseClicked_3_)) {
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
        String follow = f1 + " " + new TranslatableText("tw.follow").formatted(Formatting.GRAY).asFormattedString();
        String f2 = new LiteralText(user.getFollowersCount() + "").formatted(Formatting.BOLD).asFormattedString();
        String follower = f2 + " " + new TranslatableText("tw.follower").formatted(Formatting.GRAY).asFormattedString();
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
        k += 4 + (desc.size() * (this.minecraft.textRenderer.fontHeight + 1)) + 4;
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
        String three = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
        List<String> nameFormatted = this.font.wrapStringToWidthAsList(name, i - this.font.getStringWidth(three) - m);
        int n = this.font.draw(nameFormatted.size() == 1 ? nameFormatted.get(0) : nameFormatted.get(0) + three, (float) x, (float) i2 + 2, -1, true, matrix4f, vertexConsumerProvider$immediate, false, 0, 15728880);
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
                this.minecraft.textRenderer.drawWithShadow(names.get(i), x, y + i * this.minecraft.textRenderer.fontHeight, 11184810);
            }
            return y + names.size() * this.minecraft.textRenderer.fontHeight;
        }

        return y;
    }

    public List<String> wrapUserNameToWidth(TweetSummary summary, int width) {
        return this.minecraft.textRenderer.wrapStringToWidthAsList(I18n.translate("tw.retweeted.user", summary.getUser().getName()), width);
    }

    public void renderUserName(TweetSummary summary, int x, int y, int width) {
        boolean p = summary.getUser().isProtected();
        boolean v = summary.getUser().isVerified();

        String threeBold = new LiteralText("...").formatted(Formatting.BOLD).asFormattedString();
        int threeBoldWidth = this.minecraft.textRenderer.getStringWidth(threeBold);
        String three = new LiteralText("...").formatted(Formatting.GRAY).asFormattedString();
        int threeWidth = this.minecraft.textRenderer.getStringWidth(three);
        String time = new LiteralText("・" + summary.getDifferenceTime()).formatted(Formatting.GRAY).asFormattedString();
        int timeWidth = this.minecraft.textRenderer.getStringWidth(time);
        String screenName = new LiteralText(summary.getScreenName()).formatted(Formatting.GRAY).asFormattedString();
        String name = new LiteralText(summary.getUser().getName()).formatted(Formatting.BOLD).asFormattedString();

        int pvw = (p ? 10 : 0) + (v ? 10 : 0);
        List<String> nameFormatted = this.minecraft.textRenderer.wrapStringToWidthAsList(name, width - pvw - timeWidth);
        boolean isOver = nameFormatted.size() > 1;
        List<String> nameFormatted2 = isOver ? this.minecraft.textRenderer.wrapStringToWidthAsList(name, width - pvw - timeWidth - threeBoldWidth) : nameFormatted;

        String formattedName = nameFormatted2.size() == 1 ? nameFormatted2.get(0) : nameFormatted2.get(0) + threeBold;
        int formattedNameWidth = this.minecraft.textRenderer.getStringWidth(formattedName);
        this.minecraft.textRenderer.drawWithShadow(formattedName, x, y, 16777215);
        x += formattedNameWidth;
        if (p) {
            x += this.renderProtected(x, y);
        }
        if (v) {
            x += this.renderVerified(x, y);
        }

        List<String> screenNameFormatted = this.minecraft.textRenderer.wrapStringToWidthAsList(screenName, width - formattedNameWidth - pvw - timeWidth - threeWidth);
        if (!isOver) {
            String s = screenNameFormatted.size() == 1 ? screenNameFormatted.get(0) : screenNameFormatted.get(0) + three;
            this.minecraft.textRenderer.drawWithShadow(s, x, y, 11184810);
            x += this.minecraft.textRenderer.getStringWidth(s);
        }
        this.minecraft.textRenderer.drawWithShadow(time, x, y, 11184810);
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

        public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
            super.render(p_render_1_, p_render_2_, p_render_3_);

            AbstractTwitterScreen.TweetList.TweetEntry e = this.getEntryAtPosition(p_render_1_, p_render_2_);
            if (this.hoveringEntry != null) {
                this.isHovering = AbstractTwitterScreen.this.renderTwitterUser(this.hoveringEntry.summary, this.getRowLeft() - 60, this.hoveringEntry.getY() + this.hoveringEntry.retweetedUserNameHeight + 2 + 22, p_render_1_, p_render_2_);
                if (!this.isHovering && this.fade < 0) {
                    this.hoveringEntry = null;
                    this.fade = 0;
                }
            } else if (e != null && e.mayClickIcon(p_render_1_, p_render_2_)) {
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

            protected TwitterButton rep;
            protected TwitterButton ret;
            protected TwitterButton fav;
            protected TwitterButton sha;
            protected final int fourBtnHeightOffset;

            protected TweetEntry(@Nullable TweetSummary tweet) {
                if (tweet != null) {
                    boolean flag = tweet.getRetweetedSummary() != null;
                    this.summary = flag ? tweet.getRetweetedSummary() : tweet;
                    this.retweetedSummary = flag ? tweet : null;
                    this.quoteSourceSummary = this.summary.getQuotedTweetSummary();
                    this.strings = AbstractTwitterScreen.this.font.wrapStringToWidthAsList(this.summary.getText(), AbstractTwitterScreen.TweetList.this.getRowWidth() - 25);
                    this.quotedTweetStrings = this.quoteSourceSummary != null ? AbstractTwitterScreen.this.font.wrapStringToWidthAsList(this.quoteSourceSummary.getText(), AbstractTwitterScreen.TweetList.this.getRowWidth() - 40) : Lists.newArrayList();
                    this.height = ((this.strings.size() - 1) * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight) + 10 + 30;
                    this.height += this.summary.isIncludeImages() || this.summary.isIncludeVideo() ? 120 : 0;
                    this.retweetedUserNameHeight = flag ? AbstractTwitterScreen.this.wrapUserNameToWidth(this.retweetedSummary, AbstractTwitterScreen.TweetList.this.getRowWidth() - 24).size() * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight : 0;
                    this.height += this.retweetedUserNameHeight;
                    this.height += this.quoteSourceSummary != null ? 20 + this.quotedTweetStrings.size() * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight : 0;
                    this.fourBtnHeightOffset = this.height - 14;
                } else {
                    this.summary = null;
                    this.retweetedSummary = null;
                    this.quoteSourceSummary = null;
                    this.strings = Lists.newArrayList();
                    this.quotedTweetStrings = Lists.newArrayList();
                    this.height = 0;
                    this.retweetedUserNameHeight = 0;
                    this.fourBtnHeightOffset = 0;
                }
            }

            public void tick() {
                this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
            }

            public void init() {
                int i = AbstractTwitterScreen.TweetList.this.getRowLeft() + 24;

                this.rep = this.addButton(new TwitterButton(i, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, REPLY, 16, 32, 16, 16, (p) -> {

                }));

                this.ret = this.addButton(new TwitterButton(i + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, RETWEET, 16, 32, 16, 16, (p) -> {

                }));

                this.fav = this.addButton(new TwitterButton(i + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, this.summary.isFavorited() ? 0 : 16, this.summary.isFavorited() ? FAVORITED : FAVORITE, 16, this.summary.isFavorited() ? 16 : 32, 16, 16, (b) -> {
                    try {
                        if (this.summary.isFavorited()) {
                            TwitterForMC.mctwitter.destroyFavorite(this.summary.getId());
                            this.summary.favorite(false);
                            this.fav.setImage(FAVORITE);
                            this.fav.setWhenHovered(16);
                            this.fav.setSize(16, 32);
                        } else {
                            TwitterForMC.mctwitter.createFavorite(this.summary.getId());
                            this.summary.favorite(true);
                            this.fav.setImage(FAVORITED);
                            this.fav.setWhenHovered(0);
                            this.fav.setSize(16, 16);
                        }
                    } catch (TwitterException e) {
                        AbstractTwitterScreen.this.accept(I18n.translate("tw.failed.like", e.getErrorMessage()));
                    }
                }));

                this.sha = this.addButton(new TwitterButton(i + 60 + 60 + 60, this.fourBtnHeightOffset, 10, 10, 0, 0, 16, SHARE, 16, 32, 16, 16, (p) -> {

                }));
            }

            public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int height2, int mouseX, int mouseY, boolean isMouseOverAndObjectEquals, float p_render_9_) {
                InputStream icon = this.summary.getUserIconData();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                int nowY = rowTop;
                nowY = AbstractTwitterScreen.this.renderRetweetedUser(this.retweetedSummary, rowLeft + 6, rowLeft + 24, nowY, rowWidth - 24);

                if (icon != null) {
                    TwitterForMC.getTextureManager().bindTexture(icon);
                    DrawableHelper.blit(rowLeft, nowY, 0.0F, 0.0F, 16, 16, 16, 16);
                }

                RenderSystem.disableBlend();

                AbstractTwitterScreen.this.renderUserName(this.summary, rowLeft + 24, nowY, rowWidth - 24);

                for (int i = 0; i < this.strings.size(); i++) {
                    AbstractTwitterScreen.this.font.drawWithShadow(this.strings.get(i), (float) (rowLeft + 24), (float) (nowY + 10 + i * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight), 16777215);
                }
                nowY += 10 + this.strings.size() * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight;

                if (this.summary.isIncludeVideo()) {
                    AbstractTwitterScreen.this.fillGradient(rowLeft + 24, nowY, rowLeft + 24 + 208, nowY + 117, -1072689136, -804253680);
                    if (mouseX >= rowLeft + 24 && mouseX <= rowLeft + 24 + 208 && mouseY >= nowY && mouseY <= nowY + 117) {
                        AbstractTwitterScreen.this.renderTooltip(I18n.translate("tw.play.video"), mouseX, mouseY);
                    }
                    nowY += 117;
                }

                nowY += this.renderPhotos(rowLeft, nowY);

                if (this.quoteSourceSummary != null) {
                    nowY += 10;
                    InputStream qsIco = this.quoteSourceSummary.getUserIconData();
                    if (qsIco != null) {
                        TwitterForMC.getTextureManager().bindTexture(qsIco);
                        blit(rowLeft + 24 + 5, nowY, 0.0F, 0.0F, 10, 10, 10, 10);
                    }
                    AbstractTwitterScreen.this.renderUserName(this.quoteSourceSummary, rowLeft + 24 + 5 + 10 + 4, nowY, AbstractTwitterScreen.TweetList.this.getRowWidth() - 24 - 5 - 10 - 4 - 10);
                    for (int i = 0; i < this.quotedTweetStrings.size(); i++) {
                        AbstractTwitterScreen.this.font.drawWithShadow(this.quotedTweetStrings.get(i), rowLeft + 24 + 5, nowY + 10 + i * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight, 16777215);
                    }
                    nowY += 10 + this.quotedTweetStrings.size() * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight;
                }

                super.render(itemIndex, rowTop, rowLeft, rowWidth, height2, mouseX, mouseY, isMouseOverAndObjectEquals, p_render_9_);

                if (this.summary.getRetweetCount() != 0) {
                    AbstractTwitterScreen.this.font.drawWithShadow("" + this.summary.getRetweetCountF(), this.ret.x + 16.0F, this.ret.y, 11184810);
                }
                if (this.summary.getFavoriteCount() != 0) {
                    AbstractTwitterScreen.this.font.drawWithShadow("" + this.summary.getFavoriteCountF(), this.fav.x + 16.0F, this.fav.y, 11184810);
                }
            }

            public int renderPhotos(int rowLeft, int rowTop) {
                List<TwitterPhotoMedia> p = this.summary.getPhotoMedias();
                if (p.size() == 1) {
                    TwitterPhotoMedia media = p.get(0);
                    InputStream data = media.getData();
                    if (data != null && media.canRendering()) {
                        Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(208, 117));
                        TwitterForMC.getTextureManager().bindTexture(data);
                        DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, 0.0F, 208, 117, d.width, d.height);
                    }
                } else if (p.size() == 2) {
                    for (int i = 0; i < 2; i++) {
                        TwitterPhotoMedia media = p.get(i);
                        InputStream data = media.getData();
                        if (data != null && media.canRendering()) {
                            Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 117));
                            TwitterForMC.getTextureManager().bindTexture(data);
                            DrawableHelper.blit(rowLeft + 24 + i * 105, rowTop, 0.0F, 0.0F, 104, 117, d.width, d.height);
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
                                d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 117));
                                DrawableHelper.blit(rowLeft + 24, rowTop, 0.0F, 0.0F, 104, 117, d.width, d.height);
                            } else {
                                d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 58));
                                DrawableHelper.blit(rowLeft + 24 + 105, rowTop + ((i - 1) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
                            }
                        }
                    }
                } else if (p.size() == 4) {
                    for (int i = 0; i < 4; i++) {
                        TwitterPhotoMedia media = p.get(i);
                        InputStream data = media.getData();
                        if (data != null && media.canRendering()) {
                            Dimension d = TwitterUtil.getScaledDimensionMaxRatio(new Dimension(media.getWidth(), media.getHeight()), new Dimension(104, 58));
                            TwitterForMC.getTextureManager().bindTexture(data);
                            if (i % 2 == 0) {
                                DrawableHelper.blit(rowLeft + 24, rowTop + ((i / 2) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
                            } else {
                                DrawableHelper.blit(rowLeft + 24 + 105, rowTop + ((i / 3) * 59), 0.0F, 0.0F, 104, 58, d.width, d.height);
                            }
                        }
                    }
                }

                return p.size() == 0 ? 0 : 117;
            }

            public boolean mouseClicked(double x, double y, int button) {
                int i = AbstractTwitterScreen.TweetList.this.getRowLeft() + 24;
                int j = this.y + this.retweetedUserNameHeight + 11 + this.strings.size() * AbstractTwitterScreen.this.minecraft.textRenderer.fontHeight;
                int k = this.summary.getPhotoMediaLength();
                boolean xMore = x >= i;
                boolean yMore = y >= j;
                boolean b = xMore && x <= i + 208 && yMore && y <= j + 117;
                boolean b1 = xMore && x <= i + 104 && yMore && y <= j + 117;
                boolean b2 = x >= i + 105 && x <= i + 208 && yMore && y <= j + 58;

                if (k == 1) {
                    if (b) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                    }
                } else if (k == 2) {
                    if (b1) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                    } else if (x >= i + 105 && x <= i + 208 && yMore && y <= j + 117) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
                    }
                } else if (k == 3) {
                    if (b1) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                    } else if (b2) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
                    } else if (xMore && x <= i + 208 && y >= j + 59 && y <= j + 117) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 2);
                    }
                } else if (k == 4) {
                    if (xMore && x <= i + 104 && yMore && y <= j + 58) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 0);
                    } else if (b2) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 1);
                    } else if (xMore && x <= i + 104 && y >= j + 59 && y <= j + 117) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 2);
                    } else if (x >= i + 105 && x <= i + 208 && y >= j + 59 && y <= j + 117) {
                        return this.displayTwitterPhotoAndShowStatusScreen(button, 3);
                    }
                }

                if (this.summary.isIncludeVideo()) {
                    if (b) {
                        return this.videoClicked(button);
                    }
                }

                for (AbstractButtonWidget w : this.buttons) {
                    if (w.mouseClicked(x, y, button)) {
                        return true;
                    }
                }

                if (button == 0) {
                    AbstractTwitterScreen.TweetList.this.setSelected(this);
                    //TODO show showStatusScreen action
                    return true;
                } else {
                    return false;
                }
            }

            protected boolean mayClickIcon(double x, double y) {
                int i = AbstractTwitterScreen.TweetList.this.getRowLeft();
                int j = this.y + this.retweetedUserNameHeight;
                return x > i && x < i + 16 && y > j && y < j + 16;
            }

            protected boolean displayTwitterPhotoAndShowStatusScreen(int mouseButton, int index) {
                AbstractTwitterScreen.this.minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (mouseButton == 0) {
                    AbstractTwitterScreen.this.minecraft.openScreen(new TwitterPhotoAndShowStatusScreen(AbstractTwitterScreen.this, this.summary, index));
                } else if (mouseButton == 1) {
                    //TODO save picture action;
                }

                return true;
            }

            protected boolean videoClicked(int mouseButton) {
                if (!this.summary.isVideoNull()) {
                    if (mouseButton == 0) {
                        this.summary.getPlayer().play(AbstractTwitterScreen.this.minecraft.getWindow().getX(), AbstractTwitterScreen.this.minecraft.getWindow().getY(), AbstractTwitterScreen.this.minecraft.getWindow().getWidth() / 2, AbstractTwitterScreen.this.minecraft.getWindow().getHeight() / 2);
                    } else if (mouseButton == 1) {
                        //TODO save video action;
                    }
                }

                return false;
            }

            public int getHeight() {
                return this.height;
            }

            public int getY() {
                return this.y;
            }

            public void setY(int y) {
                this.y = y;
                this.rep.y = this.ret.y = this.fav.y = this.sha.y = this.fourBtnHeightOffset + this.y;
            }

            public boolean equals(Object obj) {
                return this.summary.equals(obj);
            }

            public int hashCode() {
                return this.summary.hashCode();
            }
        }
    }
}
