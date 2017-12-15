package com.ge.predix.mobile;

import com.ge.predix.mobile.core.AuthHandler;
import com.ge.predix.mobile.core.MobileManager;
import com.ge.predix.mobile.core.PredixMobileConfiguration;
import com.ge.predix.mobile.core.ViewInterface;
import com.ge.predix.mobile.core.notifications.InitialReplicationCompleteNotification;
import com.ge.predix.mobile.exceptions.InitializationException;
import com.ge.predix.mobile.platform.PlatformContext;
import com.ge.predix.mobile.platform.WindowView;
import com.google.common.eventbus.Subscribe;
import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ReferenceApplication
 * DesktopReferenceApplication
 * <p>
 * Created by jeremyosterhoudt on 10/19/16.
 * Copyright © 2016 GE. All rights reserved.
 */
public class ReferenceApplication extends Application {
    private MobileManager mobileManager;
    private WindowController windowView = new WindowController();
    private PreferencesController preferencesController = new PreferencesController();
    private static final String Application_Name = "MFL Reference Application";

    public static void main(String[] args) throws InitializationException {
        launch(args);
    }

    public ReferenceApplication() throws InitializationException {
        this.mobileManager = MobileManager.instance();
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    public void start(Stage primaryStage) throws Exception {
        StackPane stackPane = new StackPane();
        BorderPane root = new BorderPane(stackPane);

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primScreenBounds.getWidth() * 0.65; //take 65% of the width of the screen
        primaryStage.setScene(new Scene(root, width, width * 0.78));
        primaryStage.setTitle(Application_Name);
        primaryStage.setOnCloseRequest(windowEvent -> endApplication());
        loadMenuBar(root);
        windowView.show(stackPane);
        primaryStage.show();
        startPredixMobile(stackPane);
    }

    private boolean isMac() {
        final String os = System.getProperty("os.name");
        return os != null && os.startsWith("Mac");
    }

    private void loadMenuBar(BorderPane pane) {
        String appName = Application_Name;
        if (this.isMac()) {
            createMacMenuBar(pane, appName);
        } else {
            createWindowsMenuBar(pane, appName);
        }
    }

    private void createMacMenuBar(Pane pane, String appName) {
        MenuToolkit toolkit = MenuToolkit.toolkit();
        if (toolkit == null) return;
        MenuBar menuBar = new MenuBar(new Menu());

        Menu appMenu = new Menu();
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(event -> preferencesController.show(pane));
        appMenu.getItems().addAll(toolkit.createAboutMenuItem(appName), new SeparatorMenuItem(), preferences, new SeparatorMenuItem(), toolkit.createHideMenuItem(appName), toolkit.createHideOthersMenuItem(), toolkit.createUnhideAllMenuItem(), new SeparatorMenuItem(), toolkit.createQuitMenuItem(appName));
        toolkit.setApplicationMenu(appMenu);
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().addAll(toolkit.createMinimizeMenuItem(), toolkit.createZoomMenuItem(), toolkit.createCycleWindowsItem(), new SeparatorMenuItem(), toolkit.createBringAllToFrontItem());
        menuBar.getMenus().add(windowMenu);
        toolkit.autoAddWindowMenuItems(windowMenu);
        toolkit.setGlobalMenuBar(menuBar);
    }

    private void createWindowsMenuBar(BorderPane pane, String appName) {
        MenuBar menuBar = new MenuBar();
        Menu appMenu = new Menu(appName);
        MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(event -> preferencesController.show(pane));
        MenuItem about = new MenuItem("About " + appName);
        about.setOnAction(event -> {
            Dialog dialog = new Dialog();
            dialog.initOwner(pane.getScene().getWindow());
            dialog.setTitle("About " + appName);
            dialog.setContentText("Version 1.0.0");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            dialog.setOnCloseRequest(event1 -> dialog.close());
            dialog.show();
        });
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> endApplication());
        appMenu.getItems().addAll(about, new SeparatorMenuItem(), preferences, new SeparatorMenuItem(), exit);
        Menu aboutMenu = new Menu("About");
        aboutMenu.getItems().addAll(about);
        menuBar.getMenus().addAll(appMenu, aboutMenu);
        menuBar.prefWidthProperty().bind(pane.getScene().widthProperty());
        pane.setTop(menuBar);
    }

    private void startPredixMobile(Pane root) {
        AuthorizationController authorizationController = new AuthorizationController(root);

        ViewInterface dependencies = new ViewInterface() {
            public PlatformContext getContext() {
                return new JavaPlatformContext();
            }

            @Override
            public Map<String, Object> getDefaultPreferences() {
                return getApplicationConfig();
            }

            public AuthHandler getAuthHandler() {
                return authorizationController;
            }

            public WindowView getWindowView() {
                return windowView;
            }
        };

        mobileManager.setViewInterface(dependencies);
        new Thread(() -> {
            loadProxyIfNeeded();
            try {
                mobileManager.start();
                mobileManager.getNotificationRegistrar().registerListener(this);
            } catch (InitializationException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Subscribe
    public void listenForInitialReplicationComplete(InitialReplicationCompleteNotification notification) {
        System.out.println("\u001B[33m" + "Received replication complete notification" + "\u001B[0m");
    }

    private void loadProxyIfNeeded() {
        setProxyValue("http.proxyHost", "http.proxyPort");
        setProxyValue("https.proxyHost", "https.proxyPort");
    }

    private void setProxyValue(String hostKey, String portKey) {
        Map<String, Object> config = getProxyConfig();
        String host = (String) config.get(hostKey);
        String port = (String) config.get(portKey);

        if (host == null) return;

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException ignore) {
        }

        if (inetAddress != null) {
            System.setProperty("java.net.useSystemProxies", "true");
            System.setProperty(hostKey, host);
            if (port != null) {
                System.setProperty(portKey, port);
            }

            System.setProperty("https.proxySet", "true");
        }
    }

    private void endApplication() {
        Platform.exit();
        if (this.isMac()) {
            System.exit(0);
        }
    }

    private Map<String, Object> getApplicationConfig() {
        return getConfig("config.properties");
    }

    private Map<String, Object> getProxyConfig() {
        return getConfig("proxy.properties");
    }

    private Map<String, Object> getConfig(String file) {
        Properties properties = new Properties();
        InputStream resource = ReferenceApplication.class.getClassLoader().getResourceAsStream(file);
        try {
            properties.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HashMap<String, Object>((Map) properties);
    }
}
