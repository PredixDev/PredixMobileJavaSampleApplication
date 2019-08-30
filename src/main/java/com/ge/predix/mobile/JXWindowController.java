package com.ge.predix.mobile;

import com.ge.predix.mobile.logging.PredixSDKLogger;
import com.ge.predix.mobile.platform.CustomSchemeHandler;
import com.ge.predix.mobile.platform.WaitStateModel;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Map;

public class JXWindowController implements ApplicationWindowView {
    private Browser browser;
    private BrowserView view = null;
    private WaitStateModel waitStateModel;
    private SpinnerView spinnerView;
    private Pane rootPane;
    private Engine engine;

    public JXWindowController(Engine engine) {
        super();
        this.engine = engine;
    }

    public void loadURL(final String url, Map map) {
        new Thread(() -> {
            browser = engine.newBrowser();
            PredixSDKLogger.debug("browser debug url = " + browser.devTools().remoteDebuggingUrl());
            view = BrowserView.newInstance(browser);

            Platform.runLater(() -> {
                browser.navigation().loadUrl(url);
                rootPane.getChildren().addAll(view);
                startSpinner("Syncing, please wait...");
            });
        }).start();
    }

    public void receiveAppNotification(String script) {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(script));
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
