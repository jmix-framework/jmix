/*
 * Copyright 2024 Haulmont.
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

package io.jmix.core.impl.session;

import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides thread-local storage for session attributes. This utility class facilitates the transfer of HTTP session
 * data to newly spawned threads that do not inherently have access to the data of the original HTTP session.
 * <p>
 * Use this class when you need to perform operations in a new thread that require access to the session data from an
 * HTTP request. This is particularly useful in asynchronous processing or when handling requests outside the main
 * request processing thread.</p>
 * <p>
 * Usage example:
 * <pre>
 * Map&lt;String, Object&gt; sessionAttributes = ThreadLocalSessionData.extractHttpSessionAttributes();
 * ThreadLocalSessionData.setAttributes(sessionAttributes);
 * // Now, session attributes can be accessed from the new thread using ThreadLocalSessionData.getAttribute(...)
 * </pre>
 * <p>
 * Ensure that you clear the thread-local storage after use. Use {@link #clear()} method once the thread-local data is
 * no longer needed.
 */
public class ThreadLocalSessionData {

    private static final ThreadLocal<Map<String, Object>> sessionAttributesHolder = new ThreadLocal<>();

    /**
     * Returns all attributes stored in HTTP session as a Map.
     */
    public static Map<String, Object> extractHttpSessionAttributes() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                sessionAttributes.put(attributeName, session.getAttribute(attributeName));
            }
        }
        return sessionAttributes;
    }

    public static void setAttributes(@Nullable Map<String, Object> attributes) {
        sessionAttributesHolder.set(attributes);
    }

    @Nullable
    public static Object getAttribute(String name) {
        Map<String, Object> sessionAttributes = sessionAttributesHolder.get();
        if (sessionAttributes == null) {
            return null;
        }
        return sessionAttributes.get(name);
    }

    public static void setAttribute(String name, Object value) {
        Map<String, Object> sessionAttributes = sessionAttributesHolder.get();
        if (sessionAttributes == null) {
            sessionAttributes = new HashMap<>();
            sessionAttributesHolder.set(sessionAttributes);
        }
        sessionAttributes.put(name, value);
    }

    public static void clear() {
        sessionAttributesHolder.remove();
    }

    public static boolean isSet() {
        return sessionAttributesHolder.get() != null;
    }
}
