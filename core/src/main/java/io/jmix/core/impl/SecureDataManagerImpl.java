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

package io.jmix.core.impl;

import io.jmix.core.*;
import io.jmix.core.annotation.Secure;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Secure
@Component(DataManager.SECURE_NAME)
public class SecureDataManagerImpl implements DataManager {
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected AccessConstraintsRegistry accessConstraintsRegistry;

    @Nullable
    @Override
    public <E> E load(LoadContext<E> context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return dataManager.load(context);
    }

    @Override
    public <E> List<E> loadList(LoadContext<E> context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return dataManager.loadList(context);
    }

    @Override
    public long getCount(LoadContext<?> context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return dataManager.getCount(context);
    }

    @Override
    public EntitySet save(SaveContext context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return dataManager.save(context);
    }

    @Override
    public EntitySet save(Object... entities) {
        return dataManager.save(new SaveContext()
                .setAccessConstraints(mergeConstraints(Collections.emptyList()))
                .saving(entities));
    }

    @Override
    public <E> E save(E entity) {
        return dataManager.save(new SaveContext()
                .setAccessConstraints(mergeConstraints(Collections.emptyList()))
                .saving(entity))
                .optional(entity)
                .orElseThrow(() -> new IllegalStateException("Data store didn't return a saved entity"));
    }

    @Override
    public void remove(Object... entity) {
        dataManager.save(new SaveContext()
                .setAccessConstraints(mergeConstraints(Collections.emptyList()))
                .removing(entity));
    }

    @Override
    public <E> void remove(Id<E> entityId) {
        dataManager.save(new SaveContext()
                .setAccessConstraints(mergeConstraints(Collections.emptyList()))
                .removing(dataManager.getReference(entityId)));
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return dataManager.loadValues(context);
    }

    @Override
    public <E> FluentLoader<E> load(Class<E> entityClass) {
        FluentLoader<E> loader = dataManager.load(entityClass);
        loader.setDataManager(this);
        return loader;
    }

    @Override
    public <E> FluentLoader.ById<E> load(Id<E> entityId) {
        return load(entityId.getEntityClass())
                .id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        FluentValuesLoader loader = dataManager.loadValues(queryString);
        loader.setDataManager(this);
        return loader;
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        FluentValueLoader<T> loader = dataManager.loadValue(queryString, valueClass);
        loader.setDataManager(this);
        return loader;
    }

    @Override
    public <T> T create(Class<T> entityClass) {
        return dataManager.create(entityClass);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object id) {
        return dataManager.getReference(entityClass, id);
    }

    @Override
    public <T> T getReference(Id<T> entityId) {
        return dataManager.getReference(entityId);
    }

    protected List<AccessConstraint<?>> mergeConstraints(List<AccessConstraint<?>> accessConstraints) {
        if (accessConstraints.isEmpty()) {
            return accessConstraintsRegistry.getConstraints();
        } else {
            List<AccessConstraint<?>> newAccessConstraints = new ArrayList<>(accessConstraintsRegistry.getConstraints());
            newAccessConstraints.addAll(accessConstraints);
            return newAccessConstraints;
        }
    }
}
