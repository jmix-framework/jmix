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

package io.jmix.dataimport.impl;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.dataimport.DuplicateEntityManager;
import io.jmix.dataimport.configuration.UniqueEntityConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

@Component("datimp_DuplicateEntityManager")
public class DuplicateEntityManagerImpl implements DuplicateEntityManager {
    @Autowired
    protected DataManager dataManager;

    @Override
    public Object load(Object entity, UniqueEntityConfiguration configuration, FetchPlan fetchPlan) {
        LogicalCondition condition = LogicalCondition.and();
        configuration.getEntityPropertyNames().forEach(propertyName -> {
            Object propertyValue = EntityValues.getValueEx(entity, propertyName);
            if (propertyValue != null) {
                condition.add(PropertyCondition.equal(propertyName, propertyValue));
            } else {
                condition.add(PropertyCondition.isSet(propertyName, false));
            }
        });
        return loadByCondition(entity.getClass(), fetchPlan, condition);
    }

    @Override
    public boolean isDuplicated(Object firstEntity, Object secondEntity, UniqueEntityConfiguration configuration) {
        return !findNotEqualValue(firstEntity, secondEntity, configuration);
    }

    protected boolean findNotEqualValue(Object firstEntity, Object secondEntity, UniqueEntityConfiguration uniqueEntityConfiguration) {
        return uniqueEntityConfiguration.getEntityPropertyNames()
                .stream()
                .anyMatch(entityPropertyName -> {
                    Object firstValue = EntityValues.getValueEx(firstEntity, entityPropertyName);
                    Object secondValue = EntityValues.getValueEx(secondEntity, entityPropertyName);
                    return !EntityValues.propertyValueEquals(firstValue, secondValue);
                });
    }

    @Nullable
    protected Object loadByCondition(Class entityClass, @Nullable FetchPlan fetchPlan, LogicalCondition condition) {
        if (CollectionUtils.isNotEmpty(condition.getConditions())) {
            return dataManager.load(entityClass)
                    .condition(condition)
                    .fetchPlan(fetchPlan)
                    .optional()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Object find(Collection<Object> existingEntities, Map<String, Object> propertyValues) {
        if (!propertyValues.isEmpty()) {
            return existingEntities.stream()
                    .filter(entity -> !findNotEqualValue(propertyValues, entity))
                    .findFirst().orElse(null);
        }
        return null;
    }

    protected boolean findNotEqualValue(Map<String, Object> propertyValues, Object entity) {
        return propertyValues.entrySet().stream().anyMatch(entry -> {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            Object propertyValueInEntity = EntityValues.getValueEx(entity, propertyName);
            return !EntityValues.propertyValueEquals(propertyValue, propertyValueInEntity);
        });
    }

    @Override
    public Object load(Class entityClass, Map<String, Object> propertyValues, @Nullable FetchPlan fetchPlan) {
        LogicalCondition condition = LogicalCondition.and();
        propertyValues.forEach((propertyName, propertyValue) -> {
            if (propertyValue != null) {
                condition.add(PropertyCondition.equal(propertyName, propertyValue));
            } else {
                condition.add(PropertyCondition.isSet(propertyName, false));
            }
        });
        return loadByCondition(entityClass, fetchPlan, condition);
    }
}
