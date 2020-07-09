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
package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import io.jmix.core.*;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.core.entity.contracts.Id;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GenericDataSupplier implements DataSupplier {

    @SuppressWarnings("unchecked")
    @Override
    public <E extends JmixEntity> E newInstance(MetaClass metaClass) {
        return (E) getMetadata().create(metaClass);
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, String fetchPlanName) {
        return getDataManager().reload(entity, fetchPlanName);
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, FetchPlan fetchPlan) {
        return getDataManager().reload(entity, fetchPlan);
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass) {
        return getDataManager().reload(entity, fetchPlan, metaClass);
    }

    @Override
    public <E extends JmixEntity> E reload(E entity, FetchPlan fetchPlan, @Nullable MetaClass metaClass, boolean loadDynamicAttributes) {
        return getDataManager().reload(entity, fetchPlan, metaClass, loadDynamicAttributes);
    }

    @Override
    public <E extends JmixEntity> E commit(E instance, @Nullable FetchPlan fetchPlan) {
        return getDataManager().commit(instance, fetchPlan);
    }

    @Override
    public <E extends JmixEntity> E commit(E entity, @Nullable String fetchPlanName) {
        return getDataManager().commit(entity, fetchPlanName);
    }

    @Override
    public <E extends JmixEntity> E commit(E instance) {
        return getDataManager().commit(instance);
    }

    @Override
    public EntitySet commit(JmixEntity... entities) {
        return getDataManager().commit(entities);
    }

    @Override
    public void remove(JmixEntity entity) {
        getDataManager().remove(entity);
    }

    @Override
    public <T extends JmixEntity, K> void remove(Id<T, K> entityId) {
        getDataManager().remove(entityId);
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return getDataManager().loadValues(context);
    }

    @Override
    public DataManager secure() {
        return getDataManager();
    }

    @Override
    public io.jmix.core.DataManager getDelegate() {
        return getDataManager().getDelegate();
    }

    @Override
    public <E extends JmixEntity> FluentLoader<E> load(Class<E> entityClass) {
        return getDataManager().load(entityClass);
    }

    @Override
    public <E extends JmixEntity, K> FluentLoader.ById<E> load(Id<E, K> entityId) {
        return getDataManager().load(entityId);
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        return getDataManager().loadValues(queryString);
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return getDataManager().loadValue(queryString, valueClass);
    }

    @Override
    public <T extends JmixEntity> T create(Class<T> entityClass) {
        return getDataManager().create(entityClass);
    }

    @Override
    public <T extends JmixEntity, K> T getReference(Class<T> entityClass, K id) {
        return getDataManager().getReference(entityClass, id);
    }

    @Override
    public <T extends JmixEntity, K> T getReference(Id<T, K> entityId) {
        return null;
    }

    @Override
    public EntitySet commit(CommitContext context) {
        return getDataManager().commit(context);
    }

    @Override
    @Nullable
    public <E extends JmixEntity> E load(LoadContext<E> context) {
        return getDataManager().load(context);
    }

    @Override
    @Nonnull
    public <E extends JmixEntity> List<E> loadList(LoadContext<E> context) {
        return getDataManager().loadList(context);
    }

    @Override
    public long getCount(LoadContext<? extends JmixEntity> context) {
        return getDataManager().getCount(context);
    }

    protected Metadata getMetadata() {
        return AppBeans.get(Metadata.NAME);
    }

    protected DataManager getDataManager() {
        return AppBeans.get(DataManager.NAME);
    }
}
