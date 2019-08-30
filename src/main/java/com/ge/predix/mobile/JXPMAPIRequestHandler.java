package com.ge.predix.mobile;

import com.ge.predix.mobile.core.Platform;
import com.ge.predix.mobile.core.PlatformImpl;
import com.ge.predix.mobile.core.RequestProcessor;
import com.ge.predix.mobile.logging.PredixSDKLogger;
import com.ge.predix.mobile.network.URLResponse;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.net.*;
import com.teamdev.jxbrowser.net.callback.InterceptRequestCallback;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class JXPMAPIRequestHandler {

    public void addPMAPIProtocolHandler(Engine engine) {
        engine.network().set(InterceptRequestCallback.class, params -> {
            RequestProcessor requestProcessor = new RequestProcessor(PlatformImpl.getInstance());
            String url = params.urlRequest().url();
            if (requestProcessor.canProcessRequest(url)) {
                AtomicReference<String> data = new AtomicReference<>();
                params.uploadData().ifPresent(uploadData -> {
                    if (uploadData instanceof TextData) {
                        data.set(((TextData) uploadData).data());
                    } else if (uploadData instanceof BytesData) {
                        BytesData bytesData = (BytesData) uploadData;
                        data.set(new String(bytesData.data()));
                    } else if (uploadData instanceof MultipartFormData) {
                        MultipartFormData multipartFormData = (MultipartFormData) uploadData;
                        List<MultipartFormData.Pair> files = multipartFormData.data();
                        if (files.size() > 1) {
                            throw new RuntimeException("pmapi can't process multipart with more than one file");
                        }
                        files.get(0).fileValue().map(File::pathValue).map(Optional::get).ifPresent(data::set);
                    }
                });

                Map<String, String> headers = new HashMap<>();
                if (params.httpHeaders() != null) {
                    for (HttpHeader httpHeader : params.httpHeaders()) {
                        headers.put(httpHeader.name(), httpHeader.value());
                    }
                }

                UrlRequest urlRequest = params.urlRequest();
                URLResponse response = requestProcessor.processRequest(url, urlRequest.method(), data.get(), headers);
                byte[] rawData = convertPMAPIResponseToBytes(response);

                UrlRequestJob.Options.Builder optionsBuilder = UrlRequestJob.Options.newBuilder(params.urlRequest().id(), HttpStatusWrapper.of(response.statusCode()));

                response.getHeaders().forEach((k, v) -> v.forEach(c -> optionsBuilder.addHttpHeader(HttpHeaderWrapper.of(k, c))));

                UrlRequestJob urlRequestJob = engine.network().newUrlRequestJob(optionsBuilder.build());
                urlRequestJob.write(rawData);
                urlRequestJob.complete();

                return InterceptRequestCallback.Response.intercept(urlRequestJob);
            }

            return InterceptRequestCallback.Response.proceed();
        });
    }

    private byte[] convertPMAPIResponseToBytes(URLResponse pmResponse) {
        byte[] responseData = new byte[0];
        if (pmResponse.bodyInputStream() != null) {
            try {
                final int bufferSize = 1024;
                final byte[] buffer = new byte[bufferSize];
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                BufferedInputStream in = new BufferedInputStream(pmResponse.bodyInputStream());
                while (true) {
                    int bytesRead = in.read(buffer, 0, buffer.length);
                    if (bytesRead < 0)
                        break;
                    out.write(buffer, 0, bytesRead);
                }
                responseData = out.toByteArray();

            } catch (IOException e) {
                PredixSDKLogger.error(this, "could not deserialize PMAPI response body", e);
            }
        }
        return responseData;
    }

}
