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

package io.jmix.core;

import io.jmix.core.entity.BaseGenericIdEntity;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;

public abstract class DataManagerSupport implements DataManager {

    @Inject
    protected FetchPlanRepository viewRepository;

    @Inject
    protected Metadata metadata;

    @Inject
    protected EntityStates entityStates;

    @Override
    public <E extends Entity> E reload(E entity, String fetchPlanName) {
        Objects.requireNonNull(fetchPlanName, "viewName is null");
        return reload(entity, viewRepository.getFetchPlan(entity.getClass(), fetchPlanName));
    }

    @Override
    public <E extends Entity> E reload(E entity, FetchPlan fetchPlan) {
        return reload(entity, fetchPlan, null);
    }

    @Override
    public <E extends Entity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass) {
        return reload(entity, fetchPlan, metaClass, entityHasDynamicAttributes(entity));
    }

    @Override
    public <E extends Entity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass, boolean loadDynamicAttributes) {
        if (metaClass == null) {
            metaClass = metadata.getSession().findClass(entity.getClass());
        }
        LoadContext<E> context = new LoadContext<>(metaClass);
        context.setId(entity.getId());
        context.setView(fetchPlan);
        context.setLoadDynamicAttributes(loadDynamicAttributes);

        E reloaded = load(context);
        if (reloaded == null)
            throw new EntityAccessException(metaClass, entity.getId());

        return reloaded;
    }

    protected boolean entityHasDynamicAttributes(Entity entity) {
        return false;

        // todo dynamic attributes
//        return entity instanceof BaseGenericIdEntity
//                && ((BaseGenericIdEntity) entity).getDynamicAttributes() != null;
    }

    @Override
    public EntitySet commit(Entity... entities) {
        return commit(new CommitContext(entities));
    }

    @Override
    public <E extends Entity> E commit(E entity, @Nullable FetchPlan fetchPlan) {
        return commit(new CommitContext().addInstanceToCommit(entity, fetchPlan)).get(entity);
    }

    @Override
    public <E extends Entity> E commit(E entity, @Nullable String fetchPlanName) {
        if (fetchPlanName != null) {
            FetchPlan view = viewRepository.getFetchPlan(metadata.getClass(entity.getClass()), fetchPlanName);
            return commit(entity, view);
        } else {
            return commit(entity, (FetchPlan) null);
        }
    }

    @Override
    public <E extends Entity> E commit(E entity) {
        return commit(entity, (FetchPlan) null);
    }

    @Override
    public void remove(Entity entity) {
        CommitContext context = new CommitContext(
                Collections.<Entity>emptyList(),
                Collections.singleton(entity));
        commit(context);
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T extends BaseGenericIdEntity<K>, K> T getReference(Class<T> entityClass, K id) {
        T entity = metadata.create(entityClass);
        entity.setId(id);
        entityStates.makePatch(entity);
        return entity;
    }
}
