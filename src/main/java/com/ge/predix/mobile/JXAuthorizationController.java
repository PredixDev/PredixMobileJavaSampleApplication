package com.ge.predix.mobile;

import com.ge.predix.mobile.core.AuthHandler;
import com.ge.predix.mobile.core.AuthHandlerCallback;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.net.*;
import java.util.List;

public class JXAuthorizationController implements AuthHandler {

    private final Pane rootPane;
    private SpinnerView spinnerView;
    private AuthHandlerCallback authHandlerCallback;
    private Browser browser = new Browser();
    private BrowserView view = new BrowserView(browser);

    JXAuthorizationController(Pane rootPane) {
        this.rootPane = rootPane;
        browser.getContext().getAutofillService().setEnabled(false);
        browser.setLoadHandler(new LoadHandler() {
            @Override
            public boolean onLoad(LoadParams loadParams) {
                List<Cookie> cookies = browser.getCookieStorage().getAllCookies();
                CookieManager manager = (CookieManager) CookieHandler.getDefault();
                copyBrowserCookiesToJavaCookieStore(cookies, manager);
                if (authHandlerCallback != null) {
                    return authHandlerCallback.urlIsAuthenticateRedirect(loadParams.getURL());
                }

                return false;
            }

            @Override
            public boolean onCertificateError(CertificateErrorParams certificateErrorParams) {
                return false;
            }
        });

        JXPMAPIRequestHandler jxpmapiRequestHandler = new JXPMAPIRequestHandler();
        jxpmapiRequestHandler.addPMAPIProtocolHandler(browser.getContext());
    }

    private void copyBrowserCookiesToJavaCookieStore(List<Cookie> cookies, CookieManager manager) {
        for (Cookie cookie : cookies) {
            try {
                HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
                httpCookie.setPath(cookie.getPath());
                httpCookie.setDomain(cookie.getDomain());
                httpCookie.setHttpOnly(cookie.isHTTPOnly());
                httpCookie.setSecure(cookie.isSecure());
                httpCookie.setVersion(0);
                if (!cookie.isSession())
                    httpCookie.setMaxAge(cookie.getExpirationTime());
                manager.getCookieStore().add(new URI(cookie.getDomain() + cookie.getPath()), httpCookie);
            } catch (URISyntaxException ignore) {}
        }
    }

    private void showView(final String url) {
        Platform.runLater(() -> {
            browser.loadURL(url);
            if (!rootPane.getChildren().contains(view)) {
                rootPane.getChildren().add(view);
                startSpinner();
            }
        });
    }

    private void hideView() {
        if (!rootPane.getChildren().contains(view)) return;
        Platform.runLater(() -> {
            browser.loadURL("");
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
