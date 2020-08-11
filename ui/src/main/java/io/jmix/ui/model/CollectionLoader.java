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

package io.jmix.ui.model;

import io.jmix.core.LoadContext;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.JmixEntity;
import io.jmix.core.FetchPlan;
import io.jmix.ui.screen.InstallSubject;
import io.jmix.ui.screen.Subscribe;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Loader of entity collections.
 */
@InstallSubject("loadDelegate")
public interface CollectionLoader<E extends JmixEntity> extends BaseCollectionLoader {

    /**
     * Returns the container which accepts loaded entities.
     */
    CollectionContainer<E> getContainer();

    /**
     * Sets the container which accepts loaded entities.
     */
    void setContainer(CollectionContainer<E> container);

    /**
     * Returns {@code LoadContext} which is created by the parameters of this loader. The {@code LoadContext}
     * can be used with {@code DataManager} to load data by the same conditions.
     */
    LoadContext<E> createLoadContext();

    /**
     * Sets custom hint that should be used by the query for loading data.
     */
    void setHint(String hintName, Object value);

    /**
     * @return custom hints which are used by the query for loading data.
     */
    @Nullable
    Map<String, Object> getHints();

    /**
     * Returns true if the query for loading data is cacheable.
     */
    boolean isCacheable();

    /**
     * Sets the query for loading data cacheable.
     */
    void setCacheable(boolean cacheable);

    /**
     * @deprecated replaced by {@link CollectionLoader#getFetchPlan()}
     */
    @Deprecated
    default FetchPlan getView() {
        return getFetchPlan();
    }

    /**
     * Returns the fetch plan which is used when loading.
     */
    FetchPlan getFetchPlan();

    /**
     * @deprecated replaced by {@link CollectionLoader#setFetchPlan(FetchPlan)}
     */
    @Deprecated
    default void setView(FetchPlan view) {
        setFetchPlan(view);
    }

    /**
     * Sets the fetch plan which is used when loading.
     */
    void setFetchPlan(FetchPlan fetchPlan);

    /**
     * @deprecated replaced by {@link CollectionLoader#(FetchPlan)}
     */
    @Deprecated
    default void setView(String viewName) {
        setFetchPlan(viewName);
    }


    /**
     * Sets the name of the fetch plan which is used when loading.
     * @throws IllegalStateException if the fetch plan has already been set by {@link #setFetchPlan(FetchPlan)}
     */
    void setFetchPlan(String fetchPlanName);

    /**
     * Returns a function which will be used to load data instead of standard implementation.
     */
    Function<LoadContext<E>, List<E>> getLoadDelegate();

    /**
     * Sets a function which will be used to load data instead of standard implementation.
     */
    void setLoadDelegate(Function<LoadContext<E>, List<E>> delegate);

    /**
     * Event sent before loading entities.
     * <p>
     * You can prevent load using the {@link #preventLoad()} method of the event, for example:
     * <pre>
     *     &#64;Subscribe(id = "fooDl", target = Target.DATA_LOADER)
     *     private void onFooDlPreLoad(CollectionLoader.PreLoadEvent event) {
     *         if (doNotLoad()) {
     *             event.preventLoad();
     *         }
     *     }
     * </pre>
     *
     * @see #addPreLoadListener(Consumer)
     */
    class PreLoadEvent<T extends JmixEntity> extends EventObject {

        private final LoadContext<T> loadContext;
        private boolean loadPrevented;

        public PreLoadEvent(CollectionLoader<T> loader, LoadContext<T> loadContext) {
            super(loader);
            this.loadContext = loadContext;
        }

        /**
         * The data loader which sent the event.
         */
        @SuppressWarnings("unchecked")
        @Override
        public CollectionLoader<T> getSource() {
            return (CollectionLoader<T>) super.getSource();
        }

        /**
         * Returns the load context of the current data loader.
         */
        public LoadContext<T> getLoadContext() {
            return loadContext;
        }

        /**
         * Invoke this method if you want to abort the loading.
         */
        public void preventLoad() {
            loadPrevented = true;
        }

        /**
         * Returns true if {@link #preventLoad()} method was called and loading will be aborted.
         */
        public boolean isLoadPrevented() {
            return loadPrevented;
        }
    }

    /**
     * Adds a listener to {@link PreLoadEvent}.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe(id = "fooDl", target = Target.DATA_LOADER)
     *     private void onFooDlPreLoad(CollectionLoader.PreLoadEvent event) {
     *         // handle event here
     *     }
     * </pre>
     *
     * @param listener listener
     * @return subscription
     */
    Subscription addPreLoadListener(Consumer<PreLoadEvent<E>> listener);

    /**
     * Event sent after successful loading of entities, merging them into {@code DataContext} and setting to
     * the container.
     *
     * @see #addPostLoadListener(Consumer)
     */
    class PostLoadEvent<T extends JmixEntity> extends EventObject {

        private final List<T> loadedEntities;

        public PostLoadEvent(CollectionLoader<T> loader, List<T> loadedEntities) {
            super(loader);
            this.loadedEntities = loadedEntities;
        }

        /**
         * The data loader which sent the event.
         */
        @SuppressWarnings("unchecked")
        @Override
        public CollectionLoader<T> getSource() {
            return (CollectionLoader<T>) super.getSource();
        }

        /**
         * Returns the list of loaded entities.
         */
        public List<T> getLoadedEntities() {
            return loadedEntities;
        }
    }

    /**
     * Adds a listener to {@link PostLoadEvent}.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe(id = "fooDl", target = Target.DATA_LOADER)
     *     private void onFooDlPostLoad(CollectionLoader.PostLoadEvent event) {
     *         // handle event here
     *     }
     * </pre>
     *
     * @param listener listener
     * @return subscription
     */
    Subscription addPostLoadListener(Consumer<PostLoadEvent<E>> listener);
}
