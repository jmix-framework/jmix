/*
 * Copyright 2025 Haulmont.
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

package io.jmix.sessions.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "jmix.sessions.expiring-map")
public class JmixExpiringMapProperties {

    /**
     * Whether expired sessions should be deleted every {@code cleanupTimeout}
     */
    protected Boolean cleanupEnabled;

    /**
     * Period to clean up expired sessions.
     */
    protected Duration cleanupTimeout;

    public JmixExpiringMapProperties(@DefaultValue("true") Boolean cleanupEnabled,
                                     @DefaultValue("5m") Duration cleanupTimeout) {
        this.cleanupEnabled = cleanupEnabled;
        this.cleanupTimeout = cleanupTimeout;
    }

    /**
     * @see #cleanupEnabled
     */
    public Boolean getCleanupEnabled() {
        return cleanupEnabled;
    }

    /**
     * @see #cleanupTimeout
     */
    public Duration getCleanupTimeout() {
        return cleanupTimeout;
    }
}
