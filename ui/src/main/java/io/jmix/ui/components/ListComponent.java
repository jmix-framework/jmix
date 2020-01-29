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
package io.jmix.ui.components;

import io.jmix.core.entity.Entity;
import io.jmix.ui.components.data.DataUnit;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public interface ListComponent<E extends Entity> extends Component, Component.BelongToFrame, ActionsHolder {

    boolean isMultiSelect();

    @Nullable
    E getSingleSelected();

    Set<E> getSelected();

    void setSelected(@Nullable E item);
    void setSelected(Collection<E> items);

    DataUnit getItems();

    /**
     * Allows to set icons for particular rows in the table.
     *
     * @param <E> entity class
     * @deprecated Use {@link Function} instead
     */
    @Deprecated
    interface IconProvider<E extends Entity> extends Function<E, String> {
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