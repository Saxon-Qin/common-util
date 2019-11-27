package com.common.ui.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

/**
 * .弹出提示
 *
 * @version V1.0
 **/
public class LAlert extends Alert {
    public static void error(String message) {
        LAlert alert = new LAlert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void error(String message, Exception ex) {
        LAlert alert = new LAlert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setContentText(message);
        TextArea label = new TextArea(ex.getMessage());
        label.setEditable(false);
        label.setMaxWidth(300);
        label.setPrefWidth(300);
        label.setWrapText(true);
        alert.getDialogPane().setExpandableContent(label);
        alert.showAndWait();
    }

    public static boolean confirm(String message) {
        LAlert alert = new LAlert(AlertType.CONFIRMATION);
        alert.setTitle("警告");
        alert.setContentText(message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private LAlert(AlertType alertType) {
        super(alertType);
    }
}
