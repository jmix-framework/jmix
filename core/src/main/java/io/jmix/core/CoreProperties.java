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

package io.jmix.core;

import io.jmix.core.security.UserSessions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.core")
@ConstructorBinding
public class CoreProperties {

    String webContextName;
    String webHostName;
    String webPort;
    String confDir;
    String dbDir;
    Map<String, Locale> availableLocales;
    boolean localeSelectVisible;
    int userSessionExpirationTimeoutSec;
    int userSessionSendTimeoutSec;
    int userSessionTouchTimeoutSec;
    boolean syncNewUserSessionReplication;
    int crossDataStoreReferenceLoadingBatchSize;
    boolean idGenerationForEntitiesInAdditionalDataStoresEnabled;
    int dom4jMaxPoolSize;
    int dom4jMaxBorrowWaitMillis;

    public CoreProperties(
            String webContextName,
            String webHostName,
            String webPort,
            String confDir,
            String dbDir,
            Map<String, String> availableLocales,
            @DefaultValue("true") boolean localeSelectVisible,
            @DefaultValue("1800") int userSessionExpirationTimeoutSec,
            @DefaultValue("10") int userSessionSendTimeoutSec,
            @DefaultValue("1") int userSessionTouchTimeoutSec,
            boolean syncNewUserSessionReplication,
            @DefaultValue("50") int crossDataStoreReferenceLoadingBatchSize,
            @DefaultValue("true") boolean idGenerationForEntitiesInAdditionalDataStoresEnabled,
            @DefaultValue("100") int dom4jMaxPoolSize,
            @DefaultValue("1000") int dom4jMaxBorrowWaitMillis
    ) {
        this.webContextName = webContextName;
        this.webHostName = webHostName;
        this.webPort = webPort;
        this.confDir = confDir;
        this.dbDir = dbDir;

        if (availableLocales == null) {
            this.availableLocales = Collections.singletonMap("English", Locale.ENGLISH);
        } else {
            this.availableLocales = new HashMap<>(availableLocales.size());
            for (Map.Entry<String, String> entry : availableLocales.entrySet()) {
                this.availableLocales.put(entry.getValue(), LocaleResolver.resolve(entry.getKey()));
            }
        }

        this.localeSelectVisible = localeSelectVisible;
        this.userSessionExpirationTimeoutSec = userSessionExpirationTimeoutSec;
        this.userSessionSendTimeoutSec = userSessionSendTimeoutSec;
        this.userSessionTouchTimeoutSec = userSessionTouchTimeoutSec;
        this.syncNewUserSessionReplication = syncNewUserSessionReplication;
        this.crossDataStoreReferenceLoadingBatchSize = crossDataStoreReferenceLoadingBatchSize;
        this.idGenerationForEntitiesInAdditionalDataStoresEnabled = idGenerationForEntitiesInAdditionalDataStoresEnabled;
        this.dom4jMaxPoolSize = dom4jMaxPoolSize;
        this.dom4jMaxBorrowWaitMillis = dom4jMaxBorrowWaitMillis;
    }

    /**
     * This web application context name.
     */
    public String getWebContextName() {
        return webContextName;
    }

    /**
     * This web application host name.
     */
    public String getWebHostName() {
        return webHostName;
    }

    /**
     * This web application port.
     */
    public String getWebPort() {
        return webPort;
    }

    public String getConfDir() {
        return confDir;
    }

    public String getDbDir() {
        return dbDir;
    }

    public Map<String, Locale> getAvailableLocales() {
        return availableLocales;
    }

    public boolean isLocaleSelectVisible() {
        return localeSelectVisible;
    }

    /**
     * User session expiration timeout in seconds.
     */
    public int getUserSessionExpirationTimeoutSec() {
        return userSessionExpirationTimeoutSec;
    }

    /**
     * User session ping timeout in cluster.
     * If ping is performed by {@link UserSessions#getAndRefresh},
     * the user session is sent to the cluster only after the specified timeout.
     */
    public int getUserSessionSendTimeoutSec() {
        return userSessionSendTimeoutSec;
    }

    public int getUserSessionTouchTimeoutSec() {
        return userSessionTouchTimeoutSec;
    }

    public boolean isSyncNewUserSessionReplication() {
        return syncNewUserSessionReplication;
    }

    public int getCrossDataStoreReferenceLoadingBatchSize() {
        return crossDataStoreReferenceLoadingBatchSize;
    }

    /**
     * Whether to generate identifiers for entities located in additional data stores.
     * Default is true.
     */
    public boolean isIdGenerationForEntitiesInAdditionalDataStoresEnabled() {
        return idGenerationForEntitiesInAdditionalDataStoresEnabled;
    }

    /**
     * Maximum number of SAXParser instances available for concurrent use.
     */
    public int getDom4jMaxPoolSize() {
        return dom4jMaxPoolSize;
    }

    /**
     * Timeout to borrow SAXParser instance from object pool.
     */
    public int getDom4jMaxBorrowWaitMillis() {
        return dom4jMaxBorrowWaitMillis;
    }
}
