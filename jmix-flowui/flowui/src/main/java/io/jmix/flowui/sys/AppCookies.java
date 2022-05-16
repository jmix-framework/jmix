/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.flowui.sys;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.identityHashCode;

public class AppCookies {

    public static final String COOKIE_LOCALE = "LAST_LOCALE";
    public static final String COOKIE_REMEMBER_ME = "rememberMe";
    public static final String COOKIE_LOGIN = "rememberMe.Login";
    public static final String COOKIE_PASSWORD = "rememberMe.Password";

    public static final int COOKIE_MAX_AGE = 31536000; //1 year (in seconds)

    protected transient Map<String, Cookie> requestedCookies;

    protected String cookiePath = "/";
    protected boolean cookiesEnabled = true;

    private long lastRequestHash = 0L;

    public AppCookies() {
        requestedCookies = new HashMap<>();
    }

    @Nullable
    public String getCookieValue(String name) {
        Cookie cookie = getCookieIfEnabled(name);
        return cookie == null ? null : cookie.getValue();
    }

    public int getCookieMaxAge(String name) {
        Cookie cookie = getCookieIfEnabled(name);
        return cookie == null ? 0 : cookie.getMaxAge();
    }

    public void addCookie(String name, String value) {
        addCookie(name, value, COOKIE_MAX_AGE);
    }

    public void addCookie(String name, String value, int maxAge) {
        if (isCookiesEnabled()) {
            if (StringUtils.isEmpty(value)) {
                removeCookie(name);
            } else {
                Cookie cookie = new Cookie(name, value);
                cookie.setPath(getCookiePath());
                cookie.setMaxAge(maxAge);
                addCookie(cookie);
            }
        }
    }

    public void removeCookie(String name) {
        if (isCookiesEnabled()) {
            Cookie cookie = getCookie(name);
            if (cookie != null) {
                cookie.setValue(null);
                cookie.setPath(getCookiePath());
                cookie.setMaxAge(0);
                addCookie(cookie);
            }
        }
    }

    @Nullable
    protected Cookie getCookieIfEnabled(String name) {
        return isCookiesEnabled() ? getCookie(name) : null;
    }

    @Nullable
    protected Cookie getCookie(String name) {
        ServletRequestAttributes requestContext = getRequestContext();
        if (identityHashCode(requestContext.getRequest()) != lastRequestHash) {
            updateCookies();
        }
        return requestedCookies.get(name);
    }

    protected void addCookie(Cookie cookie) {
        ServletRequestAttributes requestContext = getRequestContext();
        HttpServletResponse response = requestContext.getResponse();
        if (response != null) {
            response.addCookie(cookie);
        }
    }

    protected ServletRequestAttributes getRequestContext() {
        return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    }

    public void updateCookies() {
        if (isCookiesEnabled()) {
            requestedCookies.clear();

            ServletRequestAttributes requestContext = getRequestContext();

            Cookie[] cookies = requestContext.getRequest().getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    requestedCookies.put(cookie.getName(), cookie);
                }
            }
            lastRequestHash = identityHashCode(requestContext.getRequest());
        }
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public boolean isCookiesEnabled() {
        return cookiesEnabled;
    }

    public void setCookiesEnabled(boolean cookiesEnabled) {
        this.cookiesEnabled = cookiesEnabled;
    }
}