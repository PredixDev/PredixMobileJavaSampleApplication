package com.ge.predix.mobile;

import com.ge.predix.mobile.core.AuthHandler;
import com.ge.predix.mobile.core.AuthHandlerCallback;
import com.ge.predix.mobile.logging.PredixSDKLogger;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

/**
 * AuthorizationController
 * DesktopReferenceApplication
 * <p>
 * Created by jeremyosterhoudt on 10/19/16.
 * Copyright © 2016 GE. All rights reserved.
 */
class AuthorizationController implements AuthHandler {

    private final Pane rootPane;
    private WebView browser = new WebView();
    private SpinnerView spinnerView;
    private AuthHandlerCallback authHandlerCallback;

    AuthorizationController(Pane rootPane) {
        this.rootPane = rootPane;
//        DevToolsDebugger.enableChromeRemoteDebugger(browser.getEngine(), 51741);
//        DevToolsDebugger.enableWebConsoleToJavaConsoleCapture(browser.getEngine());
//        DevToolsDebugger.enableFireBugInWindowDebugger(browser.getEngine());
        browser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED || newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                stopSpinner();
                if (newValue == Worker.State.FAILED) {
                    PredixSDKLogger.error("\u001B[31m" + browser.getEngine().getLoadWorker().getException().toString() + "\u001B[0m");
                    authHandlerCallback.authenticationEncounteredError(browser.getEngine().getLoadWorker().getException().toString());
                }
            } else if (newValue == Worker.State.SCHEDULED) {
                startSpinner();
            }
        });
    }

    private void showView(final String url) {
        Platform.runLater(() -> {
            browser.getEngine().load(url);
            if (!rootPane.getChildren().contains(browser)) {
                rootPane.getChildren().add(browser);
                startSpinner();
            }
        });
    }

    private void hideView() {
        if (!rootPane.getChildren().contains(browser)) return;
        Platform.runLater(() -> {
            browser.getEngine().loadContent("");
            rootPane.getChildren().remove(browser);
            stopSpinner();
        });
    }

    private void startSpinner() {
        if (spinnerView != null) return;
        spinnerView = new SpinnerView("Authenticating, Please wait...", rootPane);
        spinnerView.show();

    }

    private void stopSpinner() {
        if (spinnerView == null) return;
        spinnerView.hide();
        spinnerView = null;
    }

    @Override
    public void showAuthenticationUI(String url, AuthHandlerCallback authHandlerCallback) {
        this.authHandlerCallback = authHandlerCallback;
        showView(url);
    }

    @Override
    public void hideAuthenticationUI() {
        hideView();
    }
}