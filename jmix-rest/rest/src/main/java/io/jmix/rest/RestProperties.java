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

package io.jmix.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "jmix.rest")
public class RestProperties {

    /**
     * Base REST URL path, '/rest' by default.
     */
    private final String basePath;

    /**
     * Entities endpoint path, '/entities' by default.
     */
    private final String entitiesPath;

    /**
     * Docs endpoint path, '/docs' by default.
     */
    private final String docsPath;

    /**
     * Metadata endpoint path, '/metadata' by default.
     */
    private final String metadataPath;

    /**
     * Files endpoint path, '/files' by default.
     */
    private final String filesPath;

    /**
     * Messages endpoint path, '/messages' by default.
     */
    private final String messagesPath;

    /**
     * Permissions endpoint path, '/permissions' by default.
     */
    private final String permissionsPath;

    /**
     * Queries endpoint path, '/queries' by default.
     */
    private final String queriesPath;

    /**
     * Services endpoint path, '/services' by default.
     */
    private final String servicesPath;

    /**
     * UserInfo endpoint path, '/userInfo' by default.
     */
    private final String userInfoPath;

    /**
     * UserSession endpoint path, '/user-session' by default.
     */
    private final String userSessionPath;

    /**
     * Capabilities endpoint path, '/capabilities' by default.
     */
    private final String capabilitiesPath;

    /**
     * Whether the passed entities versions should be validated before entities are persisted.
     */
    private final boolean optimisticLockingEnabled;

    /**
     * Whether "responseView" param is required.
     */
    private final boolean responseFetchPlanEnabled;

    private final int defaultMaxFetchSize;
    private final Map<String, Integer> entityMaxFetchSize;

    /**
     * Whether inline fetch plans are enabled in entities and queries endpoints (true by default).
     */
    private final boolean inlineFetchPlanEnabled;

    /**
     * File extensions that can be opened for viewing in a browser by replying with 'Content-Disposition=inline' header.
     */
    protected Set<String> inlineEnabledFileExtensions;

    public RestProperties(
            @DefaultValue("/rest") String basePath,
            @DefaultValue("/entities") String entitiesPath,
            @DefaultValue("/docs") String docsPath,
            @DefaultValue("/metadata") String metadataPath,
            @DefaultValue("/files") String filesPath,
            @DefaultValue("/messages") String messagesPath,
            @DefaultValue("/permissions") String permissionsPath,
            @DefaultValue("/queries") String queriesPath,
            @DefaultValue("/services") String servicesPath,
            @DefaultValue("/userInfo") String userInfoPath,
            @DefaultValue("/user-session") String userSessionPath,
            @DefaultValue("/capabilities") String capabilitiesPath,
            @DefaultValue("false") boolean optimisticLockingEnabled,
            @DefaultValue("true") boolean responseFetchPlanEnabled,
            @DefaultValue("10000") int defaultMaxFetchSize,
            @DefaultValue({"jpg", "png", "jpeg", "pdf"}) Set<String> inlineEnabledFileExtensions,
            @Nullable Map<String, Integer> entityMaxFetchSize,
            @DefaultValue("true") boolean inlineFetchPlanEnabled) {
        this.basePath = checkPath("jmix.rest.base-path", basePath);
        this.entitiesPath = checkPath("jmix.rest.entities-path", entitiesPath);
        this.docsPath = checkPath("jmix.rest.docs-path", docsPath);
        this.metadataPath = checkPath("jmix.rest.metadata-path", metadataPath);
        this.filesPath = checkPath("jmix.rest.files-path", filesPath);
        this.messagesPath = checkPath("jmix.rest.messages-path", messagesPath);
        this.permissionsPath = checkPath("jmix.rest.permissions-path", permissionsPath);
        this.queriesPath = checkPath("jmix.rest.queries-path", queriesPath);
        this.servicesPath = checkPath("jmix.rest.services-path", servicesPath);
        this.userInfoPath = checkPath("jmix.rest.user-info-path", userInfoPath);
        this.userSessionPath = checkPath("jmix.rest.user-session-path", userSessionPath);
        this.capabilitiesPath = checkPath("jmix.rest.capabilities-path", capabilitiesPath);
        this.optimisticLockingEnabled = optimisticLockingEnabled;
        this.responseFetchPlanEnabled = responseFetchPlanEnabled;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
        this.inlineEnabledFileExtensions = inlineEnabledFileExtensions;
        this.inlineFetchPlanEnabled = inlineFetchPlanEnabled;
    }

    private String checkPath(String property, @Nullable String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Property '%s' must not be blank".formatted(property));
        if (!value.startsWith("/") || value.endsWith("/"))
            throw new IllegalArgumentException("Property '%s' must start with '/' and not end with '/'".formatted(property));
        return value;
    }

    /**
     * @see #basePath
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * @see #entitiesPath
     */
    public String getEntitiesPath() {
        return entitiesPath;
    }

    /**
     * @see #docsPath
     */
    public String getDocsPath() {
        return docsPath;
    }

    /**
     * @see #metadataPath
     */
    public String getMetadataPath() {
        return metadataPath;
    }

    /**
     * @see #filesPath
     */
    public String getFilesPath() {
        return filesPath;
    }

    /**
     * @see #messagesPath
     */
    public String getMessagesPath() {
        return messagesPath;
    }

    /**
     * @see #permissionsPath
     */
    public String getPermissionsPath() {
        return permissionsPath;
    }

    /**
     * @see #queriesPath
     */
    public String getQueriesPath() {
        return queriesPath;
    }

    /**
     * @see #servicesPath
     */
    public String getServicesPath() {
        return servicesPath;
    }

    /**
     * @see #userInfoPath
     */
    public String getUserInfoPath() {
        return userInfoPath;
    }

    /**
     * @see #userSessionPath
     */
    public String getUserSessionPath() {
        return userSessionPath;
    }

    /**
     * @see #capabilitiesPath
     */
    public String getCapabilitiesPath() {
        return capabilitiesPath;
    }

    /**
     * @see #optimisticLockingEnabled
     */
    public boolean isOptimisticLockingEnabled() {
        return optimisticLockingEnabled;
    }

    /**
     * @see #responseFetchPlanEnabled
     */
    public boolean isResponseFetchPlanEnabled() {
        return responseFetchPlanEnabled;
    }

    /**
     * @see #inlineEnabledFileExtensions
     */
    public Set<String> getInlineEnabledFileExtensions() {
        return inlineEnabledFileExtensions;
    }

    public int getEntityMaxFetchSize(String entityName) {
        return entityMaxFetchSize.getOrDefault(entityName, defaultMaxFetchSize);
    }

    /**
     * @see #inlineFetchPlanEnabled
     */
    public boolean isInlineFetchPlanEnabled() {
        return inlineFetchPlanEnabled;
    }
}
