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

package io.jmix.eclipselink.impl;


import com.google.common.base.Preconditions;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.BaseAttributeChangesProvider;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.sessions.changesets.AggregateChangeRecord;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

@Component("eclipselink_EclipselinkAttributeChangesProvider")
public class EclipselinkAttributeChangesProvider extends BaseAttributeChangesProvider {
    protected EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected void buildChangesByImplementation(AttributeChanges.Builder builder,
                                                Object entity,
                                                BiFunction<Object, MetaProperty, Object> transformer) {
        checkEntityByImplementation(entity);

        PropertyChangeListener propertyChangeListener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();

        ObjectChangeSet objectChanges = ((AttributeChangeListener) propertyChangeListener).getObjectChangeSet();
        if (objectChanges != null) {
            buildChanges(builder, objectChanges, metadata.getClass(entity), transformer);
        }
    }

    @Override
    @Nullable
    protected Object getOldValueByImplementation(Object entity, String attribute) {
        checkEntityByImplementation(entity);

        PropertyChangeListener propertyChangeListener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();

        ObjectChangeSet objectChanges = ((AttributeChangeListener) propertyChangeListener).getObjectChangeSet();
        if (objectChanges != null) {
            ChangeRecord changeRecord = objectChanges.getChangesForAttributeNamed(attribute);
            if (changeRecord != null) {
                return changeRecord.getOldValue();
            }
        }

        return null;
    }

    @Override
    protected Set<String> getChangedAttributeNamesByImplementation(Object entity) {
        checkEntityByImplementation(entity);

        PropertyChangeListener propertyChangeListener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();

        ObjectChangeSet objectChanges = ((AttributeChangeListener) propertyChangeListener).getObjectChangeSet();
        if (objectChanges != null) {
            return new HashSet<>(objectChanges.getChangedAttributeNames());
        }

        return Collections.emptySet();
    }

    @Override
    protected boolean isSoftDeletionEnabled() {
        return PersistenceHints.isSoftDeletion(entityManager);
    }

    protected void checkEntityByImplementation(Object entity) {
        Preconditions.checkState(entity instanceof ChangeTracker,
                "The entity '%s' is is not a ChangeTracker", entity);

        PropertyChangeListener propertyChangeListener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();

        Preconditions.checkState(propertyChangeListener != null,
                "Entity '%s' is a ChangeTracker but has no PropertyChangeListener", entity);
    }

    protected void buildChanges(AttributeChanges.Builder builder,
                                @Nullable ObjectChangeSet objectChangeSet,
                                MetaClass metaClass,
                                BiFunction<Object, MetaProperty, Object> transformer) {
        if (objectChangeSet == null)
            return;

        for (ChangeRecord changeRecord : objectChangeSet.getChanges()) {
            String propertyName = changeRecord.getAttribute();
            MetaProperty metaProperty = metaClass.getProperty(propertyName);

            builder.withChange(propertyName, transformer.apply(changeRecord.getOldValue(), metaProperty));

            if (changeRecord instanceof AggregateChangeRecord) {
                builder.withEmbedded(propertyName, embeddedBuilder -> {
                    buildChanges(embeddedBuilder, ((AggregateChangeRecord) changeRecord).getChangedObject(),
                            metaProperty.getRange().asClass(), transformer);
                });
            }
        }
    }
}
