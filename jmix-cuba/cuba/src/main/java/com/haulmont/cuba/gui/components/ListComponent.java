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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.ui.component.Table;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.ListComponent} instead
 */
@Deprecated
public interface ListComponent<E extends Entity> extends io.jmix.ui.component.ListComponent<E> {

    /**
     * @return collection datasource
     * @deprecated Use {@link #getItems()} instead
     */
    @Deprecated
    CollectionDatasource getDatasource();

    /**
     * Provides icons for particular rows in the table.
     *
     * @param <E> entity class
     * @deprecated Use {@link Function} instead
     */
    @Deprecated
    interface IconProvider<E> extends Function<E, String> {
        @Override
        default String apply(E entity) {
            return getItemIcon(entity);
        }

        /**
         * Called by {@link Table} to get an icon to be shown for a row.
         *
         * @param entity an entity instance represented by the current row
         * @return icon name or null to show no icon
         */
        @Nullable
        String getItemIcon(E entity);
    }
}
