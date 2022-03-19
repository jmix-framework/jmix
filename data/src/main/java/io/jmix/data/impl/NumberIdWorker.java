/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.data.impl;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Stores;
import io.jmix.data.DataProperties;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generates ids for entities with long/integer PK using database sequences.
 */
@Component("data_NumberIdWorker")
public class NumberIdWorker {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Sequences sequences;

    @Autowired
    protected DataProperties dataProperties;

    public Long createLongId(String entityName, String sequenceName) {
        Sequence sequence = Sequence.withName(getSequenceName(entityName, sequenceName))
                .setStore(getDataStore(entityName))
                .setStartValue(1)
                .setIncrement(1);

        return sequences.createNextValue(sequence);
    }

    public Long createCachedLongId(String entityName, String sequenceName) {
        Sequence sequence = Sequence.withName(getSequenceName(entityName, sequenceName))
                .setStore(getDataStore(entityName))
                .setStartValue(0)
                .setIncrement(dataProperties.getNumberIdCacheSize());

        return sequences.createNextValue(sequence);
    }

    /**
     * INTERNAL. Used by tests.
     */
    public void reset() {
        ((SequencesImpl) sequences).reset();
    }

    protected String getDataStore(String entityName) {
        if (!dataProperties.isUseEntityDataStoreForIdSequence()) {
            return Stores.MAIN;
        } else {
            return metadata.getClass(entityName).getStore().getName();
        }
    }

    protected String getSequenceName(String entityName, String sequenceName) {
        if (StringUtils.isBlank(entityName))
            throw new IllegalArgumentException("entityName is blank");

        if (StringUtils.isNotBlank(sequenceName))
            return sequenceName;

        return "seq_id_" + entityName.replace("$", "_");
    }
}
