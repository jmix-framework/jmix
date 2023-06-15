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
package io.jmix.core.security;

import org.springframework.lang.Nullable;

/**
 * Exception that is raised on attempt to violate a security constraint.
 * <p>
 * You can throw this exception in application code if you want a standard notification about "access denied"
 * to be shown to the user and the event to be logged.
 */
public class AccessDeniedException extends RuntimeException {
    private static final long serialVersionUID = -3097861878301424338L;

    private final String type;
    private final String resource;
    private final String action;

    /**
     * Constructor.
     *
     * @param resource permission target object, e.g. a screen id or entity operation name. When throwing the exception
     *                 in application code, can be any string suitable to describe the situation in the log.
     * @param type     permission type
     */
    public AccessDeniedException(String type, String resource) {
        this(type, resource, null);
    }

    /**
     * Constructor.
     *
     * @param resource permission target object, e.g. a screen id or entity operation name. When throwing the exception
     *                 in application code, can be any string suitable to describe the situation in the log.
     * @param type     permission type
     * @param action   type of operation on resource
     */
    public AccessDeniedException(String type, String resource, @Nullable String action) {
        super(String.format("resource: %s, type: %s, action: %s", resource, type, action));
        this.type = type;
        this.resource = resource;
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }
}
