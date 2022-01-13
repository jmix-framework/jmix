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

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.index.queue.EntityIdsLoader;
import io.jmix.search.index.queue.entity.EnqueueingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Loads data using ordering property.
 */
public abstract class OrderBasedEntityIdsLoader implements EntityIdsLoader {

    private static final Logger log = LoggerFactory.getLogger(OracleEntityIdsLoader.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Override
    public ResultHolder loadNextIds(EnqueueingSession session, int batchSize) {
        String entityName = session.getEntityName();
        MetaClass entityClass = metadata.getClass(entityName);
        String orderingPropertyName = session.getOrderingProperty();
        MetaProperty orderingProperty = entityClass.getProperty(orderingPropertyName);
        String lastProcessedRawOrderingValue = session.getLastProcessedValue();
        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(entityClass);
        if (primaryKeyProperty == null) {
            throw new RuntimeException(String.format("Entity '%s' doesn't have primary key", entityName));
        }
        String primaryKeyPropertyName = primaryKeyProperty.getName();

        ResultHolder result;
        if (metadataTools.isEmbedded(orderingProperty)) {
            log.warn("Sorted loading by embedded property is not supported - perform in-memory loading of all ids");
            result = loadAllInMemory(entityClass);
        } else {
            Object lastProcessedValue = convertRawValue(orderingProperty, lastProcessedRawOrderingValue);
            ValueLoadContext valueLoadContext = createValueLoadContext(entityClass, primaryKeyPropertyName, orderingPropertyName, lastProcessedValue, batchSize);
            List<KeyValueEntity> loadedValues = loadValues(valueLoadContext);
            List<Object> ids = loadedValues.stream().map(v -> v.getValue("objectId")).collect(Collectors.toList());
            Object lastLoadedOrderingValue = resolveLastLoadedOrderingValue(loadedValues, primaryKeyPropertyName, orderingPropertyName);
            result = new ResultHolder(ids, lastLoadedOrderingValue);
        }

        return result;
    }

    protected abstract List<KeyValueEntity> loadValues(ValueLoadContext valueLoadContext);

    protected ValueLoadContext createValueLoadContext(MetaClass entityClass, String pkProperty, String orderingProperty, @Nullable Object orderingValue, int batchSize) {
        String entityName = entityClass.getName();
        String storeName = entityClass.getStore().getName();
        String queryString;
        boolean orderingByPk = pkProperty.equals(orderingProperty);
        boolean initial = orderingValue == null;
        List<String> resultProperties = new ArrayList<>();
        resultProperties.add("objectId");
        String selection;
        if (orderingByPk) {
            selection = "e." + orderingProperty;
        } else {
            selection = String.format("e.%s, e.%s", pkProperty, orderingProperty);
            resultProperties.add("orderingValue");
        }

        String condition = initial
                ? ""
                : "where e." + orderingProperty + " > :value";

        queryString = String.format("select %s from %s e %s order by e.%s", selection, entityName, condition, orderingProperty);

        ValueLoadContext.Query query = ValueLoadContext.createQuery(queryString).setMaxResults(batchSize);
        if (!initial) {
            query.setParameter("value", orderingValue);
        }

        return ValueLoadContext.create()
                .setStoreName(storeName)
                .setQuery(query)
                .setProperties(resultProperties);
    }

    @Nullable
    protected Object resolveLastLoadedOrderingValue(List<KeyValueEntity> loadedValues, String primaryKeyProperty, String orderingProperty) {
        if(loadedValues.isEmpty()) {
            return null;
        }
        String effectiveProperty = primaryKeyProperty.equals(orderingProperty) ? "objectId" : "orderingValue";
        return loadedValues.get(loadedValues.size() - 1).getValue(effectiveProperty);
    }

    protected ResultHolder loadAllInMemory(MetaClass entityClass) {
        String entityName = entityClass.getName();
        String primaryKeyName = metadataTools.getPrimaryKeyName(entityClass);
        log.debug("Primary key of entity '{}': '{}'", entityName, primaryKeyName);
        if (primaryKeyName == null) {
            throw new IllegalArgumentException("Primary key is null");
        }

        List<?> rawIds;
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(entityClass.getStore().getName());
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        rawIds = transactionTemplate.execute(status -> {
            EntityManager em = storeAwareLocator.getEntityManager(entityClass.getStore().getName());
            Query query = em.createQuery(format("select e.%s from %s e", primaryKeyName, entityName));
            return query.getResultList();
        });
        if (rawIds == null) {
            rawIds = Collections.emptyList();
        }
        return new ResultHolder(rawIds, null);
    }

    @Nullable
    protected Object convertRawValue(MetaProperty orderingProperty, @Nullable String rawValue) {
        if (rawValue == null) {
            return null;
        }

        Class<?> javaType = orderingProperty.getJavaType();
        if (String.class.isAssignableFrom(javaType)) {
            return rawValue;
        }
        if (UUID.class.isAssignableFrom(javaType)) {
            return UUID.fromString(rawValue);
        }
        if (Long.class.isAssignableFrom(javaType)) {
            return Long.valueOf(rawValue);
        }
        if (Integer.class.isAssignableFrom(javaType)) {
            return Integer.valueOf(rawValue);
        }
        throw new IllegalArgumentException("Unsupported property: " + orderingProperty);
    }
}
