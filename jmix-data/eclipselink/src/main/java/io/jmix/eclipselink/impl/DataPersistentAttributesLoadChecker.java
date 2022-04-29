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

package io.jmix.eclipselink.impl;

import io.jmix.core.Entity;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.NoValueCollection;
import io.jmix.core.impl.CorePersistentAttributesLoadChecker;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.StoreAwareLocator;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManagerFactory;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

public class DataPersistentAttributesLoadChecker extends CorePersistentAttributesLoadChecker {

    private static final Logger log = LoggerFactory.getLogger(DataPersistentAttributesLoadChecker.class);

    private StoreAwareLocator storeAwareLocator;

    public DataPersistentAttributesLoadChecker(ApplicationContext applicationContext) {
        this.storeAwareLocator = applicationContext.getBean(StoreAwareLocator.class);
    }

    @Override
    protected PropertyLoadedState isLoadedByFetchGroup(Object entity, String property) {
        if (entity instanceof FetchGroupTracker) {
            FetchGroup fetchGroup = ((FetchGroupTracker) entity)._persistence_getFetchGroup();
            if (fetchGroup != null) {
                boolean inFetchGroup = fetchGroup.containsAttributeInternal(property);
                if (!inFetchGroup) {
                    // definitely not loaded
                    return PropertyLoadedState.NO;
                } else {
                    // requires additional check specific for the tier
                    return PropertyLoadedState.UNKNOWN;
                }
            }
        }
        return PropertyLoadedState.UNKNOWN;
    }

    @Override
    protected boolean isLoadedSpecificCheck(Object entity, String property, MetaClass metaClass, MetaProperty metaProperty) {
        if (metadataTools.isJpaEmbeddable(metaClass)
                || entity instanceof Entity && getEntityEntry(entity).isNew()) {
            // this is a workaround for unexpected EclipseLink behaviour when PersistenceUnitUtil.isLoaded
            // throws exception if embedded entity refers to persistent entity
            return checkIsLoadedWithGetter(entity, property);
        }
        if (!metadataTools.isJpaEntity(metaClass)) {
            return checkIsLoadedWithGetter(entity, property);
        }

        try {
            Object rawValue = ReflectionHelper.getFieldValue(entity, property);
            if (rawValue instanceof NoValueCollection) {
                return true;//NoValue placeholder should be considered as loaded like null values of just saved entities
            }
        } catch (RuntimeException e) {
            log.debug("Cannot get value for property {} of class {}", property, entity.getClass().getName());
        }

        EntityManagerFactory emf = storeAwareLocator.getEntityManagerFactory(metaClass.getStore().getName());
        return emf.getPersistenceUnitUtil().isLoaded(entity, property);
    }
}
