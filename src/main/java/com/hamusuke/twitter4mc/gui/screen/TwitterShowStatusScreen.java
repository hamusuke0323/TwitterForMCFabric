package com.hamusuke.twitter4mc.gui.screen;

import com.hamusuke.twitter4mc.tweet.TweetSummary;
import com.hamusuke.twitter4mc.tweet.TwitterPhotoMedia;
import com.hamusuke.twitter4mc.utils.TwitterUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TwitterShowStatusScreen extends AbstractTwitterScreen {
	private final TweetSummary summary;

	public TwitterShowStatusScreen(Screen parent, TweetSummary summary) {
		super(new TranslatableText("tweeting"), parent);
		this.summary = summary;
	}

	protected void init() {
		if (!this.summary.isGettingReplies()) {
			if (!this.summary.isAlreadyGotReplies()) {
				this.summary.startGettingReplies(() -> {
					double scroll = 0.0D;
					if (this.list != null) {
						scroll = this.list.getScrollAmount();
						this.children.remove(this.list);
					}

					this.list = new TwitterShowStatusScreen.TweetList(this.minecraft, this.summary);
					this.list.setScrollAmount(scroll);
					this.children.add(this.list);
				});
			} else {
				double scroll = this.list != null ? this.list.getScrollAmount() : 0.0D;
				this.list = new TwitterShowStatusScreen.TweetList(this.minecraft, this.summary);
				this.list.setScrollAmount(scroll);
				this.children.add(this.list);
			}
		}

		super.init();
	}

	public void render(int mouseX, int mouseY, float delta) {
		if (this.parent != null) {
			this.parent.render(-1, -1, delta);
		}
		if (this.minecraft.currentScreen == this) {
			this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		} else {
			return;
		}

		if (this.list != null) {
			this.list.render(mouseX, mouseY, delta);
		}
		super.render(mouseX, mouseY, delta);
		this.font.drawWithShadow(this.title.asFormattedString(), (float) (this.width / 2 - this.width / 4 + 2) + 20, (float) (20 - this.font.fontHeight) / 2, 16777215);
		this.renderMessage();
	}

	@Environment(EnvType.CLIENT)
	private class TweetList extends AbstractTwitterScreen.TweetList {
		private TweetList(MinecraftClient mcIn, @NotNull TweetSummary tweetSummary) {
			super(mcIn, TwitterShowStatusScreen.this.width, TwitterShowStatusScreen.this.height, 20, TwitterShowStatusScreen.this.height - 20);
			this.addEntry(new MainEntry(tweetSummary));
			List<TweetSummary> replies = tweetSummary.getReplyTweetSummaries();
			for (TweetSummary reply : replies) {
				this.addEntry(new TweetEntry(reply));
			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}
		}

		@Environment(EnvType.CLIENT)
		private class MainEntry extends TweetEntry {
			private MainEntry(@Nullable TweetSummary tweet) {
				super(tweet);

				if (this.summary != null && this.summary.getPhotoMediaLength() == 1) {
					TwitterPhotoMedia twitterPhotoMedia = this.summary.getPhotoMedias().get(0);
					Dimension dimension = TwitterUtil.wrapImageSizeToMax(new Dimension(twitterPhotoMedia.getWidth(), twitterPhotoMedia.getHeight()), new Dimension(this.photoRenderingWidth, this.photoRenderingHeight));
					this.photoRenderingHeight = dimension.height;
					this.height = ((this.strings.size() - 1) * TwitterShowStatusScreen.this.font.fontHeight) + 10 + 30;
					this.height += this.photoRenderingHeight + 3;
					this.height += this.retweetedUserNameHeight;
					this.height += this.quoteSourceSummary != null ? 20 + this.quotedTweetStrings.size() * TwitterShowStatusScreen.this.font.fontHeight : 0;
					this.fourBtnHeightOffset = this.height - 14;
				}
			}

			public boolean mouseClicked(double x, double y, int button) {
				if (this.summary != null) {
					int i = TweetList.this.getRowLeft() + 24;
					int j = this.y + this.retweetedUserNameHeight + 11 + this.strings.size() * TwitterShowStatusScreen.this.font.fontHeight;
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

					if (this.summary.isIncludeVideo() && b) {
						return this.videoClicked(button);
					}
				}

				for (AbstractButtonWidget w : this.buttons) {
					if (w.mouseClicked(x, y, button)) {
						return true;
					}
				}

				if (button == 0) {
					TweetList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
