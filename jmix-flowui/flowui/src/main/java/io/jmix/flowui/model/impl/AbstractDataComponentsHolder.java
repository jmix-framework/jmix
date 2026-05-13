/*
 * Copyright 2024 Haulmont.
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

import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.monitoring.DataLoaderMonitoringInfo;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract implementation of the {@link HasDataComponents} interface providing a base for managing
 * data-related components such as {@link DataContext}, {@link InstanceContainer}, and {@link DataLoader}.
 */
public abstract class AbstractDataComponentsHolder implements HasDataComponents {

    protected DataContext dataContext;

    protected Map<String, InstanceContainer<?>> containers;
    protected Map<String, DataLoader> loaders;

    @Override
    public DataContext getDataContext() {
        if (dataContext == null) {
            throw new IllegalStateException(DataContext.class.getSimpleName() + " is not defined");
        }

        return dataContext;
    }

    @Nullable
    @Override
    public DataContext getDataContextOrNull() {
        return dataContext;
    }

    @Override
    public void setDataContext(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends InstanceContainer<?>> T getContainer(String id) {
        T container = (T) getContainers().get(id);
        if (container == null) {
            throw new IllegalArgumentException(String.format("Container '%s' not found", id));
        }
        return container;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataLoader> T getLoader(String id) {
        T loader = (T) getLoaders().get(id);
        if (loader == null) {
            throw new IllegalArgumentException(String.format("Loader '%s' not found", id));
        }
        return loader;
    }

    @Override
    public Set<String> getContainerIds() {
        return getContainers().keySet();
    }

    @Override
    public Set<String> getLoaderIds() {
        return getLoaders().keySet();
    }

    @Override
    public void registerContainer(String id, InstanceContainer<?> container) {
        getContainers().put(id, container);
    }

    @Override
    public void registerLoader(String id, DataLoader loader) {
        getLoaders().put(id, loader);

        // Lazy provider: viewId and fragmentId are resolved on each monitoring event, not at
        // registration time. This is required for fragment-owned loaders — at the moment of
        // registration the fragment is not yet attached to its host, so the enclosing view is
        // not yet reachable via the parent chain.
        loader.setMonitoringInfoProvider(dl ->
                new DataLoaderMonitoringInfo(resolveViewId(), id, resolveFragmentId()));
    }

    @Nullable
    protected abstract String getOwnerId();

    /**
     * @return id of the enclosing view, or {@code null} if not resolvable. Default delegates to
     * {@link #getOwnerId()} so that subclasses overriding {@code getOwnerId()} keep populating
     * {@code view.id} with no migration. Override this in new code when the enclosing view id
     * cannot be derived from {@code getOwnerId()}.
     */
    @Nullable
    protected String resolveViewId() {
        return getOwnerId();
    }

    /**
     * @return id of the enclosing fragment when the loader is owned by a fragment, {@code null}
     * otherwise. Default is {@code null}; override when the holder is fragment-scoped.
     */
    @Nullable
    protected String resolveFragmentId() {
        return null;
    }

    @Override
    public void loadAll() {
        for (DataLoader loader : getLoaders().values()) {
            loader.load();
        }
    }

    protected Map<String, InstanceContainer<?>> getContainers() {
        if (containers == null) {
            containers = new LinkedHashMap<>();
        }

        return containers;
    }

    protected Map<String, DataLoader> getLoaders() {
        if (loaders == null) {
            loaders = new LinkedHashMap<>();
        }

        return loaders;
    }
}
