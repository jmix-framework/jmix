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

package io.jmix.core;

import io.jmix.core.annotation.Experimental;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Dispatches entity save and remove operations to custom update services.
 *
 * @see SaveDelegate
 * @see RemoveDelegate
 */
@Experimental
@Component("core_EntityUpdateDispatcher")
public class EntityUpdateDispatcher {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Metadata metadata;

    @SuppressWarnings("unchecked")
    @Nullable
    public SaveDelegate<Object> getSaveService(Class<?> entityClass) {
        try {
            ResolvableType type = ResolvableType.forClassWithGenerics(SaveDelegate.class, entityClass);
            return (SaveDelegate<Object>) applicationContext.getBeanProvider(type).getIfAvailable();
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public RemoveDelegate<Object> getRemoveService(Class<?> entityClass) {
        try {
            ResolvableType type = ResolvableType.forClassWithGenerics(RemoveDelegate.class, entityClass);
            return (RemoveDelegate<Object>) applicationContext.getBeanProvider(type).getIfAvailable();
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Transactional
    public EntitySet save(UnconstrainedDataManager dataManager, SaveContext saveContext) {
        SaveContext delegatedContext = new SaveContext();
        Set<Object> result = new HashSet<>();

        // Process entities to save
        for (Object entity : saveContext.getEntitiesToSave()) {
            SaveDelegate<Object> service = getSaveService(entity.getClass());
            if (service != null) {
                Object saved = service.save(entity, saveContext);
                result.add(saved);
            } else {
                delegatedContext.saving(entity, saveContext.getFetchPlans().get(entity));
            }
        }

        // Process entities to remove
        for (Object entity : saveContext.getEntitiesToRemove()) {
            RemoveDelegate<Object> service = getRemoveService(entity.getClass());
            if (service != null) {
                service.remove(entity);
            } else {
                delegatedContext.removing(entity);
            }
        }

        excludeCompositionItemsFromContext(delegatedContext, result);
        
        // Save remaining entities through DataManager
        if (!delegatedContext.getEntitiesToSave().isEmpty()
                || !delegatedContext.getEntitiesToRemove().isEmpty()) {
            delegatedContext.setHints(saveContext.getHints());
            delegatedContext.setJoinTransaction(saveContext.isJoinTransaction());
            delegatedContext.setAccessConstraints(saveContext.getAccessConstraints());
            delegatedContext.setDiscardSaved(saveContext.isDiscardSaved());
            Set<Object> dataManagerResult = dataManager.save(delegatedContext);
            result.addAll(dataManagerResult);
        }

        return EntitySet.of(result);
    }

    @Transactional
    public void remove(UnconstrainedDataManager dataManager, Collection<?> entities) {
        // Group entities by class
        Map<Class<?>, Collection<Object>> entitiesByClass = new HashMap<>();
        for (Object entity : entities) {
            entitiesByClass.computeIfAbsent(entity.getClass(), k -> new HashSet<>()).add(entity);
        }

        SaveContext delegatedContext = new SaveContext();

        // Process each class group
        for (Map.Entry<Class<?>, Collection<Object>> entry : entitiesByClass.entrySet()) {
            RemoveDelegate<Object> service = getRemoveService(entry.getKey());
            if (service != null) {
                for (Object entity : entry.getValue()) {
                    service.remove(entity);
                }
            } else {
                for (Object entity : entry.getValue()) {
                    delegatedContext.removing(entity);
                }
            }
        }

        // Remove remaining entities through DataManager
        if (!delegatedContext.getEntitiesToRemove().isEmpty()) {
            dataManager.save(delegatedContext);
        }
    }

    private void excludeCompositionItemsFromContext(SaveContext delegatedContext, Set<Object> result) {
        if (result.isEmpty()) {
            return;
        }

        Set<Object> entitiesToExcludeFromSaving = new HashSet<>();
        Set<Object> entitiesToExcludeFromRemoving = new HashSet<>();

        for (Object savedEntity : result) {
            MetaClass metaClass = metadata.getClass(savedEntity);

            for (MetaProperty metaProperty : metaClass.getProperties()) {
                if (metaProperty.getRange().isClass()
                        && metaProperty.getRange().getCardinality().isMany()
                        && metaProperty.getType() == MetaProperty.Type.COMPOSITION) {

                    Object propertyValue = EntityValues.getValue(savedEntity, metaProperty.getName());

                    if (propertyValue instanceof Collection<?> collection) {
                        entitiesToExcludeFromSaving.addAll(collection);
                        entitiesToExcludeFromRemoving.addAll(collection);
                    }
                }
            }
        }
    
        // Remove composition items from delegatedContext
        delegatedContext.getEntitiesToSave().removeAll(entitiesToExcludeFromSaving);
        delegatedContext.getEntitiesToRemove().removeAll(entitiesToExcludeFromRemoving);
    }
}

