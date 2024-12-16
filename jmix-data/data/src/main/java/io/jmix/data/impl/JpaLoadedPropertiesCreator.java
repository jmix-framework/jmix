/*
 * Copyright 2024 Haulmont.
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

package io.jmix.data.impl;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("data_JpaLoadedPropertiesCreator")
public class JpaLoadedPropertiesCreator {

    private static final Logger log = LoggerFactory.getLogger(JpaLoadedPropertiesCreator.class);

    private final MetadataTools metadataTools;
    private final Metadata metadata;

    public JpaLoadedPropertiesCreator(MetadataTools metadataTools, Metadata metadata) {
        this.metadataTools = metadataTools;
        this.metadata = metadata;
    }

    public void addProperty(Object entity, String attributeName) {
        getLoadedProperties(entity).add(attributeName);
    }

    public void fillLoadedProperties(Object entity, EntityManagerFactory emf) {
        fillLoadedProperties(entity, emf, new HashSet<>());
    }

    private void fillLoadedProperties(Object entity, EntityManagerFactory emf, Set<Object> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        MetaClass metaClass = metadata.getClass(entity);

        for (MetaProperty property : metaClass.getProperties()) {
            if (metadataTools.isJpa(property)) {
                boolean loaded;
                if (metadataTools.isJpaEmbeddable(metaClass)) {
                    // this is a workaround for unexpected EclipseLink behaviour when PersistenceUnitUtil.isLoaded
                    // throws exception if embedded entity refers to persistent entity
                    loaded = checkIsLoadedWithGetter(entity, property.getName());
                } else {
                    loaded = emf.getPersistenceUnitUtil().isLoaded(entity, property.getName());
                }
                log.trace("Check: {}.{} is loaded = {}", entity, property.getName(), loaded);
                if (loaded) {
                    addProperty(entity, property.getName());
                    if (property.getRange().isClass()) {
                        Object value = EntityValues.getValue(entity, property.getName());
                        if (value != null) {
                            if (value instanceof Collection) {
                                for (Object item : ((Collection<?>) value)) {
                                    fillLoadedProperties(item, emf, visited);
                                }
                            } else {
                                fillLoadedProperties(value, emf, visited);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkIsLoadedWithGetter(Object entity, String property) {
        try {
            Object value = EntityValues.getValue(entity, property);
            if (value instanceof Collection) {
                //check for IndirectCollection behaviour, should fail if property is not loaded
                //noinspection ResultOfMethodCallIgnored
                ((Collection<?>) value).size();
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Set<String> getLoadedProperties(Object entity) {
        Set<String> loadedProperties = EntitySystemAccess.getEntityEntry(entity).getLoadedProperties();
        if (loadedProperties == null) {
            loadedProperties = new HashSet<>();
            EntitySystemAccess.getEntityEntry(entity).setLoadedProperties(loadedProperties);
        }
        return loadedProperties;
    }
}
