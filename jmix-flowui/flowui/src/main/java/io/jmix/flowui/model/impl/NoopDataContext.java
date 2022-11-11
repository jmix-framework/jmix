/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.model.impl;

import io.jmix.core.EntitySet;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.event.sys.VoidSubscription;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.MergeOptions;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Dummy implementation of {@link DataContext} used for read-only views like entity list views.
 */
public class NoopDataContext implements DataContext {

    protected ApplicationContext applicationContext;

    public NoopDataContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Nullable
    @Override
    public <T> T find(Class<T> entityClass, Object entityId) {
        return null;
    }

    @Override
    public <T> T find(T entity) {
        return null;
    }

    @Override
    public boolean contains(Object entity) {
        return false;
    }

    @Override
    public <T> T merge(T entity, MergeOptions options) {
        return entity;
    }

    @Override
    public <T> T merge(T entity) {
        return entity;
    }

    @Override
    public EntitySet merge(Collection entities, MergeOptions options) {
        return EntitySet.of(entities);
    }

    @Override
    public EntitySet merge(Collection entities) {
        return EntitySet.of(entities);
    }

    @Override
    public void remove(Object entity) {
    }

    @Override
    public void evict(Object entity) {
    }

    @Override
    public void evictModified() {
    }

    @Override
    public void clear() {
    }

    @Override
    public <T> T create(Class<T> entityClass) {
        return applicationContext.getBean(Metadata.class).create(entityClass);
    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public boolean isModified(Object entity) {
        return false;
    }

    @Override
    public void setModified(Object entity, boolean modified) {
    }

    @Override
    public Set getModified() {
        return Collections.emptySet();
    }

    @Override
    public boolean isRemoved(Object entity) {
        return false;
    }

    @Override
    public Set getRemoved() {
        return Collections.emptySet();
    }

    @Override
    public EntitySet save() {
        return EntitySet.of(Collections.emptySet());
    }

    @Override
    public DataContext getParent() {
        return null;
    }

    @Override
    public void setParent(DataContext parentContext) {
    }

    @Override
    public Subscription addChangeListener(Consumer<ChangeEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addPreSaveListener(Consumer<PreSaveEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addPostSaveListener(Consumer<PostSaveEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Function<SaveContext, Set<Object>> getSaveDelegate() {
        return null;
    }

    @Override
    public void setSaveDelegate(Function<SaveContext, Set<Object>> delegate) {
    }
}
