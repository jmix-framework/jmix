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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "jmix.core")
@ConstructorBinding
public class CoreProperties {

    String webHostName;
    String webPort;
    String confDir;
    String workDir;
    String tempDir;
    String dbDir;
    String defaultFileStorage;
    String anonymousAuthenticationTokenKey;
    List<Locale> availableLocales;
    int crossDataStoreReferenceLoadingBatchSize;
    boolean idGenerationForEntitiesInAdditionalDataStoresEnabled;
    int dom4jMaxPoolSize;
    int dom4jMaxBorrowWaitMillis;
    boolean entitySerializationTokenRequired;
    String entitySerializationTokenEncryptionKey;
    boolean legacyFetchPlanSerializationAttributeName;
    boolean triggerFilesEnabled;
    Duration triggerFilesProcessInterval;

    public CoreProperties(
            String webHostName,
            String webPort,
            String confDir,
            String workDir,
            String tempDir,
            String dbDir,
            List<String> availableLocales,
            @DefaultValue("50") int crossDataStoreReferenceLoadingBatchSize,
            @DefaultValue("true") boolean idGenerationForEntitiesInAdditionalDataStoresEnabled,
            @DefaultValue("100") int dom4jMaxPoolSize,
            @DefaultValue("1000") int dom4jMaxBorrowWaitMillis,
            @DefaultValue("de72c623-6d3d-458c-a187-c526de515ecd") String anonymousAuthenticationTokenKey,
            String defaultFileStorage,
            @DefaultValue("false") boolean entitySerializationTokenRequired,
            @DefaultValue("KEY") String entitySerializationTokenEncryptionKey,
            @DefaultValue("false") boolean legacyFetchPlanSerializationAttributeName,
            @DefaultValue("true") boolean triggerFilesEnabled,
            @DefaultValue("5000") Duration triggerFilesProcessInterval) {
        this.webHostName = webHostName;
        this.webPort = webPort;
        this.confDir = confDir;
        this.workDir = workDir;
        this.tempDir = tempDir;
        this.dbDir = dbDir;
        this.defaultFileStorage = defaultFileStorage;
        this.anonymousAuthenticationTokenKey = anonymousAuthenticationTokenKey;

        if (availableLocales == null) {
            this.availableLocales = Collections.singletonList(Locale.ENGLISH);
        } else {
            this.availableLocales = availableLocales.stream()
                    .map(LocaleResolver::resolve)
                    .collect(Collectors.toList());
        }

        this.crossDataStoreReferenceLoadingBatchSize = crossDataStoreReferenceLoadingBatchSize;
        this.idGenerationForEntitiesInAdditionalDataStoresEnabled = idGenerationForEntitiesInAdditionalDataStoresEnabled;
        this.dom4jMaxPoolSize = dom4jMaxPoolSize;
        this.dom4jMaxBorrowWaitMillis = dom4jMaxBorrowWaitMillis;

        this.entitySerializationTokenRequired = entitySerializationTokenRequired;
        this.entitySerializationTokenEncryptionKey = entitySerializationTokenEncryptionKey;
        this.legacyFetchPlanSerializationAttributeName = legacyFetchPlanSerializationAttributeName;
        this.triggerFilesEnabled = triggerFilesEnabled;
        this.triggerFilesProcessInterval = triggerFilesProcessInterval;
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

    public String getWorkDir() {
        return workDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public String getDbDir() {
        return dbDir;
    }

    @Nullable
    public String getDefaultFileStorage() {
        return defaultFileStorage;
    }

    /**
     * List of locales supported by the application.
     * If not specified, contains the single {@code Locale.ENGLISH} element.
     */
    public List<Locale> getAvailableLocales() {
        return availableLocales;
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

    /**
     * @return a key that is used in {@link org.springframework.security.authentication.AnonymousAuthenticationProvider}
     * and {@link org.springframework.security.web.authentication.AnonymousAuthenticationFilter}
     */
    public String getAnonymousAuthenticationTokenKey() {
        return anonymousAuthenticationTokenKey;
    }

    /**
     * @return true if entity serialization uses security token data for deserialization entities with row level security
     */
    public boolean isEntitySerializationTokenRequired() {
        return entitySerializationTokenRequired;
    }

    public String getEntitySerializationTokenEncryptionKey() {
        return entitySerializationTokenEncryptionKey;
    }

    public boolean isLegacyFetchPlanSerializationAttributeName() {
        return legacyFetchPlanSerializationAttributeName;
    }

    /**
     * @return true if enables the processing of bean invocation trigger files. Default value: true
     * The trigger file is a file that is placed in the triggers subdirectory of the application's temporary directory.
     * The file name consists of two parts separated with a #: the first part is the bean class, the second part is the method name
     * of the bean to invoke. For example: io.jmix.core.Messages#clearCache.
     * The trigger files handler monitors the folder for new trigger files, invokes the appropriate methods and then removes the files.
     */
    public boolean isTriggerFilesEnabled() {
        return triggerFilesEnabled;
    }

    /**
     * Defines the period in milliseconds of trigger files processing
     */
    public Duration getTriggerFilesProcessInterval() {
        return triggerFilesProcessInterval;
    }
}
