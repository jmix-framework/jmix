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
package io.jmix.ui.sys;

import io.jmix.ui.AppUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;

public final class ControllerUtils {
    private static final String DISPATCHER = "dispatch";

    private static final Logger log = LoggerFactory.getLogger(ControllerUtils.class);

    private ControllerUtils() {
    }

    /**
     * The URL string that is returned will have '/' in the end
     */
    public static String getLocationWithoutParams() {
        URI location = AppUI.getCurrent().getPage().getLocation();
        return getLocationWithoutParams(location);
    }

    /**
     * The URL string that is returned will have '/' in the end
     */
    public static String getLocationWithoutParams(URI location) {
        try {
            StringBuilder baseUrl = new StringBuilder(location.toURL().toExternalForm());
            if (location.getQuery() != null) {
                baseUrl.delete(baseUrl.indexOf("?" + location.getQuery()), baseUrl.length());
            } else if (location.getFragment() != null) {
                baseUrl.delete(baseUrl.indexOf("#" + location.getRawFragment()), baseUrl.length());
            }
            String baseUrlString = baseUrl.toString();
            return baseUrlString.endsWith("/") ? baseUrlString : baseUrlString + "/";
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to get location without params", e);
        }
    }

    public static String getWebControllerURL(@Nullable String mapping) {
        if (mapping == null) throw new IllegalArgumentException("Mapping cannot be null");
        String baseUrl = getLocationWithoutParams();

        StringBuilder url = new StringBuilder(baseUrl).append(getDispatcher());
        if (!mapping.startsWith("/")) {
            url.append("/");
        }
        url.append(mapping);
        return url.toString();
    }

    public static String getDispatcher() {
        return DISPATCHER;
    }

    public static String getControllerPrefix() {
        return "/" + DISPATCHER;
    }

    public static String getControllerPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.startsWith(getControllerPrefix())) {
            path = path.substring(getControllerPrefix().length());
        }
        return path;
    }
}