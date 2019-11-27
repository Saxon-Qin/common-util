package com.common.ui.fx;

import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

/**
 * 单元素编辑对话框
 *
 * @version V1.0
 **/
public class SingleInputDialog extends Dialog {
    private Consumer<String> submitConsumer;

    private TextField valueField = new TextField();

    private SingleInputDialog(Builder builder) {
        this.setTitle(builder.title);

        String fieldTitle = builder.fieldTitle;
        this.submitConsumer = builder.submitConsumer;

        Label titleLabel = new Label(fieldTitle + "：");

        HBox hBox = new HBox();
        hBox.setSpacing(10F);
        hBox.getChildren().addAll(titleLabel, valueField);
        this.getDialogPane().setContent(hBox);

        this.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        this.getDialogPane().lookupButton(ButtonType.OK).addEventHandler(ActionEvent.ACTION, event -> {
            if ("".equals(valueField.getText().trim())) {
                PopupMessage.showMessage("输入值不能为空");
                return;
            }

            if (null != submitConsumer) {
                submitConsumer.accept(valueField.getText());
                SingleInputDialog.this.close();
            }
        });
    }

    public static Builder newSingleInputDialog() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private String fieldTitle;
        private Consumer<String> submitConsumer;

        private Builder() {
        }

        public SingleInputDialog build() {
            return new SingleInputDialog(this);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder fieldTitle(String fieldTitle) {
            this.fieldTitle = fieldTitle;
            return this;
        }

        public Builder submitConsumer(Consumer<String> submitConsumer) {
            this.submitConsumer = submitConsumer;
            return this;
        }
    }
}
