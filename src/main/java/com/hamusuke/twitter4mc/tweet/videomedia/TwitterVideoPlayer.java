package com.hamusuke.twitter4mc.tweet.videomedia;

import java.io.InputStream;

import com.hamusuke.twitter4mc.TwitterForMC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TwitterVideoPlayer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String url;
	@Nullable
	private Stage stage;

	public TwitterVideoPlayer(String videoURL) {
		this.url = videoURL;
	}

	public void play(int x, int y, int width, int height) {
		Platform.runLater(() -> {
			if (this.stage != null) {
				if (!this.stage.isShowing()) {
					this.stage.show();
					this.stage.requestFocus();
					return;
				}
				this.stage.requestFocus();
				return;
			}

			try {
				this.start(new Stage(), x, y, width, height);
			} catch (Exception e) {
				LOGGER.error("Error occurred starting video player", e);
				this.stage = null;
			}
		});
	}

	private void start(Stage primaryStage, int x, int y, int width, int height) throws Exception {
		this.stage = primaryStage;
		this.stage.show();
		InputStream icon = TwitterVideoPlayer.class.getResourceAsStream("/assets/" + TwitterForMC.MOD_ID + "/textures/twitter/icon/twitter.png");
		if (icon != null) {
			this.stage.getIcons().add(new Image(icon));
		} else {
			LOGGER.error("Couldn't load icon");
		}
		BorderPane root = new BorderPane();
		Media Video = new Media(this.url);
		MediaPlayer Play = new MediaPlayer(Video);
		this.stage.setOnCloseRequest((e) -> {
			this.stage.hide();
			Play.stop();
		});
		MediaView mediaView = new MediaView(Play);
		mediaView.setFitWidth(width);
		root.setCenter(mediaView);
		HBox bottomNode = new HBox(10.0);
		bottomNode.getChildren().add(this.createButton(Play));
		bottomNode.getChildren().add(this.createTimeSlider(Play));
		bottomNode.getChildren().add(this.createVolumeSlider(Play));
		root.setBottom(bottomNode);
		Scene scene = new Scene(root, width, height);
		this.stage.setX(x);
		this.stage.setY(y);
		this.stage.setTitle("TwitterVideoPlayer: " + this.url);
		this.stage.setScene(scene);
		this.stage.widthProperty().addListener((obs, ov, nv) -> {
			mediaView.setFitWidth(nv.doubleValue());
		});
		this.stage.heightProperty().addListener((obs, ov, nv) -> {
			mediaView.setFitHeight(nv.doubleValue() - 65.0D);
		});
		this.stage.show();
		this.stage.centerOnScreen();
		Play.play();
	}

	private Node createButton(MediaPlayer mp) {
		HBox root = new HBox(1.0);
		Button playButton = new Button("Play");
		Button pauseButton = new Button("Pause");
		Button stopButton = new Button("Stop");
		ToggleButton repeatButton = new ToggleButton("Repeat");
		root.getChildren().add(playButton);
		root.getChildren().add(pauseButton);
		root.getChildren().add(stopButton);
		root.getChildren().add(repeatButton);
		playButton.addEventHandler(ActionEvent.ACTION, (e) -> {
			mp.play();
		});
		pauseButton.addEventHandler(ActionEvent.ACTION, (e) -> {
			mp.pause();
		});
		stopButton.addEventHandler(ActionEvent.ACTION, (e) -> {
			mp.stop();
		});
		mp.setOnEndOfMedia(() -> {
			if (repeatButton.isSelected()) {
				mp.seek(mp.getStartTime());
				mp.play();
			} else {
				mp.seek(mp.getStartTime());
				mp.stop();
			}
		});
		return root;
	}

	private Node createTimeSlider(MediaPlayer mp) {
		HBox root = new HBox(5.0);
		Slider slider = new Slider();
		Label info = new Label();
		root.getChildren().add(slider);
		root.getChildren().add(info);
		Runnable beforeFunc = mp.getOnReady();
		mp.setOnReady(() -> {
			if (beforeFunc != null) {
				beforeFunc.run();
			}

			slider.setMin(mp.getStartTime().toSeconds());
			slider.setMax(mp.getStopTime().toSeconds());
			slider.setSnapToTicks(true);
		});
		mp.currentTimeProperty().addListener((ov, old, current) -> {
			String infoStr = String.format("%4.2f", mp.getCurrentTime().toSeconds()) + "/" + String.format("%4.2f", mp.getTotalDuration().toSeconds());
			info.setText(infoStr);
			slider.setValue(mp.getCurrentTime().toSeconds());
		});
		slider.addEventFilter(MouseEvent.MOUSE_RELEASED, (e) -> {
			mp.seek(Duration.seconds(slider.getValue()));
		});
		return root;
	}

	private Node createVolumeSlider(MediaPlayer mp) {
		HBox root = new HBox(5.0);
		Label info = new Label();
		Slider slider = new Slider();
		root.getChildren().add(info);
		root.getChildren().add(slider);
		Runnable beforeFunc = mp.getOnReady();
		mp.setOnReady(() -> {
			if (beforeFunc != null) {
				beforeFunc.run();
			}
			slider.setMin(0.0);
			slider.setMax(1.0);
			slider.setValue(mp.getVolume());
		});
		slider.valueProperty().addListener((ov, old, current) -> {
			String infoStr = String.format("Vol: %4.2f", mp.getVolume());
			info.setText(infoStr);
			mp.setVolume(slider.getValue());
		});
		return root;
	}
}
