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

package io.jmix.autoconfigure.ui.vaadin;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.vaadin.annotations.VaadinServletConfiguration;

/**
 * Configuration properties for Vaadin Spring Boot. These correspond to the
 * similarly named parameters of {@link VaadinServletConfiguration} and can be
 * set in various ways.
 *
 * @see <a
 *      href="http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html">http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html</a>
 */
@ConfigurationProperties(prefix = "vaadin.servlet")
public class VaadinServletConfigurationProperties {
    // note that the explicit values are used instead of constant references to
    // allow the annotation processor to parse these, and pay attention to the
    // special formatting of the field javadoc - see
    // http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#configuration-metadata-annotation-processor

    /**
     * The time resources can be cached in the browser in seconds (default one
     * hour).
     */
    private int resourceCacheTime = 3600; // DefaultDeploymentConfiguration.DEFAULT_RESOURCE_CACHE_TIME;

    /**
     * The number of seconds between heartbeat requests of a UI or a
     * non-positive number to disable heartbeat (default 5 minutes).
     */
    private int heartbeatInterval = 300; // DefaultDeploymentConfiguration.DEFAULT_HEARTBEAT_INTERVAL;

    /**
     * Whether a session should be closed when all its open UIs have been idle
     * for longer than its configured maximum inactivity time (default false).
     */
    private boolean closeIdleSessions = false; // DefaultDeploymentConfiguration.DEFAULT_CLOSE_IDLE_SESSIONS;

    /**
     * Location of static resources.
     */
    private String resources = null;

    /**
     * The base URL mapping of the Vaadin servlet. By default, the mapping
     * {@code /*} is used.
     *
     * If a value other than {@code /*} is used, also {@code /VAADIN/*} is
     * automatically mapped to the same servlet.
     */
    private String urlMapping = null;

    public int getResourceCacheTime() {
        return resourceCacheTime;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public boolean isCloseIdleSessions() {
        return closeIdleSessions;
    }

    public String getResources() {
        return resources;
    }

    public String getUrlMapping() {
        return urlMapping;
    }

    public void setResourceCacheTime(int resourceCacheTime) {
        this.resourceCacheTime = resourceCacheTime;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public void setCloseIdleSessions(boolean closeIdleSessions) {
        this.closeIdleSessions = closeIdleSessions;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setUrlMapping(String urlMapping) {
        this.urlMapping = urlMapping;
    }
}
