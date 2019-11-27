package com.common.ui.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 弹出提示框
 *
 * @version V1.0
 **/
public class PopupMessage {
    public static void showMessage(String message) {
        Stage stage = new Stage();
        stage.setOnCloseRequest(e -> stage.close());
        stage.setTitle("提示");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        HBox box = new HBox();
        Label label = new Label();
        label.setText(message);
        box.setSpacing(20);
        box.setPadding(new Insets(20));
        box.getChildren().add(label);
        box.setStyle("-fx-border-style: solid; " +
                "-fx-border-color: #CBEEF9; " +
                "-fx-background-color: #000; ");
        label.setStyle("-fx-text-fill: #30F74C; -fx-font-size: 14px;");
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(3);
        dropShadow.setOffsetY(3);
        dropShadow.setColor(Color.GRAY);
        box.setEffect(dropShadow);
        stage.setScene(new Scene(box));
        stage.setAlwaysOnTop(true);
        stage.show();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(stage::close);
            }
        }, 1000);
    }
}
