/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.FluentLoader;
import io.jmix.core.Sort;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.search.index.queue.entity.EnqueueingSession;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads ids of an entity that is not a JPA entity, for example a dynamic entity stored in a custom
 * data store. Pages through the entity ordered by the session's ordering property using the
 * store's own condition support instead of JPQL.
 */
@NullMarked
@Component("search_NonJpaEntityIdsLoader")
public class NonJpaEntityIdsLoader extends OrderBasedEntityIdsLoader {

    @Override
    public ResultHolder loadNextIds(EnqueueingSession session, int batchSize) {
        MetaClass metaClass = metadata.getClass(session.getEntityName());
        MetaProperty orderingProperty = metaClass.getProperty(session.getOrderingProperty());
        String orderingPropertyName = orderingProperty.getName();
        Object lastProcessedValue = convertRawValue(orderingProperty, session.getLastProcessedValue());

        FluentLoader.ByCondition<Object> loader = lastProcessedValue == null
                ? dataManager.load(metaClass.getJavaClass()).all()
                : dataManager.load(metaClass.getJavaClass())
                .condition(PropertyCondition.greater(orderingPropertyName, lastProcessedValue));
        List<Object> entities = loader
                .fetchPlanProperties(orderingPropertyName)
                .sort(Sort.by(orderingPropertyName))
                .maxResults(batchSize)
                .list();

        List<?> ids = entities.stream().map(EntityValues::getId).toList();
        Object lastLoadedValue = entities.isEmpty()
                ? null
                : EntityValues.getValue(entities.get(entities.size() - 1), orderingPropertyName);
        return new ResultHolder(ids, lastLoadedValue);
    }

    @Override
    protected List<KeyValueEntity> loadValues(ValueLoadContext valueLoadContext) {
        throw new UnsupportedOperationException("Value loading is not used for non-JPA entities");
    }
}
