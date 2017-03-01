package com.ge.predix.mobile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * PreferencesController
 * DesktopReferenceApplication
 * <p>
 * Created by jeremyosterhoudt on 11/3/16.
 * Copyright © 2016 GE. All rights reserved.
 */
public class PreferencesController {

    private boolean showing = false;
    private Stage stage;

    PreferencesController() {
        super();
        this.stage = new Stage();

        String name = "Preferences";
        StackPane root = new StackPane();
        root.getChildren().add(layoutFields(name));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle(name);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(event -> showing = false);
    }

    public void show(Pane parent) {
        if (showing) return;
        showing = true;
        if (stage.getOwner() == null) {
            stage.initOwner(parent.getScene().getWindow());
        }
        stage.show();
    }

    private GridPane layoutFields(String name) {
        Properties properties = new Properties();
        URL resource = PreferencesController.class.getResource("/config.properties");
        try {
            properties.load(resource.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 15, 15, 15));

        Text sceneTitle = new Text(name);
        sceneTitle.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label hostLabel = new Label("Host:");
        grid.add(hostLabel, 0, 1);

        TextField hostTextField = new TextField();
        hostTextField.setText(properties.getProperty("server_hostname"));
        grid.add(hostTextField, 1, 1, 24, 1);

        Label logLevelLabel = new Label("Log Level:");
        grid.add(logLevelLabel, 0, 2);

        TextField levelTextField = new TextField();
        levelTextField.setText(properties.getProperty("logging_level"));
        grid.add(levelTextField, 1, 2, 24, 1);

        Button button = new Button("Save");
        button.setOnAction(event -> {
            savePreferences(properties, resource, hostTextField.getText(), levelTextField.getText());
            showing = false;
            stage.close();
        });
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(button);
        grid.add(button, 8, 4);
        return grid;
    }

    private void savePreferences(Properties properties, URL resource, String host, String level) {
        properties.setProperty("server_hostname", host);
        properties.setProperty("logging_level", level);
        try {
            properties.store(new FileOutputStream(new File(resource.toURI())), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
