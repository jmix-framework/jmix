/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component("core_PersistentAttributesLoadChecker")
public class CorePersistentAttributesLoadChecker implements PersistentAttributesLoadChecker {

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Metadata metadata;

    protected enum PropertyLoadedState {
        YES,
        NO,
        UNKNOWN
    }

    @Override
    public boolean isLoaded(Object entity, String property) {
        if (entity instanceof KeyValueEntity) {
            KeyValueEntity keyValue = (KeyValueEntity) entity;
            return keyValue.getInstanceMetaClass() != null && keyValue.getInstanceMetaClass().findProperty(property) != null;
        }
        MetaClass metaClass = metadata.getClass(entity);

        if (ObjectPathUtils.isSpecialPath(property)) {
            return metadataTools.isAdditionalProperty(metaClass, property);
        }

        MetaProperty metaProperty = metaClass.getProperty(property);

        if (!metadataTools.isJpa(metaProperty)) {
            List<String> dependsOnProperties = metadataTools.getDependsOnProperties(metaProperty);
            if (dependsOnProperties.isEmpty()) {
                return true;
            } else {
                for (String relatedProperty : dependsOnProperties) {
                    if (!isLoaded(entity, relatedProperty))
                        return false;
                }
                return true;
            }
        }

        PropertyLoadedState isLoaded = isLoadedCommonCheck(entity, property);
        if (isLoaded != PropertyLoadedState.UNKNOWN) {
            return isLoaded == PropertyLoadedState.YES;
        }

        return isLoadedSpecificCheck(entity, property, metaClass, metaProperty);
    }

    protected PropertyLoadedState isLoadedCommonCheck(Object entity, String property) {
        if (entity instanceof Entity) {
            return isLoadedByFetchGroup(entity, property);
        }

        return PropertyLoadedState.UNKNOWN;
    }

    protected PropertyLoadedState isLoadedByFetchGroup(Object entity, String property) {
        return PropertyLoadedState.UNKNOWN;
    }

    protected boolean isLoadedSpecificCheck(Object entity, String property, MetaClass metaClass, MetaProperty metaProperty) {
        return checkIsLoadedWithGetter(entity, property);
    }

    protected boolean checkIsLoadedWithGetter(Object entity, String property) {
        if (entity instanceof Entity) {
            try {
                Object value = EntityValues.getValue(entity, property);
                if (value instanceof Collection) { //check for IndirectCollection behaviour, should fail if property is not loaded
                    //noinspection ResultOfMethodCallIgnored
                    ((Collection) value).size();
                }
                return true;
            } catch (Exception ignored) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Unable to check if the attribute is loaded: the entity is of unknown type");
        }
    }
}
