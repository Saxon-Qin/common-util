package com.common.ui.fx;

import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * 逻辑时的提示下拉框
 *
 * @version V1.0
 **/
public class EditorPopDialog extends Dialog {
    public EditorPopDialog() {
        this.setOnCloseRequest(value -> close());
        this.initModality(Modality.NONE);
        this.initStyle(StageStyle.UNDECORATED);
    }
}
