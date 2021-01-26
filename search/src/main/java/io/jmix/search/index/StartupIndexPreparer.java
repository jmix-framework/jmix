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

package io.jmix.search.index;

import io.jmix.search.index.mapping.AnnotatedIndexDefinitionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component(StartupIndexPreparer.NAME)
public class StartupIndexPreparer {

    private static final Logger log = LoggerFactory.getLogger(StartupIndexPreparer.class);

    public static final String NAME = "search_StartupIndexPreparer";

    @Autowired
    protected IndexManager indexManager;

    @Autowired
    protected AnnotatedIndexDefinitionsProvider indexDefinitionsProvider;

    @EventListener(ApplicationReadyEvent.class) //todo Prepare indexes during initialization
    public void prepareIndexes() {
        log.info("[IVGA] Prepare indexes");
        Collection<IndexDefinition> indexDefinitions = indexDefinitionsProvider.getIndexDefinitions();
        indexDefinitions.forEach(this::prepareIndex);
    }

    protected void prepareIndex(IndexDefinition indexDefinition) {
        log.info("[IVGA] Prepare index '{}'", indexDefinition.getIndexName());
        try {
            boolean indexExist = indexManager.isIndexExist(indexDefinition.getIndexName());
            if (indexExist) {
                log.info("[IVGA] Index '{}' already exists", indexDefinition.getIndexName());
                //todo compare mapping & settings
            } else {
                log.info("[IVGA] Index '{}' does not exists. Create", indexDefinition.getIndexName());
                indexManager.createIndex(indexDefinition);
            }
        } catch (IOException e) {
            log.error("[IVGA] Unable to prepare index '{}'", indexDefinition.getIndexName(), e);
        }
    }
}
