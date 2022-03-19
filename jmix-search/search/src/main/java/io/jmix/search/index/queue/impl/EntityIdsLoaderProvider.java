/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.index.queue.impl;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Store;
import io.jmix.data.persistence.DbmsType;
import io.jmix.search.index.queue.EntityIdsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("search_EntityIdsLoaderProvider")
public class EntityIdsLoaderProvider {

    private static final Logger log = LoggerFactory.getLogger(EntityIdsLoaderProvider.class);

    protected final Metadata metadata;
    protected final MetadataTools metadataTools;
    protected final DbmsType dbmsType;

    protected final Map<Class<? extends EntityIdsLoader>, EntityIdsLoader> registry = new HashMap<>();

    @Autowired
    public EntityIdsLoaderProvider(Metadata metadata,
                                   MetadataTools metadataTools,
                                   DbmsType dbmsType,
                                   List<EntityIdsLoader> entityIdsLoaders) {
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.dbmsType = dbmsType;
        entityIdsLoaders.forEach(loader -> registry.put(loader.getClass(), loader));
    }

    public EntityIdsLoader getLoader(String entityName) {
        MetaClass entityClass = metadata.getClass(entityName);
        Store store = entityClass.getStore();
        String dbType = dbmsType.getType(store.getName());
        if ("ORACLE".equalsIgnoreCase(dbType)) {
            return registry.get(OracleEntityIdsLoader.class);
        } else {
            return registry.get(CommonJpaEntityIdsLoader.class);
        }
    }
}
