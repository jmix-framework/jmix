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

package com.haulmont.cuba.core.app;

/**
 * Interface to provide basic information about the middleware.
 */
public interface ServerInfoAPI {

    String NAME = "cuba_ServerInfo";

    /**
     * @return release number
     */
    String getReleaseNumber();

    /**
     * @return release timestamp
     */
    String getReleaseTimestamp();

    /**
     * This middleware instance identifier (unique in the current cluster).
     * The identifier has the form <code>host:port/context</code> and is built from the following configuration
     * parameters:
     * <ul>
     *     <li>{@link io.jmix.core.CoreProperties#getWebHostName()}</li>
     *     <li>{@link io.jmix.core.CoreProperties#getWebPort()}</li>
     * </ul>
     *
     * @return this middleware instance identifier
     */
    String getServerId();
}
