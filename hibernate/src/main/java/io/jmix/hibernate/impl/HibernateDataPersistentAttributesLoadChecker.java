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

package io.jmix.hibernate.impl;

import io.jmix.core.Entity;
import io.jmix.core.impl.CorePersistentAttributesLoadChecker;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.StoreAwareLocator;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

public class HibernateDataPersistentAttributesLoadChecker extends CorePersistentAttributesLoadChecker {

    private StoreAwareLocator storeAwareLocator;

    public HibernateDataPersistentAttributesLoadChecker(ApplicationContext applicationContext) {
        this.storeAwareLocator = applicationContext.getBean(StoreAwareLocator.class);
    }

    @Override
    protected PropertyLoadedState isLoadedByFetchGroup(Object entity, String property) {
        PersistenceUtil persistenceUnitUtil = Persistence.getPersistenceUtil();
        if (persistenceUnitUtil.isLoaded(entity)) {

            if (persistenceUnitUtil.isLoaded(entity, property)) {
                return PropertyLoadedState.YES;
            } else {
                return PropertyLoadedState.NO;
            }

        }
        return PropertyLoadedState.UNKNOWN;
    }

    @Override
    protected boolean isLoadedSpecificCheck(Object entity, String property, MetaClass metaClass, MetaProperty metaProperty) {
        if (metadataTools.isEmbeddable(metaClass)
                || entity instanceof Entity && getEntityEntry(entity).isNew()) {
            // this is a workaround for unexpected EclipseLink behaviour when PersistenceUnitUtil.isLoaded
            // throws exception if embedded entity refers to persistent entity
            return checkIsLoadedWithGetter(entity, property);
        }
        if (!metadataTools.isPersistent(metaClass)) {
            return checkIsLoadedWithGetter(entity, property);
        }

        EntityManagerFactory emf = storeAwareLocator.getEntityManagerFactory(metaClass.getStore().getName());
        return emf.getPersistenceUnitUtil().isLoaded(entity, property);
    }
}
