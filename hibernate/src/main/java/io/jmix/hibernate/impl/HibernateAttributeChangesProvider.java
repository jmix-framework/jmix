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

package io.jmix.hibernate.impl;


import com.google.common.base.Preconditions;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.BaseAttributeChangesProvider;
import io.jmix.hibernate.HibernateConfiguration;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

@Component("hibernate_HibernateAttributeChangesProvider")
public class HibernateAttributeChangesProvider extends BaseAttributeChangesProvider {

    @Autowired
    protected HibernateChangesProvider changesProvider;

    @Autowired
    protected LoadedValueProvider loadedValueProvider;

    @Autowired
    protected HibernateDataProperties dataProperties;

    @Override
    protected void buildChangesByImplementation(AttributeChanges.Builder builder,
                                                Object entity,
                                                BiFunction<Object, MetaProperty, Object> transformer) {
        checkEntityByImplementation(entity);
        builder.mergeChanges(changesProvider.getEntityAttributeChanges(entity));
    }

    @Override
    @Nullable
    protected Object getOldValueByImplementation(Object entity, String attribute) {
        checkEntityByImplementation(entity);

        return loadedValueProvider.getLoadedValue(entity, attribute);
    }

    @Override
    protected Set<String> getChangedAttributeNamesByImplementation(Object entity) {
        checkEntityByImplementation(entity);
        return changesProvider.dirtyFields(entity);
    }

    @Override
    protected boolean isSoftDeletionEnabled() {
        return dataProperties.isSoftDeletionEnabled();
    }

    protected void checkEntityByImplementation(Object entity) {
        Preconditions.checkState(Hibernate.isInitialized(entity),
                "The entity '%s' is not initialized", entity);
    }
}
