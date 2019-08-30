package com.ge.predix.mobile;

import com.teamdev.jxbrowser.cookie.Cookie;
import com.teamdev.jxbrowser.time.Timestamp;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class JxBrowserCookieStore implements CookieStore {

    private com.teamdev.jxbrowser.cookie.CookieStore cookieStore;

    public JxBrowserCookieStore(com.teamdev.jxbrowser.cookie.CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        cookieStore.put(uri.toString(), httpCookieToJxCookie(cookie));
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return convertCookies(cookieStore.cookies(uri.toString()));
    }

    @Override
    public List<HttpCookie> getCookies() {
        return convertCookies(cookieStore.cookies());
    }

    @Override
    public List<URI> getURIs() {
        List<Cookie> cookies = cookieStore.cookies();
        HashSet<URI> uris = new HashSet<>();
        for (Cookie cookie : cookies) {
            try {
                uris.add(new URI(cookie.domain()));
            } catch (URISyntaxException ignore) {}
        }

        return Arrays.asList(uris.toArray(new URI[0]));
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        cookieStore.delete(httpCookieToJxCookie(cookie));
        return true;
    }

    @Override
    public boolean removeAll() {
        cookieStore.deleteAll();
        return true;
    }

    private Cookie httpCookieToJxCookie(HttpCookie cookie) {
        Cookie.Builder cookieBuilder = Cookie.newBuilder()
                .name(cookie.getName())
                .value(cookie.getValue())
                .domain(cookie.getDomain())
                .path(cookie.getPath());

        if (cookie.getMaxAge() == -1L) {
            return cookieBuilder.expirationTime(Timestamp.fromSeconds(cookie.getMaxAge())).build();
        }

        return cookieBuilder.build();
    }

    private List<HttpCookie> convertCookies(List<Cookie> cookies) {
        if (cookies == null) return null;

        List<HttpCookie> httpCookies = new ArrayList<>(cookies.size());

        for (Cookie cookie : cookies) {
            if (cookie.path().equals("/")) {
                httpCookies.add(jxCookieToHttpCookie(cookie));
            }
        }

        return httpCookies;
    }

    private HttpCookie jxCookieToHttpCookie(Cookie cookie) {
        HttpCookie httpCookie = new HttpCookie(cookie.name(), cookie.value());
        httpCookie.setPath(cookie.path());
        httpCookie.setDomain(cookie.domain());
        httpCookie.setHttpOnly(cookie.isHttpOnly());
        httpCookie.setSecure(cookie.isSecure());
        httpCookie.setVersion(0);

        return httpCookie;
    }
}
