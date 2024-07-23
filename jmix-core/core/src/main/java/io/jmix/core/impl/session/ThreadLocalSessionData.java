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

import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local storage for session attributes. This class can be used when running a new thread that does not have
 * access to the HTTP session but needs to access session data in the code executed within the thread. In this case, you
 * should copy the current session data to the thread-local storage and then access the thread-local data in your code.
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