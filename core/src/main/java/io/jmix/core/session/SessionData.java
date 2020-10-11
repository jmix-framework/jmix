/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.session;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Enumeration;

/**
 * Session scoped bean to provide {@link HttpSession} with attributes.
 **/
public interface SessionData extends Serializable {

    /**
     * Return all session attributes
     **/
    Enumeration<String> getAttributeNames();

    /**
     * Get session attribute
     *
     * @param name attribute name
     * @return attribute value
     **/
    Object getAttribute(String name);

    /**
     * Set session attribute
     *
     * @param name      attribute name
     * @param attribute attribute value
     **/
    void setAttribute(String name, Object attribute);

    /**
     * Provide current session id
     *
     * @return current session id
     **/
    String getSessionId();

    /**
     * Provide current http session
     *
     * @return current http session
     **/
    HttpSession getHttpSession();
}

