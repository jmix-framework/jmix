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

package io.jmix.data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;

@ConfigurationProperties(prefix = "jmix.data")
@ConstructorBinding
public class DataProperties {

    boolean useReadOnlyTransactionForLoad;
    int numberIdCacheSize;
    boolean useEntityDataStoreForIdSequence;

    /**
     * Overridden pattern to parse Unique Constraint Violation exception
     */
    String uniqueConstraintViolationPattern;
    boolean useUserLocaleForRelativeDateTimeMoments;

    public DataProperties(
            @DefaultValue("true") boolean useReadOnlyTransactionForLoad,
            @DefaultValue("100") int numberIdCacheSize,
            boolean useEntityDataStoreForIdSequence,
            @Nullable String uniqueConstraintViolationPattern,
            @DefaultValue("true") boolean useUserLocaleForRelativeDateTimeMoments) {
        this.useReadOnlyTransactionForLoad = useReadOnlyTransactionForLoad;
        this.numberIdCacheSize = numberIdCacheSize;
        this.useEntityDataStoreForIdSequence = useEntityDataStoreForIdSequence;
        this.uniqueConstraintViolationPattern = uniqueConstraintViolationPattern;
        this.useUserLocaleForRelativeDateTimeMoments = useUserLocaleForRelativeDateTimeMoments;
    }

    public boolean isUseReadOnlyTransactionForLoad() {
        return useReadOnlyTransactionForLoad;
    }

    public int getNumberIdCacheSize() {
        return numberIdCacheSize;
    }

    public boolean isUseEntityDataStoreForIdSequence() {
        return useEntityDataStoreForIdSequence;
    }

    /**
     * @see #uniqueConstraintViolationPattern
     */
    @Nullable
    public String getUniqueConstraintViolationPattern() {
        return uniqueConstraintViolationPattern;
    }

    public boolean isUseUserLocaleForRelativeDateTimeMoments() {
        return useUserLocaleForRelativeDateTimeMoments;
    }
}
