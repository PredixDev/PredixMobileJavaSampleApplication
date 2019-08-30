package com.ge.predix.mobile;

import com.ge.predix.mobile.core.AuthHandler;
import com.ge.predix.mobile.core.AuthHandlerCallback;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.callback.CertificateErrorCallback;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFailed;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFinished;
import com.teamdev.jxbrowser.navigation.event.NavigationStarted;
import com.teamdev.jxbrowser.net.NetError;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class JXAuthorizationController implements AuthHandler {

    private final Pane rootPane;
    private SpinnerView spinnerView;
    private Browser browser;
    private BrowserView view;
    private AuthHandlerCallback authHandlerCallback;

    JXAuthorizationController(Pane rootPane, Engine engine) {
        this.rootPane = rootPane;
        this.browser = engine.newBrowser();
        this.view = BrowserView.newInstance(browser);

        browser.set(CertificateErrorCallback.class,(params, tell) -> tell.allow());
        browser.navigation().on(NavigationStarted.class, event -> startSpinner());
        browser.navigation().on(FrameLoadFinished.class, event -> stopSpinner());

        browser.navigation().on(FrameLoadFailed.class, event -> {
            NetError error = event.error();
            authHandlerCallback.authenticationEncounteredError(error.toString());
            stopSpinner();
        });
    }

    private void showView(final String url) {
        Platform.runLater(() -> {
            browser.navigation().loadUrl(url);
            if (!rootPane.getChildren().contains(view)) {
                rootPane.getChildren().add(view);
                startSpinner();
            }
        });
    }

    private void hideView() {
        if (!rootPane.getChildren().contains(view)) return;
        Platform.runLater(() -> {
            browser.navigation().loadUrl("about:blank");
            rootPane.getChildren().remove(view);
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
