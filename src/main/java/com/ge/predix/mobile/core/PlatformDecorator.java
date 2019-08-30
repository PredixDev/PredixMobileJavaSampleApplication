package com.ge.predix.mobile.core;

import com.ge.predix.mobile.platform.CookieStorage;

import java.net.CookieStore;

public class PlatformDecorator {

    public static void updateCookieStore(CookieStorage storage) {
        PlatformImpl.getInstance().setCookieStorage(storage);
    }
}
