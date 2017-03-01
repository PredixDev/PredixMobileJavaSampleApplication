package com.ge.predix.mobile.debugging;

import com.sun.javafx.scene.web.Debugger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletContext;
import java.io.IOException;

public class DevToolsDebugger {
    private static ServletContextHandler contextHandler;
    private static Debugger debugger;
    private static Server server;

    public static boolean enableChromeRemoteDebugger(WebEngine engine, int port) {
        try {
            DevToolsDebugger.startDebugServer(engine.impl_getDebugger(), port);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean enableWebConsoleToJavaConsoleCapture(WebEngine engine) {
        try {
            engine.documentProperty().addListener((observable, oldValue, newValue) -> {
                JSObject window = (JSObject) engine.executeScript("window");
                ConsoleLogBridge bridge = new ConsoleLogBridge();
                window.setMember("logBridge", bridge);
                engine.executeScript("console.log = function(message) { logBridge.log('[WebView Console Log] ' + message); };");
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
//        //Allows logs messages to be redirected to the console output window.
//        browser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("called state change " + oldValue + " other " + newValue);
//            if (newValue == Worker.State.SUCCEEDED) {
//
//            }
//        });
    }

    public static boolean enableFireBugInWindowDebugger(WebEngine engine) {
        try {
            engine.documentProperty().addListener((observable, oldValue, newValue) -> {
                engine.executeScript("javascript:(function() {" +
                        "if (document.getElementById('FirebugLite')) return;" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.setAttribute('src', 'http://getfirebug.com/releases/lite/1.4/firebug-lite-debug.js');" +
                        "script.setAttribute('id', 'FirebugLite');" +
                        "parent.appendChild(script);" +
                        "})()");
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void startDebugServer(Debugger debugger, int debuggerPort) throws Exception {

        server = new Server(debuggerPort);

        debugger.setEnabled(true);
        debugger.sendMessage("{\"id\" : -2, \"method\" : \"Console.enable\"}");
        debugger.sendMessage("{\"id\" : -1, \"method\" : \"Network.enable\"}");

        contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        ServletHolder devToolsHolder = new ServletHolder(new DevToolsWebSocketServlet());
        contextHandler.addServlet(devToolsHolder, "/");

        server.setHandler(contextHandler);
        server.start();

        DevToolsDebugger.debugger = debugger;
        debugger.setMessageCallback(data -> {
            DevToolsWebSocket mainSocket = (DevToolsWebSocket) contextHandler.getServletContext()
                    .getAttribute(DevToolsWebSocket.WEB_SOCKET_ATTR_NAME);
            if (mainSocket != null) {
                try {
                    mainSocket.sendMessage(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

        String remoteUrl = "chrome-devtools://devtools/bundled/inspector.html?ws=localhost:" + debuggerPort + "/";
        System.out.println("\u001B[33m" + "To debug open chrome and load next url: " + remoteUrl + "\u001B[0m");
    }

    private static void stopDebugServer() throws Exception {
        if (server != null) {
            server.stop();
            server.join();
        }
    }

    public static void sendMessageToBrowser(final String data) {
        // Display.asyncExec won't be successful here
        Platform.runLater(() -> debugger.sendMessage(data));
    }

    public static String getServerState() {
        return server == null ? null : server.getState();
    }

    public static ServletContext getServletContext() {
        return (contextHandler != null) ? contextHandler.getServletContext() : null;
    }
}