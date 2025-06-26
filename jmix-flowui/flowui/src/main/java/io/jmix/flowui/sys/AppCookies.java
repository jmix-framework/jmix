/*
 * Copyright 2022 Haulmont.
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

import io.jmix.core.annotation.Internal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.identityHashCode;

/**
 * A utility class for handling cookies within the application. This class provides functionality
 * to add, retrieve, update, and remove cookies. It also manages the state and configuration
 * of cookies, such as their enabled state and path.
 */
@Internal
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

    /**
     * Returns the value of a cookie by its name if cookies are enabled.
     *
     * @param name the name of the cookie to retrieve
     * @return the value of the cookie, or {@code null} if the cookie does not exist
     * or cookies are not enabled
     */
    @Nullable
    public String getCookieValue(String name) {
        Cookie cookie = getCookieIfEnabled(name);
        return cookie == null ? null : cookie.getValue();
    }

    /**
     * Returns the maximum age of the specified cookie in seconds.
     * If the cookie does not exist or cookies are not enabled, {@code 0} is returned.
     *
     * @param name the name of the cookie for which the maximum age is to be retrieved
     * @return the maximum age of the specified cookie in seconds, or {@code 0} if the cookie does not exist
     * or cookies are not enabled
     */
    public int getCookieMaxAge(String name) {
        Cookie cookie = getCookieIfEnabled(name);
        return cookie == null ? 0 : cookie.getMaxAge();
    }

    /**
     * Adds a cookie with the specified name and value. The cookie is assigned a default maximum age
     * defined by {@code COOKIE_MAX_AGE}.
     *
     * @param name  the name of the cookie to add
     * @param value the value of the cookie to add;
     *              if {@code null} or empty, the cookie is removed
     */
    public void addCookie(String name, @Nullable String value) {
        addCookie(name, value, COOKIE_MAX_AGE);
    }

    /**
     * Adds a cookie with the specified name, value, and maximum age.
     * The cookie is only added if cookies are enabled. If the value is
     * {@code null} or empty, the cookie is removed instead.
     *
     * @param name   the name of the cookie to add or remove
     * @param value  the value of the cookie to add;
     *               if {@code null} or empty, the cookie is removed
     * @param maxAge the maximum age of the cookie in seconds
     */
    public void addCookie(String name, @Nullable String value, int maxAge) {
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

    /**
     * Removes a cookie with the specified name.
     *
     * @param name the name of the cookie to be removed
     */
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

    /**
     * Updates the internal cookie storage by synchronizing it with cookies from the current HTTP request.
     * If cookies are enabled, it clears the internal cookies collection and reloads all cookies
     * from the current HTTP request into its internal storage.
     */
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

    /**
     * Returns the path used by the cookies in the application.
     *
     * @return the cookie path
     */
    public String getCookiePath() {
        return cookiePath;
    }

    /**
     * Sets the path used by the cookies in the application.
     *
     * @param cookiePath the path to be set for the cookies
     */
    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    /**
     * Indicates whether cookies are enabled for the application.
     *
     * @return {@code true} if cookies are enabled, {@code false} otherwise
     */
    public boolean isCookiesEnabled() {
        return cookiesEnabled;
    }

    /**
     * Sets whether cookies are enabled for the application.
     *
     * @param cookiesEnabled {@code true} if cookies are enabled, {@code false} otherwise
     */
    public void setCookiesEnabled(boolean cookiesEnabled) {
        this.cookiesEnabled = cookiesEnabled;
    }
}