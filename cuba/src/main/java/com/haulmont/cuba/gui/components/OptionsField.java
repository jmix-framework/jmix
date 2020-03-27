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
import com.haulmont.cuba.gui.data.options.DatasourceOptions;
import io.jmix.ui.components.data.Options;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @param <I> type of option items
 * @deprecated Use {@link io.jmix.ui.components.OptionsField} instead
 */
@SuppressWarnings("rawtypes")
public interface OptionsField<V, I> extends Field<V>, io.jmix.ui.components.OptionsField<V, I> {

    /**
     * @return options datasource
     * @deprecated Use {@link #getOptions()} instead.
     */
    @Deprecated
    default CollectionDatasource getOptionsDatasource() {
        Options<I> options = getOptions();
        if (options instanceof DatasourceOptions) {
            return ((DatasourceOptions) options).getDatasource();
        }
        return null;
    }

    /**
     * @param datasource datasource
     * @deprecated set options using {@link #setOptions(Options)} with {@link DatasourceOptions}.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    default void setOptionsDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setOptions(null);
        } else {
            setOptions(new DatasourceOptions<>(datasource));
        }
    }
}
