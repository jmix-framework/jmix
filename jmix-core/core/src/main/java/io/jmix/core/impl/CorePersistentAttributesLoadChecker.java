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

import io.jmix.core.*;
import io.jmix.core.EntityStates.PropertyLoadedState;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;


import static io.jmix.core.EntityStates.PropertyLoadedState.*;

@Component("core_PersistentAttributesLoadChecker")
public class CorePersistentAttributesLoadChecker implements PersistentAttributesLoadChecker {

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Metadata metadata;


    @Override
    public boolean isLoaded(Object entity, String property) {
        return YES.equals(isLoadedInternal(entity, property, false));
    }

    /**
     * Checks whether the {@code property} of the {@code entity} is loaded.
     *
     * @param safe determines the behavior when the loaded state cannot be determined using standard JPA mechanisms:
     *             if {@code true}, {@code UNKNOWN} is returned;
     *             if {@code false}, the getter is invoked, which may trigger lazy loading
     */
    public PropertyLoadedState isLoadedInternal(Object entity, String property, boolean safe) {
        if (entity instanceof KeyValueEntity) {
            KeyValueEntity keyValue = (KeyValueEntity) entity;
            return PropertyLoadedState.fromBoolean(keyValue.getInstanceMetaClass() != null && keyValue.getInstanceMetaClass().findProperty(property) != null);
        }

        MetaClass metaClass = metadata.getClass(entity);

        if (ObjectPathUtils.isSpecialPath(property)) {
            return PropertyLoadedState.fromBoolean(metadataTools.isAdditionalProperty(metaClass, property));
        }

        MetaProperty metaProperty = metaClass.getProperty(property);

        if (!metadataTools.isJpa(metaProperty)) {
            List<String> dependsOnProperties = metadataTools.getDependsOnProperties(metaProperty);
            if (dependsOnProperties.isEmpty()) {
                return YES;
            } else {
                boolean fullFetchingOfAllPropertiesGuaranteed = true;
                for (String relatedPropertyName : dependsOnProperties) {
                    MetaProperty relatedProperty = metaClass.getProperty(relatedPropertyName);
                    if (relatedProperty.getRange().isClass()) {
                        fullFetchingOfAllPropertiesGuaranteed = false;
                    }
                    PropertyLoadedState propertyState = isLoadedInternal(entity, relatedPropertyName, safe);
                    if (propertyState == NO)
                        return NO;
                }

                if (fullFetchingOfAllPropertiesGuaranteed) {
                    return YES;
                } else {
                    return safe ? UNKNOWN : checkIsLoadedWithGetter(entity, property);
                }
            }
        }

        PropertyLoadedState isLoaded = isLoadedCommonCheck(entity, property);
        if (isLoaded != UNKNOWN) {
            return isLoaded;
        }

        return isLoadedSpecificCheck(entity, property, metaClass, metaProperty, safe);
    }

    @Override
    public PropertyLoadedState isLoadedSafe(Object entity, String property) {
        return isLoadedInternal(entity, property, true);
    }

    protected PropertyLoadedState isLoadedCommonCheck(Object entity, String property) {
        if (entity instanceof Entity) {
            return isLoadedByFetchGroup(entity, property);
        }

        return UNKNOWN;
    }

    protected PropertyLoadedState isLoadedByFetchGroup(Object entity, String property) {
        return UNKNOWN;
    }

    /**
     * Checks whether the {@code property} of the {@code entity} is loaded using store-specific mechanisms.
     *
     * @param safe determines the behavior when the loaded state cannot be determined using standard mechanisms:
     *             if {@code true}, {@code UNKNOWN} is returned;
     *             if {@code false}, the getter is invoked, which may trigger lazy loading
     */
    protected PropertyLoadedState isLoadedSpecificCheck(Object entity, String property, MetaClass metaClass, MetaProperty metaProperty, boolean safe) {
        return safe ? UNKNOWN : checkIsLoadedWithGetter(entity, property);
    }

    protected PropertyLoadedState checkIsLoadedWithGetter(Object entity, String property) {
        if (entity instanceof Entity) {
            try {
                Object value = EntityValues.getValue(entity, property);
                if (value instanceof Collection) { //check for IndirectCollection behaviour, should fail if property is not loaded
                    //noinspection ResultOfMethodCallIgnored
                    ((Collection) value).size();
                }
                return YES;
            } catch (Exception ignored) {
                return NO;
            }
        } else {
            throw new IllegalArgumentException("Unable to check if the attribute is loaded: the entity is of unknown type");
        }
    }
}
