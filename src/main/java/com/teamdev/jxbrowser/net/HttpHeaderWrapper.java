package com.teamdev.jxbrowser.net;

public class HttpHeaderWrapper {

    public static HttpHeader of(String key, String value) {
        //chromium incorrectly understand jar scheme in Access-Control-Allow-Origin header
        if ("Access-Control-Allow-Origin".equals(key) && value.startsWith("jar://")) {
            value = "*";
        }

        return HttpHeader.of(key, value);
    }
}
