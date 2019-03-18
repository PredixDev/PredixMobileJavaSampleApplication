package com.ge.predix.mobile;

import com.ge.predix.mobile.core.PlatformImpl;
import com.ge.predix.mobile.core.RequestProcessor;
import com.ge.predix.mobile.logging.PredixSDKLogger;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.javafx.DefaultNetworkDelegate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JXPMAPIRequestHandler {

    private Map<Long, UploadData> jxPUTDeleteBugFix = new HashMap<>();

    public void addPMAPIProtocolHandler(BrowserContext context) {
        RequestProcessor requestProcessor = new RequestProcessor(PlatformImpl.getInstance());
        ProtocolHandler handler = urlRequest -> {
            if (requestProcessor.canProcessRequest(urlRequest.getURL())) {
                String data = null;
                String url1 = urlRequest.getURL();
                UploadData uploadData = (urlRequest.getUploadData() != null) ? urlRequest.getUploadData() : jxPUTDeleteBugFix.get(urlRequest.getRequestId());
                jxPUTDeleteBugFix.remove(urlRequest.getRequestId());

                if (uploadData != null) {
                    if (uploadData.getType() == UploadDataType.BYTES) {
                        BytesData bytesData = (BytesData) uploadData;
                        data = new String(bytesData.getData());
                    }
                }

                Map<String, String> headers = new HashMap<>();
                if (urlRequest.getRequestHeaders() != null) {
                    Map<String, List<String>> inputHeaders = urlRequest.getRequestHeaders().getHeaders();
                    for (String inputHeader : inputHeaders.keySet()) {
                        headers.put(inputHeader, inputHeaders.get(inputHeader).get(0));
                    }
                }

                com.ge.predix.mobile.network.URLResponse pmResponse = requestProcessor.processRequest(url1, urlRequest.getMethod(), data, headers);

                byte[] responseData = new byte[0];
                try {
                    final int bufferSize = 1024;
                    final char[] buffer = new char[bufferSize];
                    final StringBuilder out = new StringBuilder();
                    Reader in = new InputStreamReader(pmResponse.bodyInputStream(), StandardCharsets.UTF_8);
                    while (true) {
                        int bytesRead = in.read(buffer, 0, buffer.length);
                        if (bytesRead < 0)
                            break;
                        out.append(buffer, 0, bytesRead);
                    }
                    responseData = out.toString().getBytes(StandardCharsets.UTF_8);

                } catch (IOException e) {
                    PredixSDKLogger.error(this, "could not deserialize PMAPI response body", e);
                }

                URLResponse urlResponse = new URLResponse(responseData, HttpStatus.from(pmResponse.statusCode()));
                Map<String, List<String>> responseHeaders = pmResponse.getHeaders();
                for (String key : responseHeaders.keySet()) {
                    List<String> value = responseHeaders.get(key);
                    urlResponse.getHeaders().setHeaders(key, value);
                }
                return urlResponse;
            }

            return null;
        };
        context.getProtocolService().setProtocolHandler("http", handler);
        context.getProtocolService().setProtocolHandler("https", handler);
        context.getNetworkService().setNetworkDelegate(new DefaultNetworkDelegate() {

            @Override
            public void onBeforeURLRequest(BeforeURLRequestParams params) {
                if ("put".equalsIgnoreCase(params.getMethod()) && params.getUploadData() != null) {
                    jxPUTDeleteBugFix.put(params.getRequestId(), params.getUploadData());
                }
                super.onBeforeURLRequest(params);
            }


        });
    }
}
