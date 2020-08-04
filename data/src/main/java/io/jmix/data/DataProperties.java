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

@ConfigurationProperties(prefix = "jmix.data")
@ConstructorBinding
public class DataProperties {

    boolean inMemoryDistinct;
    boolean useReadOnlyTransactionForLoad;
    boolean queryCacheEnabled;
    int queryCacheMaxSize;
    int numberIdCacheSize;
    boolean useEntityDataStoreForIdSequence;

    public DataProperties(
            boolean inMemoryDistinct,
            @DefaultValue("true") boolean useReadOnlyTransactionForLoad,
            @DefaultValue("true") boolean queryCacheEnabled,
            @DefaultValue("100") int queryCacheMaxSize,
            @DefaultValue("100") int numberIdCacheSize,
            boolean useEntityDataStoreForIdSequence
    ) {
        this.inMemoryDistinct = inMemoryDistinct;
        this.useReadOnlyTransactionForLoad = useReadOnlyTransactionForLoad;
        this.queryCacheEnabled = queryCacheEnabled;
        this.queryCacheMaxSize = queryCacheMaxSize;
        this.numberIdCacheSize = numberIdCacheSize;
        this.useEntityDataStoreForIdSequence = useEntityDataStoreForIdSequence;
    }

    public boolean isInMemoryDistinct() {
        return inMemoryDistinct;
    }

    public boolean isUseReadOnlyTransactionForLoad() {
        return useReadOnlyTransactionForLoad;
    }

    public boolean isQueryCacheEnabled() {
        return queryCacheEnabled;
    }

    public int getQueryCacheMaxSize() {
        return queryCacheMaxSize;
    }

    public int getNumberIdCacheSize() {
        return numberIdCacheSize;
    }

    public boolean isUseEntityDataStoreForIdSequence() {
        return useEntityDataStoreForIdSequence;
    }
}
