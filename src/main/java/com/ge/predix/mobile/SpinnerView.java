package com.ge.predix.mobile;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * SpinnerView
 * DesktopReferenceApplication
 * <p>
 * Created by jeremyosterhoudt on 12/2/16.
 * Copyright © 2016 GE. All rights reserved.
 */
public class SpinnerView extends VBox {

    private final Label progressLabel;
    private final Pane pane;

    public SpinnerView(String message, Pane pane) {
        super();
        this.pane = pane;
        super.setAlignment(Pos.CENTER);
        super.setSpacing(10);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressLabel = new Label(message);
        progressLabel.setTextAlignment(TextAlignment.CENTER);
        progressIndicator.setMaxSize(100, 100);
        super.getChildren().add(progressIndicator);
        super.getChildren().add(progressLabel);
    }

    public void show() {
        if (pane.getChildren().contains(this)) return;
        Platform.runLater(() -> {
            pane.getChildren().add(this);
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            pane.getChildren().remove(this);
        });
    }

    public void setText(String text) {
        Platform.runLater(() -> {
            progressLabel.setText(text);
        });
    }
}
