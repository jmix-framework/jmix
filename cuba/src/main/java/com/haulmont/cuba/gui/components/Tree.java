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

package com.haulmont.cuba.gui.components;

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.components.data.tree.DatasourceTreeItems;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import io.jmix.core.Entity;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.TreeItems;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.Tree} instead
 */
@SuppressWarnings("rawtypes")
@Deprecated
public interface Tree<E extends Entity> extends ListComponent<E>, io.jmix.ui.component.Tree<E>, HasItemCaptionMode,
        LookupComponent<E> {

    static <T extends Entity> TypeToken<Tree<T>> of(Class<T> itemClass) {
        return new TypeToken<Tree<T>>() {
        };
    }

    /**
     * Sets an instance of {@code HierarchicalDatasource} as Tree data source.
     *
     * @param datasource hierarchical datasource
     * @deprecated Use {@link #setItems(TreeItems)} instead
     */
    @Deprecated
    default void setDatasource(HierarchicalDatasource datasource) {
        //noinspection unchecked
        setItems(datasource != null
                ? new DatasourceTreeItems(datasource)
                : null);
    }

    /**
     * @return hierarchical datasource
     * @deprecated Use {@link #getItems()} instead
     */
    @Deprecated
    default HierarchicalDatasource getDatasource() {
        TreeItems<E> treeItems = getItems();
        return treeItems != null
                ? ((DatasourceTreeItems) treeItems).getDatasource()
                : null;
    }

    /**
     * Sets whether multiple selection mode is enabled.
     *
     * @param multiselect {@code true} for multiselect, {@code false} otherwise
     * @deprecated Use {@link #setSelectionMode(SelectionMode)} instead
     */
    @Deprecated
    void setMultiSelect(boolean multiselect);

    /**
     * @deprecated refresh datasource instead
     */
    @Deprecated
    void refresh();

    /**
     * Provides style names for tree items.
     */
    @Deprecated
    interface StyleProvider<E> extends Function<E, String> {
        @Override
        default String apply(E entity) {
            return getStyleName(entity);
        }

        /**
         * Called by {@link io.jmix.ui.component.Tree} to get a style for item. <br>
         * All unhandled exceptions from StyleProvider in Web components by default are logged with ERROR level
         * and not shown to users.
         *
         * @param entity an entity instance represented by the current item
         * @return style name or null to apply the default
         */
        String getStyleName(E entity);
    }

    /**
     * Sets a new details generator for item details.
     * <p>
     * The currently opened item details will be re-rendered.
     *
     * @param generator the details generator to set
     * @deprecated Use {@link #setDetailsGenerator(Function)} instead
     */
    @Deprecated
    void setDetailsGenerator(@Nullable DetailsGenerator<? super E> generator);

    /**
     * A callback interface for generating details for a particular item in Tree.
     *
     * @param <E> Tree data type
     * @deprecated Use {@link #setDetailsGenerator(Function)} instead
     */
    @FunctionalInterface
    interface DetailsGenerator<E> extends Function<E, Component> {
    }
}
