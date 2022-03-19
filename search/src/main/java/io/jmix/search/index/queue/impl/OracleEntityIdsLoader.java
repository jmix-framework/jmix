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

import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle-specific implementation of {@link OrderBasedEntityIdsLoader}.
 * Ensures that ordering and comparison works the same way.
 */
@Component("search_OracleEntityIdsLoader")
public class OracleEntityIdsLoader extends OrderBasedEntityIdsLoader {

    protected List<KeyValueEntity> loadValues(ValueLoadContext valueLoadContext) {
        String storeName = valueLoadContext.getStoreName();
        valueLoadContext.setJoinTransaction(true);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(storeName);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        List<KeyValueEntity> loadedValues = transactionTemplate.execute(status -> {
            EntityManager entityManager = storeAwareLocator.getEntityManager(storeName);
            Query selectParametersQuery = entityManager.createNativeQuery("SELECT PARAMETER, value FROM nls_session_parameters WHERE parameter IN ('NLS_COMP', 'NLS_SORT')");
            List<?> resultList = selectParametersQuery.getResultList();
            Map<String, String> originalValues = new HashMap<>();
            resultList.forEach(item -> {
                Object[] row = (Object[]) item;
                String name = (String) row[0];
                String value = (String) row[1];
                if (!"BINARY".equalsIgnoreCase(value)) {
                    originalValues.put(name, value);
                }
            });

            originalValues.keySet().forEach(name -> {
                Query alterSessionQuery = entityManager.createNativeQuery("ALTER SESSION SET " + name + " = BINARY");
                alterSessionQuery.executeUpdate();
            });

            List<KeyValueEntity> result = dataManager.loadValues(valueLoadContext);

            originalValues.forEach((name, value) -> {
                Query alterSessionQuery = entityManager.createNativeQuery("ALTER SESSION SET " + name + " = " + value);
                alterSessionQuery.executeUpdate();
            });

            return result;
        });
        return loadedValues == null ? Collections.emptyList() : loadedValues;
    }
}
