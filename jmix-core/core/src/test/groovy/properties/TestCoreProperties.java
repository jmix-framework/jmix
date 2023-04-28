/*
 * Copyright 2022 Haulmont.
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

package properties;

import io.jmix.core.CoreProperties;

import java.time.Duration;
import java.util.List;

public class TestCoreProperties extends CoreProperties {

    public TestCoreProperties(String webHostName,
                              String webPort,
                              String confDir,
                              String workDir,
                              String tempDir,
                              String dbDir,
                              List<String> availableLocales,
                              int crossDataStoreReferenceLoadingBatchSize,
                              boolean idGenerationForEntitiesInAdditionalDataStoresEnabled,
                              int dom4jMaxPoolSize,
                              int dom4jMaxBorrowWaitMillis,
                              String anonymousAuthenticationTokenKey,
                              String defaultFileStorage,
                              boolean entitySerializationTokenRequired,
                              String entitySerializationTokenEncryptionKey,
                              boolean legacyFetchPlanSerializationAttributeName,
                              boolean triggerFilesEnabled,
                              Duration triggerFilesProcessInterval,
                              PessimisticLock pessimisticLock,
                              boolean decimalValueRoundByFormat) {
        super(webHostName, webPort, confDir, workDir, tempDir, dbDir, availableLocales,
                crossDataStoreReferenceLoadingBatchSize, idGenerationForEntitiesInAdditionalDataStoresEnabled,
                dom4jMaxPoolSize, dom4jMaxBorrowWaitMillis, anonymousAuthenticationTokenKey, defaultFileStorage,
                entitySerializationTokenRequired, entitySerializationTokenEncryptionKey,
                legacyFetchPlanSerializationAttributeName, triggerFilesEnabled, triggerFilesProcessInterval,
                pessimisticLock, decimalValueRoundByFormat);
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        String webHostName;
        String webPort;
        String confDir;
        String workDir;
        String tempDir;
        String dbDir;
        List<String> availableLocales;
        int crossDataStoreReferenceLoadingBatchSize = 50;
        boolean idGenerationForEntitiesInAdditionalDataStoresEnabled = true;
        int dom4jMaxPoolSize = 100;
        int dom4jMaxBorrowWaitMillis = 1000;
        String anonymousAuthenticationTokenKey = "de72c623-6d3d-458c-a187-c526de515ecd";
        String defaultFileStorage;
        boolean entitySerializationTokenRequired = false;
        String entitySerializationTokenEncryptionKey = "KEY";
        boolean legacyFetchPlanSerializationAttributeName = false;
        boolean triggerFilesEnabled = true;
        Duration triggerFilesProcessInterval = Duration.ofSeconds(5000);
        PessimisticLock pessimisticLock = new PessimisticLock("0 * * * * ?", true);
        boolean decimalValueRoundByFormat = true;

        public Builder setWebHostName(String webHostName) {
            this.webHostName = webHostName;
            return this;
        }

        public Builder setWebPort(String webPort) {
            this.webPort = webPort;
            return this;
        }

        public Builder setConfDir(String confDir) {
            this.confDir = confDir;
            return this;
        }

        public Builder setWorkDir(String workDir) {
            this.workDir = workDir;
            return this;
        }

        public Builder setTempDir(String tempDir) {
            this.tempDir = tempDir;
            return this;
        }

        public Builder setDbDir(String dbDir) {
            this.dbDir = dbDir;
            return this;
        }

        public Builder setAvailableLocales(List<String> availableLocales) {
            this.availableLocales = availableLocales;
            return this;
        }

        public Builder setCrossDataStoreReferenceLoadingBatchSize(int crossDataStoreReferenceLoadingBatchSize) {
            this.crossDataStoreReferenceLoadingBatchSize = crossDataStoreReferenceLoadingBatchSize;
            return this;
        }

        public Builder setIdGenerationForEntitiesInAdditionalDataStoresEnabled(boolean idGenerationForEntitiesInAdditionalDataStoresEnabled) {
            this.idGenerationForEntitiesInAdditionalDataStoresEnabled = idGenerationForEntitiesInAdditionalDataStoresEnabled;
            return this;
        }

        public Builder setDom4jMaxPoolSize(int dom4jMaxPoolSize) {
            this.dom4jMaxPoolSize = dom4jMaxPoolSize;
            return this;
        }

        public Builder setDom4jMaxBorrowWaitMillis(int dom4jMaxBorrowWaitMillis) {
            this.dom4jMaxBorrowWaitMillis = dom4jMaxBorrowWaitMillis;
            return this;
        }

        public Builder setAnonymousAuthenticationTokenKey(String anonymousAuthenticationTokenKey) {
            this.anonymousAuthenticationTokenKey = anonymousAuthenticationTokenKey;
            return this;
        }

        public Builder setDefaultFileStorage(String defaultFileStorage) {
            this.defaultFileStorage = defaultFileStorage;
            return this;
        }

        public Builder setEntitySerializationTokenRequired(boolean entitySerializationTokenRequired) {
            this.entitySerializationTokenRequired = entitySerializationTokenRequired;
            return this;
        }

        public Builder setEntitySerializationTokenEncryptionKey(String entitySerializationTokenEncryptionKey) {
            this.entitySerializationTokenEncryptionKey = entitySerializationTokenEncryptionKey;
            return this;
        }

        public Builder setLegacyFetchPlanSerializationAttributeName(boolean legacyFetchPlanSerializationAttributeName) {
            this.legacyFetchPlanSerializationAttributeName = legacyFetchPlanSerializationAttributeName;
            return this;
        }

        public Builder setTriggerFilesEnabled(boolean triggerFilesEnabled) {
            this.triggerFilesEnabled = triggerFilesEnabled;
            return this;
        }

        public Builder setTriggerFilesProcessInterval(Duration triggerFilesProcessInterval) {
            this.triggerFilesProcessInterval = triggerFilesProcessInterval;
            return this;
        }

        public Builder setPessimisticLock(PessimisticLock pessimisticLock) {
            this.pessimisticLock = pessimisticLock;
            return this;
        }

        public Builder setDecimalValueRoundByFormat(boolean decimalValueRoundByFormat) {
            this.decimalValueRoundByFormat = decimalValueRoundByFormat;
            return this;
        }

        public TestCoreProperties build() {
            return new TestCoreProperties(
                    this.webHostName,
                    this.webPort,
                    this.confDir,
                    this.workDir,
                    this.tempDir,
                    this.dbDir,
                    this.availableLocales,
                    this.crossDataStoreReferenceLoadingBatchSize,
                    this.idGenerationForEntitiesInAdditionalDataStoresEnabled,
                    this.dom4jMaxPoolSize,
                    this.dom4jMaxBorrowWaitMillis,
                    this.anonymousAuthenticationTokenKey,
                    this.defaultFileStorage,
                    this.entitySerializationTokenRequired,
                    this.entitySerializationTokenEncryptionKey,
                    this.legacyFetchPlanSerializationAttributeName,
                    this.triggerFilesEnabled,
                    this.triggerFilesProcessInterval,
                    this.pessimisticLock,
                    this.decimalValueRoundByFormat);
        }
    }
}
