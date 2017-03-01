package com.ge.predix.mobile;

import com.ge.predix.mobile.debugging.DevToolsDebugger;
import com.ge.predix.mobile.logging.PredixSDKLogger;
import com.ge.predix.mobile.platform.CustomSchemeHandler;
import com.ge.predix.mobile.platform.WaitStateModel;
import com.ge.predix.mobile.platform.WindowView;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * WindowController
 * DesktopReferenceApplication
 * <p>
 * Created by jeremyosterhoudt on 10/19/16.
 * Copyright © 2016 GE. All rights reserved.
 */
public class WindowController implements WindowView {
    private WebView browser = new WebView();
    private WaitStateModel waitStateModel;
    private SpinnerView spinnerView;
    private Pane rootPane;

    public WindowController() {
        super();
        DevToolsDebugger.enableChromeRemoteDebugger(browser.getEngine(), 51742);
        DevToolsDebugger.enableWebConsoleToJavaConsoleCapture(browser.getEngine());
//        DevToolsDebugger.enableFireBugInWindowDebugger(browser.getEngine());
    }

    public void loadURL(final String url, Map map) {
        PredixSDKLogger.trace(this, "SDK is asking the webview to load url: = " + url);
        Platform.runLater(() -> {
            String loadUrl = url;
            try {
                //This needs to be here for windows...  Sending an un-encoded URL causes it not to be loaded from the correct path making resources images and files unavailable.
                //TODO:  Move this into the SDK!
                loadUrl = URLDecoder.decode(loadUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                PredixSDKLogger.warning(this, "could not remove % escapes from url string", e);
            }
            browser.getEngine().load(loadUrl);

            stopSpinner();
        });
    }

    public void receiveAppNotification(String script) {
        Platform.runLater(() -> browser.getEngine().executeScript(script));
    }

    public void showDialog(String s, String s1, String s2) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(new Group(new Text(25, 25, "message: " + s + " other 1: " + s1 + " other 2: " + s2)));
        dialog.setScene(scene);
        dialog.show();
    }

    public WaitStateModel waitState() {
        return this.waitStateModel;
    }

    public void updateWaitState(WaitStateModel waitStateModel) {
        this.waitStateModel = waitStateModel;
        switch (waitStateModel.waitState) {
            case Waiting:
                startSpinner(waitStateModel.message);
                break;
            case NotWaiting:
                stopSpinner();
                break;
            default:
                break;

        }
    }

    public CustomSchemeHandler customSchemeHandler() {
        return null;
    }

    public void show(Pane rootPane) {
        this.rootPane = rootPane;
        rootPane.getChildren().addAll(browser);
        startSpinner("Syncing, please wait...");
    }

    private void startSpinner(String message) {
        if (spinnerView != null) {
            spinnerView.setText(message);
            return;
        }
        spinnerView = new SpinnerView(message, rootPane);
        spinnerView.show();
    }

    private void stopSpinner() {
        if (spinnerView == null) return;
        spinnerView.hide();
        spinnerView = null;
    }
}
